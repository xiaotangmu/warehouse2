package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.BaseAttr;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.bean.Spu;
import com.tan.warehouse2.mapper.SpuMapper;
import com.tan.warehouse2.service.SpuService;
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
public class SpuServiceImpl implements SpuService{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    SpuMapper spuMapper;

    private final static String CACHEALLSTR = "spu:info:";//name 检查 -- spu:info:id(catalog3):brandId
    private final static String CACHEALLLOCK = "spu:lock:";//加锁 -- spu:lock:id(catalog3)
    private final static String CACHEALLSTRZSET = "spu:info:zset";//分页
//    private final static String CACHECATALOGZSET = "spu:catalog:";//catalog3Id -- 分页zset
//    private final static String CACHEBRANDZSET = "spu:brand:";//brandId -- 分页zset

    @Override
    public int delete(List<Spu> spus){
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        RLock lock2 = null;
        boolean tryLock2 = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if(tryLock){
                //判断是否要更新缓存
                Boolean flag = jedis.exists(CACHEALLSTRZSET);
                for (int i=0; i< spus.size(); i++){
                    Spu spu = spus.get(i);
                    String strKey = CACHEALLSTR + spu.getCatalog3Id() + ":" + spu.getBrandId();
                    spuMapper.deleteByPrimaryKey(spu);
                    //删除关联属性
                    List<Integer> ids = new ArrayList<>();
                    ids.add(spu.getId());
                    spuMapper.deleteAttr(ids);
                    spuMapper.deleteAttrValues(ids);

                    if(flag){
                        jedis.hdel(strKey, spu.getName());
                        jedis.zremrangeByScore(CACHEALLSTRZSET, spu.getId(), spu.getId());
                    }

                }
                return 1;

            }else{//有锁自旋
                return delete(spus);
            }
        }finally {
            destroy(jedis, lock,tryLock);
        }

    }

    @Override
    public int update(Spu spu, String oldName, Integer oldBrandId) {
        Jedis jedis = null;
        RLock lock = null;
        RLock lock2 = null;
        boolean tryLock = false;
        boolean tryLock2 = false;
        RLock lock3 = null;
        boolean tryLock3 = false;
        try{
            Integer catalog3Id = spu.getCatalog3Id();
            Integer brandId = spu.getBrandId();
            String strKey = CACHEALLSTR + catalog3Id + ":" + brandId;
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog3Id + ":" + brandId);// 声明锁
            tryLock = lock.tryLock();
            lock2 = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock2 = lock2.tryLock();

            boolean flag = tryLock && tryLock2;
            boolean flag2 = false;
            if(brandId != oldBrandId){
                lock3 = redissonClient.getLock(CACHEALLLOCK);// 声明锁
                tryLock3 = lock2.tryLock();
                flag = flag && tryLock3;
                flag2 = true;
            }

            if (flag) {//成功上锁
                int i = spuMapper.updateByPrimaryKey(spu);
                //更新attr
                List<Integer> ids = new ArrayList<>();
                ids.add(spu.getId());
                spuMapper.deleteAttr(ids);
                spuMapper.deleteAttrValues(ids);
                List<BaseAttr> baseAttrs = spu.getBaseAttrs();
                List<Integer> ids2 = new ArrayList<>();
                if(baseAttrs != null || baseAttrs.size() > 0){

//                        baseAttrs.forEach(item -> {
//                            ids2.add(item.getId());
//                        });
                    spuMapper.insertAttr(baseAttrs,spu.getId());
                    for (BaseAttr b: baseAttrs){
                        if(b.getValue() != null && b.getValue().size() > 0){
                            spuMapper.insertAttrValue(b.getValue(),b.getCatalog3Id(),b.getId());
                        }
                    }
                }

                if (i != 0){//更新成功
                    //判断是否要更新缓存
                    Boolean flag3 = jedis.exists(CACHEALLSTRZSET);
                    if (flag3) {//缓存中有数据
                        //缓存中有数据，更新缓存
                        String s = JSON.toJSONString(spu);
                        //先删除再添加
                        if(flag2){//两次品牌不同
                            jedis.hdel(CACHEALLSTR + catalog3Id + ":" + oldBrandId, oldName);
                        }else{
                            jedis.hdel(strKey, oldName);
                        }
                        jedis.hset(strKey, spu.getName(), s);

                        jedis.zremrangeByScore(CACHEALLSTRZSET, spu.getId(), spu.getId());
                        jedis.zadd(CACHEALLSTRZSET, spu.getId(), s);
                    }
                    return i;
                }
                return 0;
            }else{//有锁自旋
                return update(spu, oldName, oldBrandId);
            }
        }finally {
            if(tryLock2 && lock2 != null){
                lock2.unlock();
            }
            if(tryLock3){
                lock3.unlock();
            }
            destroy(jedis,lock,tryLock);
        }

    }

    @Override
    public PageInfo<Spu> getSpuLikeName(String name, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        Example example = new Example(Spu.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andLike("name", name);
//        PageInfo<Spu> pageInfo = new PageInfo<>(spuMapper.selectByExample(example));//[]
        PageInfo<Spu> pageInfo = new PageInfo<>(spuMapper.getSpuLikeName(name));
        List<Spu> list = pageInfo.getList();
        list.forEach(item -> {
            List<BaseAttr> attr = spuMapper.getAttr(item.getId());
            if(attr != null && attr.size() > 0){
                for (BaseAttr b: attr){
                    List<String> attrValues = spuMapper.getAttrValues(item.getId(), b.getId());
                    b.setValue(attrValues);
                }
            }
            item.setBaseAttrs(attr);
        });

        return pageInfo;
    }

    @Override
    public Spu getSpuByName(String name, Integer catalog3Id, Integer brandId) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{

//            jedis = redisUtil.getJedis();
//            jedis.select(1);
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog3Id + ":" + brandId);// 声明锁
            boolean locked = lock.isLocked();
            if (!locked) {//没有被锁
                return getSpuByName2(jedis, name, catalog3Id,brandId);
            }else{//有锁自旋
                return getSpuByName(name,catalog3Id, brandId);//自旋
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    @Override
    public MyPage<Spu> getAllPage(Integer pageNum, Integer pageSize) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Boolean exists = jedis.exists(CACHEALLSTRZSET);
                if (exists) {
                    Set<String> brandStrSet = new HashSet<>();
                    Set<Spu> brandSet = new HashSet<>();
//                brandMap.forEach((k,v)->{
//                    brandList.add(JSON.parseObject(v, Brand.class));
//                });
//                    brandSet = jedis.zrangeByScore(CACHEALLSTR + ":zset", 0, 100, 0, pageSize);//max 不能设置为-1
                    brandStrSet = jedis.zrangeByScore(CACHEALLSTRZSET, "-inf", "+inf", (pageNum - 1) * pageSize, pageSize);//min max可以直接用数字字符串
                    brandStrSet.forEach(brandStr -> {
                        brandSet.add(JSON.parseObject(brandStr, Spu.class));
                    });

                    MyPage<Spu> myPage = new MyPage<>();
                    myPage.setPageNum(pageNum);
                    myPage.setPageSize(pageSize);
                    myPage.setList(brandSet);
                    myPage.setTotal(jedis.zcard(CACHEALLSTRZSET));
                    return myPage;
                }else{
                    //缓存没有数据则查询数据库，并将数据同步到缓存
                    // 设置分布式锁
                    lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
                    tryLock = lock.tryLock();
                    if (tryLock) {//成功上锁
                        List<Spu> list = addAllCache(jedis);
                        if (list == null) {//数据库数据为空
                            return null;
                        }
                        return getAllPage(pageNum, pageSize);//自旋重新获取数据
                    } else {//有锁,自旋
                        return getAllPage(pageNum, pageSize);
                    }
                }



            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return getAllPage(pageNum, pageSize);
            }

        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    private Spu getSpuByName2(Jedis jedis, String name, Integer catalog3Id, Integer brandId){//复用
        //判断是否要更新缓存
        String strKey = CACHEALLSTR + catalog3Id + ":" + brandId;
        boolean flag = jedis.exists(CACHEALLSTRZSET);//null
//                Long hlen = jedis.hlen(CACHEALLSTR);
        if (!flag) {
            //没有缓存数据 -- 查询数据库
            Example example = new Example(Spu.class);
            Example.Criteria createCriteria1 = example.createCriteria();
            createCriteria1.andEqualTo("name", name);
            createCriteria1.andEqualTo("catalog3Id", catalog3Id);
            createCriteria1.andEqualTo("brandId", brandId);
            List<Spu> spus2= spuMapper.selectByExample(example);//[]
            if(spus2.size() > 0){
                return spus2.get(0);
            }
        }else{
            //缓存中有数据
            String str1 = jedis.hget(strKey,name);
            if (StringUtils.isNotBlank(str1)) {
                return JSON.parseObject(str1, Spu.class);
            }
        }
        return null;
    }

    @Override
//    @Transactional()//事务回滚//注释了没错也插入不了
    public int add(Spu spu) {

        Jedis jedis = null;
        RLock lock = null;
        RLock lock2 = null;
        boolean tryLock = false;
        boolean tryLock2 = false;
        try{

            Integer catalog3Id = spu.getCatalog3Id();
            Integer brandId = spu.getBrandId();
            String str1 = catalog3Id + ":" + brandId;
            String strKey = CACHEALLSTR + str1;

            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + str1);// 声明锁
            lock2 = redissonClient.getLock(CACHEALLLOCK + str1);// 声明锁
            tryLock = lock.tryLock();
            tryLock2 = lock2.tryLock();
            if (tryLock && tryLock2) {//成功上锁
                //防止快速多次点击，某部分跳过name检查，所以再次检查是否已经存在数据
                Spu spuByName2 = getSpuByName2(jedis, spu.getName(), catalog3Id, brandId);
                if (spuByName2 == null) {//确认没有数据再添加
                    int insert = spuMapper.insert(spu);
                    List<BaseAttr> baseAttrs = spu.getBaseAttrs();
                    System.out.println("add"  + spu);
                    if(baseAttrs != null && baseAttrs.size() > 0){
//                        List<Integer> list = new ArrayList<>();
//                        baseAttrs.forEach(item -> {
//                            list.add(item.getId());
//                        });
                        spuMapper.insertAttr(baseAttrs, spu.getId());
                        for (BaseAttr b: baseAttrs){
                            if(b.getValue() != null && b.getValue().size() > 0){
                                spuMapper.insertAttrValue(b.getValue(),spu.getId(),b.getId());
                            }
                        }
                    }

                    if (insert != 0){
                        //判断是否要更新缓存
                        Boolean flag = jedis.exists(CACHEALLSTRZSET);
                        if (flag) {//缓存中有数据
                            String str = spu.getName();
                            String jsonStr = JSON.toJSONString(spu);
                            jedis.hset(strKey, str,jsonStr);
                            jedis.zadd(CACHEALLSTRZSET, spu.getId(), jsonStr);
                        }else{
                            //添加缓存，不然容易出错 穿透
                            addAllCache(jedis);
                        }
                        return insert;
                    }

                    return 0;//没有插入
                }
                return -1;//已经存在
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return add(spu);
            }
        }finally {
            if(tryLock2 && lock2 != null){
                lock2.unlock();
            }
            destroy(jedis, lock, tryLock);
        }

    }

    @Override
    public List<Spu> getAll(Integer catalog3Id, Integer brandId) {

        Jedis jedis = null;
        RLock lock = null;
        RLock lock2 = null;
        boolean tryLock = false;
        boolean tryLock2 = false;
        try{

            String str = catalog3Id + ":" + brandId;
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + str);// 声明锁
            lock2 = redissonClient.getLock(CACHEALLLOCK );// 声明锁
            boolean isLocked = lock.isLocked();
            boolean isLocked2 = lock2.isLocked();
            if (!isLocked && !isLocked2) {//没有被锁
                Boolean exists = jedis.exists(CACHEALLSTRZSET);
                if (exists) {
                    Map<String, String> spuMap = jedis.hgetAll(CACHEALLSTR + str);//不存在返回 {}
                    if (spuMap != null && spuMap.size() > 0) {//缓存中有数据
                        List<Spu> spuList = new ArrayList<>();
                        spuMap.forEach((k, v) -> {
                            spuList.add(JSON.parseObject(v, Spu.class));
                        });
                        return spuList;
                    }
                    return null;
                }else{

                    //缓存没有数据则查询数据库，并将数据同步到缓存
                    // 设置分布式锁
                    tryLock = lock.tryLock();
                    tryLock2 = lock2.tryLock();

                    if (tryLock && tryLock2) {//成功上锁
                        //缓存中没有数据,从数据库中获取
                        return addAllCache(jedis);
                    } else {//有锁,自旋
                        return getAll(catalog3Id, brandId);
                    }
                }
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return getAll(catalog3Id, brandId);
            }
        }finally {
            if (tryLock2 && lock2 != null) {
                lock2.unlock();//解锁
            }
            destroy(jedis, lock, tryLock);
        }

    }


    //内部方法调用更新缓存
    private List<Spu> addAllCache(Jedis jedis){
        //缓存中没有数据,从数据库中获取
//        Example example = new Example(Spu.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andEqualTo("catalog3Id", catalog3Id);
//        criteria.andEqualTo("brandId", brandId);
//        List<Spu> spus = spuMapper.selectByExample(example);
        List<Spu> spus = spuMapper.selectAll();

        if (spus != null && spus.size() > 0) {

            for (Spu s: spus){
                List<BaseAttr> attrs = spuMapper.getAttr(s.getId());

                for(BaseAttr b: attrs){
                    List<String> values = spuMapper.getAttrValues(s.getId(), b.getId());
                    b.setValue(values);
                    System.out.println(values);
                }

                s.setBaseAttrs(attrs);

                Integer catalog3Id = s.getCatalog3Id();
                Integer brandId = s.getBrandId();
                String strKey = CACHEALLSTR + catalog3Id + ":" + brandId;
                String str = s.getName();

                String jsonStr = JSON.toJSONString(s);
                jedis.hset(strKey, str, jsonStr);
                jedis.zadd(CACHEALLSTRZSET, s.getId(), jsonStr);
            }
            return spus;
        }else {
            // 数据库没有
            // 为了防止缓存穿透将，null或者空字符串值设置给redis
//                    jedis.hmset(CACHEALLSTR, null);//这样写报错 -- NullPointerException
            jedis.zadd(CACHEALLSTRZSET, 0, "");
            jedis.expire(CACHEALLSTRZSET, 60);
        }
        return null;

    }

    private Jedis initCache(){
        Jedis jedis = redisUtil.getJedis();
        jedis.select(1);
        return jedis;
    }
    private void destroy(Jedis jedis, RLock lock, boolean tryLock){
        if (tryLock && lock != null) {
            lock.unlock();//解锁
        }
        if (jedis != null) {
            jedis.close();
        }
    }

}
