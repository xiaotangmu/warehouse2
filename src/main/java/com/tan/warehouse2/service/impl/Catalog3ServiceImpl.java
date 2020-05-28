package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Catalog;
import com.tan.warehouse2.bean.Catalog3;
import com.tan.warehouse2.mapper.Catalog3Mapper;
import com.tan.warehouse2.service.Catalog3Service;
import com.tan.warehouse2.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class Catalog3ServiceImpl implements Catalog3Service{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    Catalog3Mapper catalog3Mapper;

    private final static String CACHEALLSTR = "catalog3:info:";//检查是否存在
    private final static String CACHEALLSTRZSET = "catalog3:all:info:zset";//分页
    private final static String CACHEALLLOCK = "catalog3:lock:";//加锁
    private final static String CACHELISTSTR = "catalog1:list:";//树 -- 三级分类

    @Override
    public int delete(List<String> names, List<Integer> ids, List<String> ztreeIds, Integer catalog1Id, Integer catalog2Id) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog2Id);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                try{
                    System.out.println(ids);
                    catalog3Mapper.deleteCatalogs(ids);//返回null
                }catch (Exception e){
                    throw e;
                }
                String strAll = CACHEALLSTR + catalog2Id;
                //判断是否要更新缓存
                Boolean flag = jedis.exists(strAll);
                if (flag) {//缓存中有数据
                    for (String name : names) {
                        String str = "catalog3:" + name + ":info";
                        jedis.hdel(strAll, str);
                    }
                    for (String ztreeId: ztreeIds){
                        jedis.hdel(CACHELISTSTR + catalog1Id, ztreeId);
                    }
                    for (Integer id : ids) {
                        jedis.zremrangeByScore(CACHEALLSTRZSET, id, id);
                    }
                }
                return 1;
            }else{//有锁自旋
                return delete(names,ids, ztreeIds, catalog1Id, catalog2Id);
            }
        }finally {
            destroy(jedis, lock,tryLock);
        }

    }

    @Override
    public int update(Catalog3 catalog3, String oldName) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();

            Integer catalog2Id = catalog3.getCatalog2Id();
            Integer catalog1Id = catalog3.getCatalog1Id();

            lock = redissonClient.getLock(CACHEALLLOCK + catalog2Id);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                int i = catalog3Mapper.updateByPrimaryKeySelective(catalog3);
                if (i != 0){//更新成功
                    //判断是否要更新缓存
                    String s2 = CACHEALLSTR + catalog2Id;
                    String oldStr = "catalog3:" + oldName + ":info";
                    String strTemp = jedis.hget(s2, oldStr);
                    Catalog3 c = JSON.parseObject(strTemp, Catalog3.class);

                    Boolean flag = jedis.exists(s2);
                    if (flag) {//缓存中有数据
                        //缓存中有数据，更新缓存

                        c.setName(catalog3.getName());
                        String s = JSON.toJSONString(c);

                        //删除原来的
                        jedis.hdel(s2, oldStr);
                        jedis.hset(s2, "catalog3:" + c.getName() + ":info", s);
                        jedis.hdel(CACHELISTSTR + catalog1Id, c.getZtreeId());
                        jedis.hset(CACHELISTSTR + catalog1Id, c.getZtreeId(), s);
//                        jedis.zadd(CACHEALLSTRZSET, catalog3.getId(), s);//直接添加不会覆盖
                        //先删除再添加
                        jedis.zremrangeByScore(CACHEALLSTRZSET, c.getId(), c.getId());
                        jedis.zadd(CACHEALLSTRZSET, c.getId(), s);
                    }
                    return i;
                }
                return 0;
            }else{//有锁自旋
                return update(catalog3,oldName);
            }
        }finally {
            destroy(jedis,lock,tryLock);
        }

    }

    @Override
    public PageInfo<Catalog3> getCatalog3LikeName(String name, Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        Example example = new Example(Catalog3.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andLike("name", name);
//        PageInfo<Catalog3> pageInfo = new PageInfo<>(catalog3Mapper.selectByExample(example));//[]
        PageInfo<Catalog3> pageInfo = new PageInfo<>(catalog3Mapper.getCatalog3LikeName(name));
        return pageInfo;
    }

    @Override
    public Catalog3 getCatalog3ByName(String name, Integer catalog2Id) {

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
                return getCatalog3ByName2(jedis, name, catalog2Id);
            }else{//有锁自旋
                return getCatalog3ByName(name,catalog2Id);//自旋
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    private Catalog3 getCatalog3ByName2(Jedis jedis, String name, Integer catalog2Id){//复用
        //判断是否要更新缓存
        boolean flag = jedis.exists(CACHEALLSTR + catalog2Id);//null
//                Long hlen = jedis.hlen(CACHEALLSTR);
        if (!flag) {
            //没有缓存数据 -- 查询数据库
            Example example = new Example(Catalog3.class);
            Example.Criteria createCriteria1 = example.createCriteria();
            createCriteria1.andEqualTo("name", name);
            createCriteria1.andEqualTo("catalog2Id", catalog2Id);
            List<Catalog3> catalog3s2= catalog3Mapper.selectByExample(example);//[]
            if(catalog3s2.size() > 0){
                return catalog3s2.get(0);
            }
        }else{
            //缓存中有数据
            String str1 = jedis.hget(CACHEALLSTR + catalog2Id,"catalog3:" + name + ":info");
            if (StringUtils.isNotBlank(str1)) {
                return JSON.parseObject(str1, Catalog3.class);
            }
        }
        return null;
    }

    @Override
    public int add(Catalog3 catalog3) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            Integer catalog2Id = catalog3.getCatalog2Id();
            String strCache =  CACHEALLSTR + catalog2Id;

            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog2Id);// 声明锁
            tryLock = lock.tryLock();

            if (tryLock) {//成功上锁
                //防止快速多次点击，某部分跳过name检查，所以再次检查是否已经存在数据
                Catalog3 catalog3ByName2 = getCatalog3ByName2(jedis, catalog3.getName(),catalog3.getCatalog2Id());
                if (catalog3ByName2 == null) {//确认没有数据再添加
                    int insert = catalog3Mapper.insert(catalog3);
                    catalog3.setZtreeId("catalog3" + catalog3.getId());
                    catalog3Mapper.updateByPrimaryKey(catalog3);
                    System.out.println("catalog3" + catalog3);
                    if (insert != 0){
                        //判断是否要更新缓存
                        Boolean flag = jedis.exists(strCache);
                        if (flag) {//缓存中有数据
                            String str = "catalog3:" + catalog3.getName() + ":info";
                            String jsonStr = JSON.toJSONString(catalog3);
                            jedis.hset(strCache, str, jsonStr);
                            jedis.zadd(CACHEALLSTRZSET, catalog3.getId(), jsonStr);
                            jedis.hset(CACHELISTSTR + catalog3.getCatalog1Id(), catalog3.getZtreeId(), jsonStr);
                        }else{
                            //添加缓存，不然容易出错 穿透
                            addAllCache(jedis, catalog2Id);
                        }
                        return insert;
                    }

                    return 0;//没有插入
                }
                return -1;//已经存在
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return add(catalog3);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    //当前页 一页多少个  mysql通过limit分页的哈
//    public PageInfo<Catalog3> findCatalog3List(int page, int size) {
//        // 开启分页插件,放在查询语句上面 帮助生成分页语句
//        PageHelper.startPage(page, size); //底层实现原理采用改写语句   将下面的方法中的sql语句获取到然后做个拼接 limit  AOPjishu
//        List<Catalog3> listCatalog3 = getAll();
//        // 封装分页之后的数据  返回给客户端展示  PageInfo做了一些封装 作为一个类
//        PageInfo<Catalog3> pageInfoDemo = new PageInfo<Catalog3>(listCatalog3);
//        //所有分页属性都可以冲pageInfoDemo拿到；
//        return pageInfoDemo;
//    }

    @Override
    public List<Catalog3> getAll(Integer catalog2Id) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog2Id);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> catalog3Map = jedis.hgetAll(CACHEALLSTR + catalog2Id);//不存在返回 {}
                if (catalog3Map != null && catalog3Map.size() > 0) {//缓存中有数据
                    List<Catalog3> catalog3List = new ArrayList<>();
                    catalog3Map.forEach((k,v)->{
                        catalog3List.add(JSON.parseObject(v, Catalog3.class));
                    });
                    return catalog3List;
                }

                //缓存没有数据则查询数据库，并将数据同步到缓存
                // 设置分布式锁
                tryLock = lock.tryLock();
                if (tryLock) {//成功上锁
                    //缓存中没有数据,从数据库中获取
                    return addAllCache(jedis, catalog2Id);
                } else {//有锁,自旋
                    return getAll(catalog2Id);
                }
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return getAll(catalog2Id);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }
    }
    @Override
    public List<Catalog> getAll2(Integer catalog2Id, Integer catalog1Id) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog2Id);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁

                Long hlen = jedis.hlen(CACHEALLSTR + catalog1Id);//不存在返回 {}
                if (hlen != null && hlen > 0) {//缓存中有数据
                    return getCatalog(catalog1Id, jedis);
                }

                //缓存没有数据则查询数据库，并将数据同步到缓存
                // 设置分布式锁
                tryLock = lock.tryLock();
                if (tryLock) {//成功上锁
                    //缓存中没有数据,从数据库中获取
                    addAllCache(jedis, catalog2Id);
                    return getCatalog(catalog1Id, jedis);
                } else {//有锁,自旋
                    return getAll2(catalog2Id, catalog1Id);
                }
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return getAll2(catalog2Id,catalog1Id);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }
    }
    public List<Catalog> getCatalog(Integer catalog1Id, Jedis jedis){
        Map<String, String> map = jedis.hgetAll(CACHELISTSTR + catalog1Id);
        List<Catalog> list = new ArrayList<>();
        if(map != null && map.size() > 0){
            map.forEach((k,v) -> {
                Catalog catalog = JSON.parseObject(v, Catalog.class);
                list.add(catalog);
            });
            return list;
        }
        return null;
    }

