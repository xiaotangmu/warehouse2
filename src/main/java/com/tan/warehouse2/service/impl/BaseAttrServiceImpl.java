package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.tan.warehouse2.bean.BaseAttr;
import com.tan.warehouse2.mapper.BaseAttrMapper;
import com.tan.warehouse2.service.BaseAttrService;
import com.tan.warehouse2.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class BaseAttrServiceImpl implements BaseAttrService{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    BaseAttrMapper baseAttrMapper;

    private final static String CACHEALLSTR = "baseAttr:info:";//name 检查 -- baseAttr:info:id(catalog3)
    private final static String CACHEALLLOCK = "baseAttr:lock:";//加锁 -- baseAttr:lock:id(catalog3)

    @Override
    public Set<BaseAttr> getAttrAndValueBySpuId(Integer spuId) {
        List<BaseAttr> baseAttrs = baseAttrMapper.getAttrBySpuId(spuId);
        if(baseAttrs != null && baseAttrs.size() > 0){
            for (BaseAttr baseAttr : baseAttrs) {
                List<String> list = baseAttrMapper.getValueByAttrIdAndSpuId(baseAttr.getId(), spuId);
                if(list != null){
                    System.out.println("base value: " + list);

                    baseAttr.setValue(list);
                }else{
                    baseAttr.setValue(new ArrayList<>());
                }
            }
        }
        Set<BaseAttr> baseAttrSet = new HashSet<>();
        baseAttrSet.addAll(baseAttrs);
        return baseAttrSet;
    }

    @Override
    public int delete(List<String> names, List<Integer> ids, Integer catalog3Id) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            String strKey = CACHEALLSTR + catalog3Id;
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog3Id);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                try{
                    baseAttrMapper.deleteBaseAttrs(ids);//返回null
                }catch (Exception e){
                    throw e;
                }
                //判断是否要更新缓存
                Boolean flag = jedis.exists(strKey);
                if (flag) {//缓存中有数据
                    for (String name : names) {
                        jedis.hdel(strKey, name);
                    }
                }
                return 1;
            }else{//有锁自旋
                return delete(names,ids, catalog3Id);
            }
        }finally {
            destroy(jedis, lock,tryLock);
        }

    }

    @Override
    public int update(BaseAttr baseAttr, String oldName) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            Integer catalog3Id = baseAttr.getCatalog3Id();
            String strKey = CACHEALLSTR + catalog3Id;
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog3Id);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                int i = baseAttrMapper.updateByPrimaryKeySelective(baseAttr);
                if (i != 0){//更新成功
                    //判断是否要更新缓存
                    Boolean flag = jedis.exists(strKey);
                    if (flag) {//缓存中有数据
                        //缓存中有数据，更新缓存
                        String strTemp = jedis.hget(strKey, oldName);
                        BaseAttr c = JSON.parseObject(strTemp, BaseAttr.class);
                        c.setName(baseAttr.getName());
                        String s = JSON.toJSONString(c);
                        //先删除再添加
                        jedis.hdel(strKey, oldName);
                        jedis.hset(strKey, c.getName(), s);
                    }
                    return i;
                }
                return 0;
            }else{//有锁自旋
                return update(baseAttr, oldName);
            }
        }finally {
            destroy(jedis,lock,tryLock);
        }

    }

