package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Catalog1;
import com.tan.warehouse2.bean.Catalog2;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.mapper.Catalog1Mapper;
import com.tan.warehouse2.service.Catalog1Service;
import com.tan.warehouse2.service.Catalog2Service;
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
public class Catalog1ServiceImpl implements Catalog1Service{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    Catalog1Mapper catalog1Mapper;

    @Autowired
    Catalog2Service catalog2Service;

    private final static String CACHEALLSTR = "catalog1:all:info";//name 检查
    private final static String CACHEALLSTRZSET = "catalog1:all:info:zset";//分页
    private final static String CACHEALLLOCK = "catalog1:all:lock";//加锁
    private final static String CACHELISTSTR = "catalog1:list:";//树 -- 三级分类 -- hset 方便更新

    @Override
    public int delete(Catalog1 catalog1) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                try{
                    catalog1Mapper.deleteByPrimaryKey(catalog1);
                }catch (Exception e){
                    throw e;
                }
                //判断是否要更新缓存
                Boolean flag = jedis.exists(CACHEALLSTR);
                if (flag) {//缓存中有数据
                    String str = "catalog1:" + catalog1.getName() + ":info";
                    jedis.hdel(CACHEALLSTR, str);
                    jedis.del(CACHELISTSTR + catalog1.getId());
                    jedis.zremrangeByScore(CACHEALLSTRZSET, catalog1.getId(), catalog1.getId());
                    //删除子节点
                    Map<String, String> map = jedis.hgetAll("catalog2:info:" + catalog1.getId());
                    if(map != null && map.size() > 0){
                        List<Integer> ids2 = new ArrayList<>();
                        List<String> names2 = new ArrayList<>();
                        List<String> ztreeIds2 = new ArrayList<>();
                        for (String s: map.values()){
                            Catalog2 catalog2 = JSON.parseObject(s, Catalog2.class);
                            ids2.add(catalog2.getId());
                            names2.add(catalog2.getName());
                            ztreeIds2.add(catalog2.getZtreeId());
                        }
                        catalog2Service.delete(names2, ids2, ztreeIds2, catalog1.getId());
                    }
                }
                return 1;
            }else{//有锁自旋
                return delete(catalog1);
            }
        }finally {
            destroy(jedis, lock,tryLock);
        }

    }

    @Override
    public int update(Catalog1 catalog1, String oldName) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                int i = catalog1Mapper.updateByPrimaryKeySelective(catalog1);
                if (i != 0){//更新成功
                    //判断是否要更新缓存
                    Boolean flag = jedis.exists(CACHEALLSTR);
                    if (flag) {//缓存中有数据
                        //缓存中有数据，更新缓存
                        Integer catalog1Id = catalog1.getId();
                        String s2 = CACHEALLSTR + catalog1Id;
                        String strTemp = jedis.hget(CACHEALLSTR, "catalog1:" + oldName + ":info");
                        Catalog1 c = JSON.parseObject(strTemp, Catalog1.class);
                        c.setName(catalog1.getName());
                        String s = JSON.toJSONString(c);
                        //先删除再添加
                        jedis.hdel(CACHEALLSTR, "catalog1:" + oldName + ":info");
                        jedis.hset(CACHEALLSTR, "catalog1:" + c.getName() + ":info", s);
                        jedis.hdel(CACHELISTSTR + c.getId(), c.getZtreeId());
                        jedis.hset(CACHELISTSTR + c.getId(), c.getZtreeId(), s);
//                        jedis.zadd(CACHEALLSTRZSET, catalog1.getId(), s);//直接添加不会覆盖

                        jedis.zremrangeByScore(CACHEALLSTRZSET, c.getId(), c.getId());
                        jedis.zadd(CACHEALLSTRZSET, c.getId(), s);
                    }
                    return i;
                }
                return 0;
            }else{//有锁自旋
                return update(catalog1, oldName);
            }
        }finally {
            destroy(jedis,lock,tryLock);
        }

    }

    @Override
    public PageInfo<Catalog1> getCatalog1LikeName(String name, Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        Example example = new Example(Catalog1.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andLike("name", name);
//        PageInfo<Catalog1> pageInfo = new PageInfo<>(catalog1Mapper.selectByExample(example));//[]
        PageInfo<Catalog1> pageInfo = new PageInfo<>(catalog1Mapper.getCatalog1LikeName(name));
        return pageInfo;
    }

    @Override
    public Catalog1 getCatalog1ByName(String name) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{

//            jedis = redisUtil.getJedis();
//            jedis.select(1);
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            boolean locked = lock.isLocked();
            if (!locked) {//没有被锁
                return getCatalog1ByName2(jedis, name);
            }else{//有锁自旋
                return getCatalog1ByName(name);//自旋
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    private Catalog1 getCatalog1ByName2(Jedis jedis, String name){//复用
        //判断是否要更新缓存
        boolean flag = jedis.exists(CACHEALLSTR);//null
//                Long hlen = jedis.hlen(CACHEALLSTR);
        if (!flag) {
            //没有缓存数据 -- 查询数据库
            Example example = new Example(Catalog1.class);
            Example.Criteria createCriteria1 = example.createCriteria();
            createCriteria1.andEqualTo("name", name);
            List<Catalog1> catalog1s2= catalog1Mapper.selectByExample(example);//[]
            if(catalog1s2.size() > 0){
                return catalog1s2.get(0);
            }
        }else{
            //缓存中有数据
            String str1 = jedis.hget(CACHEALLSTR,"catalog1:" + name + ":info");
            if (StringUtils.isNotBlank(str1)) {
                return JSON.parseObject(str1, Catalog1.class);
            }
        }
        return null;
    }

    @Override
//    @Transactional()//事务回滚//注释了没错也插入不了
    public int add(Catalog1 catalog1) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                //防止快速多次点击，某部分跳过name检查，所以再次检查是否已经存在数据
                Catalog1 catalog1ByName2 = getCatalog1ByName2(jedis, catalog1.getName());
                if (catalog1ByName2 == null) {//确认没有数据再添加
                    int insert = catalog1Mapper.insert(catalog1);
                    catalog1.setZtreeId("catalog1" + catalog1.getId());
                    catalog1Mapper.updateByPrimaryKey(catalog1);
                    if (insert != 0){
                        //判断是否要更新缓存
                        Boolean flag = jedis.exists(CACHEALLSTR);
                        if (flag) {//缓存中有数据
                            String str = "catalog1:" + catalog1.getName() + ":info";
                            String jsonStr = JSON.toJSONString(catalog1);
                            jedis.hset(CACHEALLSTR, str,jsonStr);
                            jedis.zadd(CACHEALLSTRZSET, catalog1.getId(), jsonStr);
                            jedis.hset(CACHELISTSTR + catalog1.getId(), catalog1.getZtreeId(), jsonStr);
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
                return add(catalog1);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    //当前页 一页多少个  mysql通过limit分页的哈
    public PageInfo<Catalog1> findCatalog1List(int page, int size) {
        // 开启分页插件,放在查询语句上面 帮助生成分页语句
        PageHelper.startPage(page, size); //底层实现原理采用改写语句   将下面的方法中的sql语句获取到然后做个拼接 limit  AOPjishu
        List<Catalog1> listCatalog1 = getAll();
        // 封装分页之后的数据  返回给客户端展示  PageInfo做了一些封装 作为一个类
        PageInfo<Catalog1> pageInfoDemo = new PageInfo<Catalog1>(listCatalog1);
        //所有分页属性都可以冲pageInfoDemo拿到；
        return pageInfoDemo;
    }

    @Override
    public List<Catalog1> getAll() {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> catalog1Map = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
                if (catalog1Map != null && catalog1Map.size() > 0) {//缓存中有数据
                    List<Catalog1> catalog1List = new ArrayList<>();
                    catalog1Map.forEach((k,v)->{
                        catalog1List.add(JSON.parseObject(v, Catalog1.class));
                    });
                    return catalog1List;
                }

                //缓存没有数据则查询数据库，并将数据同步到缓存
                // 设置分布式锁
                tryLock = lock.tryLock();
                if (tryLock) {//成功上锁
                    //缓存中没有数据,从数据库中获取
                    return addAllCache(jedis);
                } else {//有锁,自旋
                    return getAll();
                }
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return getAll();
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    @Override
    public MyPage<Catalog1> getAllPage(Integer pageNum, Integer pageSize) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> catalog1Map = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
                if (catalog1Map != null && catalog1Map.size() > 0) {//缓存中有数据
                    if (!jedis.exists(CACHEALLSTRZSET)) {
                        return null;
                    }
                    Set<String> catalog1StrSet = new HashSet<>();
                    Set<Catalog1> catalog1Set = new HashSet<>();
//                catalog1Map.forEach((k,v)->{
//                    catalog1List.add(JSON.parseObject(v, Catalog1.class));
//                });
//                    catalog1Set = jedis.zrangeByScore(CACHEALLSTR + ":zset", 0, 100, 0, pageSize);//max 不能设置为-1
                    catalog1StrSet = jedis.zrangeByScore(CACHEALLSTRZSET, "-inf", "+inf", (pageNum - 1) * pageSize, pageSize);//min max可以直接用数字字符串
                    catalog1StrSet.forEach(catalog1Str -> {
                        catalog1Set.add(JSON.parseObject(catalog1Str, Catalog1.class));
                    });

                    MyPage<Catalog1> myPage = new MyPage<>();
                    myPage.setPageNum(pageNum);
                    myPage.setPageSize(pageSize);
                    myPage.setList(catalog1Set);
                    myPage.setTotal(jedis.zcard(CACHEALLSTRZSET));
                    return myPage;
                }

                //缓存没有数据则查询数据库，并将数据同步到缓存
                // 设置分布式锁
                lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
                tryLock = lock.tryLock();
                if (tryLock) {//成功上锁
                    List<Catalog1> list = addAllCache(jedis);
                    if (list == null) {//数据库数据为空
                        return null;
                    }
                    return getAllPage(pageNum, pageSize);//自旋重新获取数据
                } else {//有锁,自旋
                    return getAllPage(pageNum, pageSize);
                }
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return getAllPage(pageNum, pageSize);
            }

        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    //内部方法调用更新缓存
    private List<Catalog1> addAllCache(Jedis jedis){
        //缓存中没有数据,从数据库中获取
        List<Catalog1> catalog1s = catalog1Mapper.selectAll();//没有值返回 []

        if (catalog1s != null && catalog1s.size() > 0) {
            for (Catalog1 catalog1 : catalog1s) {
                String str = "catalog1:" + catalog1.getName() + ":info";
                String jsonStr = JSON.toJSONString(catalog1);
                jedis.hset(CACHEALLSTR, str, jsonStr);
                jedis.zadd(CACHEALLSTRZSET, catalog1.getId(), jsonStr);
                jedis.hset(CACHELISTSTR + catalog1.getId(),catalog1.getZtreeId(), jsonStr);
            }
            return catalog1s;
        }else {
            // 数据库没有
            // 为了防止缓存穿透将，null或者空字符串值设置给redis
//                    jedis.hmset(CACHEALLSTR, null);//这样写报错 -- NullPointerException
            jedis.hset(CACHEALLSTR, "", "");
            jedis.expire(CACHEALLSTR, 60);
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