//    @Override
//    public MyPage<Catalog3> getAllPage(Integer pageNum, Integer pageSize) {
//
//        Jedis jedis = null;
//        RLock lock = null;
//        boolean tryLock = false;
//        try{
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//            boolean isLocked = lock.isLocked();
//            if (!isLocked) {//没有被锁
//                Map<String, String> catalog3Map = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
//                if (catalog3Map != null && catalog3Map.size() > 0) {//缓存中有数据
//                    if (!jedis.exists(CACHEALLSTRZSET)) {
//                        return null;
//                    }
//                    Set<String> catalog3StrSet = new HashSet<>();
//                    Set<Catalog3> catalog3Set = new HashSet<>();
////                catalog3Map.forEach((k,v)->{
////                    catalog3List.add(JSON.parseObject(v, Catalog3.class));
////                });
////                    catalog3Set = jedis.zrangeByScore(CACHEALLSTR + ":zset", 0, 100, 0, pageSize);//max 不能设置为-1
//                    catalog3StrSet = jedis.zrangeByScore(CACHEALLSTRZSET, "-inf", "+inf", (pageNum - 1) * pageSize, pageSize);//min max可以直接用数字字符串
//                    catalog3StrSet.forEach(catalog3Str -> {
//                        catalog3Set.add(JSON.parseObject(catalog3Str, Catalog3.class));
//                    });
//
//                    MyPage<Catalog3> myPage = new MyPage<>();
//                    myPage.setPageNum(pageNum);
//                    myPage.setPageSize(pageSize);
//                    myPage.setList(catalog3Set);
//                    myPage.setTotal(jedis.zcard(CACHEALLSTRZSET));
//                    return myPage;
//                }
//
//                //缓存没有数据则查询数据库，并将数据同步到缓存
//                // 设置分布式锁
//                lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//                tryLock = lock.tryLock();
//                if (tryLock) {//成功上锁
//                    List<Catalog3> list = addAllCache(jedis);
//                    if (list == null) {//数据库数据为空
//                        return null;
//                    }
//                    return getAllPage(pageNum, pageSize);//自旋重新获取数据
//                } else {//有锁,自旋
//                    return getAllPage(pageNum, pageSize);
//                }
//            }else{//有锁自旋
//                //添加缓存，不然容易出错 穿透
//                return getAllPage(pageNum, pageSize);
//            }
//
//        }finally {
//            destroy(jedis, lock, tryLock);
//        }
//
//    }

    //内部方法调用更新缓存
    private List<Catalog3> addAllCache(Jedis jedis, Integer catalog2Id){
        //缓存中没有数据,从数据库中获取
//        List<Catalog3> catalog3s = catalog3Mapper.selectAll();//没有值返回 []
        Example example = new Example(Catalog3.class);
        Example.Criteria createCriteria1 = example.createCriteria();
        createCriteria1.andEqualTo("catalog2Id", catalog2Id);
        List<Catalog3> catalog3s= catalog3Mapper.selectByExample(example);//[]
        String strCache = CACHEALLSTR + catalog2Id;

        if (catalog3s != null && catalog3s.size() > 0) {
            for (Catalog3 catalog3 : catalog3s) {
                String str = "catalog3:" + catalog3.getName() + ":info";
                String jsonStr = JSON.toJSONString(catalog3);
                jedis.hset(strCache, str,jsonStr);
                //用来分页
                jedis.zadd(CACHEALLSTRZSET, catalog3.getId(), jsonStr);
                jedis.hset(CACHELISTSTR + catalog3.getCatalog1Id(), catalog3.getZtreeId(), jsonStr);
            }
            return catalog3s;
        }else {
            // 数据库没有
            // 为了防止缓存穿透将，null或者空字符串值设置给redis
//                    jedis.hmset(CACHEALLSTR, null);//这样写报错 -- NullPointerException
            jedis.hset(strCache, "", "");
            jedis.expire(strCache, 3);
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