//    @Override
//    public PageInfo<BaseAttr> getBaseAttrLikeName(String name, Integer pageNum,Integer pageSize) {
//        PageHelper.startPage(pageNum, pageSize);
////        Example example = new Example(BaseAttr.class);
////        Example.Criteria criteria = example.createCriteria();
////        criteria.andLike("name", name);
////        PageInfo<BaseAttr> pageInfo = new PageInfo<>(baseAttrMapper.selectByExample(example));//[]
//        PageInfo<BaseAttr> pageInfo = new PageInfo<>(baseAttrMapper.getBaseAttrLikeName(name));
//        return pageInfo;
//    }

    @Override
    public BaseAttr getBaseAttrByName(String name, Integer catalog3Id) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{

//            jedis = redisUtil.getJedis();
//            jedis.select(1);
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog3Id);// 声明锁
            boolean locked = lock.isLocked();
            if (!locked) {//没有被锁
                return getBaseAttrByName2(jedis, name, catalog3Id);
            }else{//有锁自旋
                return getBaseAttrByName(name,catalog3Id);//自旋
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    private BaseAttr getBaseAttrByName2(Jedis jedis, String name, Integer catalog3Id){//复用
        //判断是否要更新缓存
        String strKey = CACHEALLSTR + catalog3Id;
        boolean flag = jedis.exists(strKey);//null
//                Long hlen = jedis.hlen(CACHEALLSTR);
        if (!flag) {
            //没有缓存数据 -- 查询数据库
            Example example = new Example(BaseAttr.class);
            Example.Criteria createCriteria1 = example.createCriteria();
            createCriteria1.andEqualTo("name", name);
            createCriteria1.andEqualTo("catalog3Id", catalog3Id);
            List<BaseAttr> baseAttrs2= baseAttrMapper.selectByExample(example);//[]
            if(baseAttrs2.size() > 0){
                return baseAttrs2.get(0);
            }
        }else{
            //缓存中有数据
            String str1 = jedis.hget(strKey,name);
            if (StringUtils.isNotBlank(str1)) {
                return JSON.parseObject(str1, BaseAttr.class);
            }
        }
        return null;
    }

    @Override
//    @Transactional()//事务回滚//注释了没错也插入不了
    public int add(BaseAttr baseAttr) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            Integer catalog3Id = baseAttr.getCatalog3Id();
            String strKey = CACHEALLSTR + catalog3Id;
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog3Id);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                //防止快速多次点击，某部分跳过name检查，所以再次检查是否已经存在数据
                BaseAttr baseAttrByName2 = getBaseAttrByName2(jedis, baseAttr.getName(), baseAttr.getCatalog3Id());
                if (baseAttrByName2 == null) {//确认没有数据再添加
                    int insert = baseAttrMapper.insert(baseAttr);
                    System.out.println("hello");
                    System.out.println(baseAttr);

                    if (insert != 0){
                        //判断是否要更新缓存
                        Boolean flag = jedis.exists(strKey);
                        if (flag) {//缓存中有数据
                            String str = baseAttr.getName();
                            String jsonStr = JSON.toJSONString(baseAttr);
                            jedis.hset(strKey, str,jsonStr);
                        }else{
                            //添加缓存，不然容易出错 穿透
                            addAllCache(jedis,catalog3Id);
                        }
                        return insert;
                    }

                    return 0;//没有插入
                }
                return -1;//已经存在
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return add(baseAttr);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    @Override
    public List<BaseAttr> getAll(Integer catalog3Id) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{

            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog3Id);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> baseAttrMap = jedis.hgetAll(CACHEALLSTR + catalog3Id);//不存在返回 {}
                if (baseAttrMap != null && baseAttrMap.size() > 0) {//缓存中有数据
                    List<BaseAttr> baseAttrList = new ArrayList<>();
                    baseAttrMap.forEach((k,v)->{
                        baseAttrList.add(JSON.parseObject(v, BaseAttr.class));
                    });
                    return baseAttrList;
                }

                //缓存没有数据则查询数据库，并将数据同步到缓存
                // 设置分布式锁
                tryLock = lock.tryLock();
                if (tryLock) {//成功上锁
                    //缓存中没有数据,从数据库中获取
                    return addAllCache(jedis,catalog3Id);
                } else {//有锁,自旋
                    return getAll(catalog3Id);
                }
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return getAll(catalog3Id);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }


    //内部方法调用更新缓存
    private List<BaseAttr> addAllCache(Jedis jedis, Integer catalog3Id){
        if(catalog3Id == null || catalog3Id <= 0){
            return null;
        }
        //缓存中没有数据,从数据库中获取
        Example example = new Example(BaseAttr.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("catalog3Id", catalog3Id);
        List<BaseAttr> baseAttrs = baseAttrMapper.selectByExample(example);

        String strKey = CACHEALLSTR + catalog3Id;

        if (baseAttrs != null && baseAttrs.size() > 0) {
            for (BaseAttr baseAttr : baseAttrs) {
                String str = baseAttr.getName();
                String jsonStr = JSON.toJSONString(baseAttr);
                jedis.hset(strKey, str, jsonStr);
            }
            return baseAttrs;
        }else {
            // 数据库没有
            // 为了防止缓存穿透将，null或者空字符串值设置给redis
//                    jedis.hmset(CACHEALLSTR, null);//这样写报错 -- NullPointerException
            jedis.hset(strKey, "", "");
            jedis.expire(strKey, 60);
        }
        return null;
    }

    private Jedis initCache(){
        Jedis jedis = redisUtil.getJedis();
        jedis.select(1);
        return jedis;
    }
    private void destroy(Jedis jedis, RLock lock, boolean tryLock){
        if (jedis != null) {
            jedis.close();
        }
        if (tryLock && lock != null) {
            lock.unlock();//解锁
        }
    }

}
