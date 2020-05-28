package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Catalog;
import com.tan.warehouse2.bean.Catalog2;
import com.tan.warehouse2.bean.Catalog3;
import com.tan.warehouse2.mapper.Catalog2Mapper;
import com.tan.warehouse2.service.Catalog2Service;
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
public class Catalog2ServiceImpl implements Catalog2Service{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    Catalog2Mapper catalog2Mapper;

    @Autowired
    Catalog3Service catalog3Service;

    private final static String CACHEALLSTR = "catalog2:info:";//name 检查
    private final static String CACHEALLSTRZSET = "catalog2:info:zset";//分页
    private final static String CACHEALLLOCK = "catalog2:lock:";//加锁
    private final static String CACHELISTSTR = "catalog1:list:";//树 -- 三级分类

    @Override
    public int delete(List<String> names, List<Integer> ids, List<String> ztreeIds, Integer catalog1Id) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog1Id);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                try{
                    catalog2Mapper.deleteCatalogs(ids);//返回null
                }catch (Exception e){
                    throw e;
                }
                String strAll = CACHEALLSTR + catalog1Id;
                String strList = CACHELISTSTR + catalog1Id;
                //判断是否要更新缓存
                Boolean flag = jedis.exists(strAll);
                if (flag) {//缓存中有数据
                    for (String name : names) {
                        String str = "catalog2:" + name + ":info";
                        jedis.hdel(strAll, str);
                    }
                    for(String ztreeName : ztreeIds){
                        jedis.hdel(strList, ztreeName);
                    }

                    for(Integer id: ids){
                        //删除分页数据
                        jedis.zremrangeByScore(CACHEALLSTRZSET, id, id);

                        //删除子类数据
                        Map<String, String> map = jedis.hgetAll("catalog3:info:" + id);

                        List<String> names3 = new ArrayList<>();
                        List<String> ztreeIds3 = new ArrayList<>();
                        List<Integer> ids3 = new ArrayList<>();
//                        System.out.println(map);
                        if (map != null && map.size() > 0){

                            for (String s : map.values()){
                                Catalog3 c3 = JSON.parseObject(s, Catalog3.class);

                                names3.add(c3.getName());
                                ztreeIds3.add(c3.getZtreeId());
                                ids3.add(c3.getId());

//                                    jedis.hdel(strList, c3.getZtreeId());
//                                    //分页数据
//                                    jedis.zremrangeByScore("catalog3:all:info:zset", c3.getId(), c3.getId());
                            }
                            //删除整个key
//                                jedis.del(strAll);
                            //调用3级分类删除
                            catalog3Service.delete(names3,ids3,ztreeIds3,catalog1Id,id);
                        }
                    }

                }
                return 1;
            }else{//有锁自旋
                return delete(names,ids, ztreeIds, catalog1Id);
            }
        }finally {
            destroy(jedis, lock,tryLock);
        }

    }

    @Override
    public int update(Catalog2 catalog2, String oldName) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();

            Integer catalog1Id = catalog2.getCatalog1Id();

            lock = redissonClient.getLock(CACHEALLLOCK + catalog1Id);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                int i = catalog2Mapper.updateByPrimaryKeySelective(catalog2);//没有更新catalog2 自行获取
//                System.out.println(i);
                if (i != 0){//更新成功
                    String s2 = CACHEALLSTR + catalog1Id;
                    String strTemp = jedis.hget(s2, "catalog2:" + oldName + ":info");
                    Catalog2 c = JSON.parseObject(strTemp, Catalog2.class);

                    //判断是否要更新缓存
                    Boolean flag = jedis.exists(s2);
                    if (flag) {//缓存中有数据
                        //缓存中有数据，更新缓存

                        c.setName(catalog2.getName());
                        String s = JSON.toJSONString(c);

                        //删除原来的
                        jedis.hdel(s2, "catalog2:" + oldName + ":info", s);
                        jedis.hset(s2, "catalog2:" + c.getName() + ":info", s);
                        jedis.hdel(CACHELISTSTR + catalog1Id, c.getZtreeId());
                        jedis.hset(CACHELISTSTR + catalog1Id, c.getZtreeId(), s);
                        //先删除再添加
                        jedis.zremrangeByScore(CACHEALLSTRZSET, c.getId(), c.getId());
                        jedis.zadd(CACHEALLSTRZSET, c.getId(), s);
                    }
                    return i;
                }
                return 0;
            }else{//有锁自旋
                return update(catalog2,oldName);
            }
        }finally {
            destroy(jedis,lock,tryLock);
        }

    }

    @Override
    public PageInfo<Catalog2> getCatalog2LikeName(String name, Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        Example example = new Example(Catalog2.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andLike("name", name);
//        PageInfo<Catalog2> pageInfo = new PageInfo<>(catalog2Mapper.selectByExample(example));//[]
        PageInfo<Catalog2> pageInfo = new PageInfo<>(catalog2Mapper.getCatalog2LikeName(name));
        return pageInfo;
    }

    @Override
    public Catalog2 getCatalog2ByName(String name, Integer catalog1Id) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{

//            jedis = redisUtil.getJedis();
//            jedis.select(1);
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog1Id);// 声明锁
            boolean locked = lock.isLocked();
            if (!locked) {//没有被锁
                return getCatalog2ByName2(jedis, name, catalog1Id);
            }else{//有锁自旋
                return getCatalog2ByName(name, catalog1Id);//自旋
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    private Catalog2 getCatalog2ByName2(Jedis jedis, String name, Integer catalog1Id){//复用
        //判断是否要更新缓存
        boolean flag = jedis.exists(CACHEALLSTR + catalog1Id);//null
//                Long hlen = jedis.hlen(CACHEALLSTR);
        if (!flag) {
            //没有缓存数据 -- 查询数据库
            Example example = new Example(Catalog2.class);
            Example.Criteria createCriteria1 = example.createCriteria();
            createCriteria1.andEqualTo("name", name);
            createCriteria1.andEqualTo("catalog1Id", catalog1Id);
            List<Catalog2> catalog2s2= catalog2Mapper.selectByExample(example);//[]
            if(catalog2s2.size() > 0){
                return catalog2s2.get(0);
            }
        }else{
            //缓存中有数据
            String str1 = jedis.hget(CACHEALLSTR + catalog1Id,"catalog2:" + name + ":info");
            if (StringUtils.isNotBlank(str1)) {
                return JSON.parseObject(str1, Catalog2.class);
            }
        }
        return null;
    }

    @Override
//    @Transactional()//事务回滚//注释了没错也插入不了
    public int add(Catalog2 catalog2) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog2.getCatalog1Id());// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                //防止快速多次点击，某部分跳过name检查，所以再次检查是否已经存在数据
                Catalog2 catalog2ByName2 = getCatalog2ByName2(jedis, catalog2.getName(), catalog2.getCatalog1Id());
                if (catalog2ByName2 == null) {//确认没有数据再添加
                    int insert = catalog2Mapper.insert(catalog2);
                    catalog2.setZtreeId("catalog2" + catalog2.getId());
                    catalog2Mapper.updateByPrimaryKey(catalog2);
                    System.out.println("catalog2" + catalog2);
                    if (insert != 0){
                        //判断是否要更新缓存
                        Boolean flag = jedis.exists(CACHEALLSTR + catalog2.getCatalog1Id());
                        if (flag) {//缓存中有数据
                            String str = "catalog2:" + catalog2.getName() + ":info";
                            String jsonStr = JSON.toJSONString(catalog2);
                            jedis.hset(CACHEALLSTR + catalog2.getCatalog1Id(), str,jsonStr);
                            jedis.zadd(CACHEALLSTRZSET, catalog2.getId(), jsonStr);
                            jedis.hset(CACHELISTSTR + catalog2.getCatalog1Id(), catalog2.getZtreeId(), jsonStr);
                        }else{
                            //添加缓存，不然容易出错 穿透
                            addAllCache(jedis,catalog2.getCatalog1Id());
                        }
                        return insert;
                    }

                    return 0;//没有插入
                }
                return -1;//已经存在
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return add(catalog2);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    //当前页 一页多少个  mysql通过limit分页的哈
//    public PageInfo<Catalog2> findCatalog2List(int page, int size) {
//        // 开启分页插件,放在查询语句上面 帮助生成分页语句
//        PageHelper.startPage(page, size); //底层实现原理采用改写语句   将下面的方法中的sql语句获取到然后做个拼接 limit  AOPjishu
//        List<Catalog2> listCatalog2 = getAll();
//        // 封装分页之后的数据  返回给客户端展示  PageInfo做了一些封装 作为一个类
//        PageInfo<Catalog2> pageInfoDemo = new PageInfo<Catalog2>(listCatalog2);
//        //所有分页属性都可以冲pageInfoDemo拿到；
//        return pageInfoDemo;
//    }

    @Override
    public List<Catalog2> getAll(Integer catalog1Id) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog1Id);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> catalog3Map = jedis.hgetAll(CACHEALLSTR + catalog1Id);//不存在返回 {}
                if (catalog3Map != null && catalog3Map.size() > 0) {//缓存中有数据
                    List<Catalog2> catalog3List = new ArrayList<>();
                    catalog3Map.forEach((k,v)->{
                        catalog3List.add(JSON.parseObject(v, Catalog2.class));
                    });
                    return catalog3List;
                }

                //缓存没有数据则查询数据库，并将数据同步到缓存
                // 设置分布式锁
                tryLock = lock.tryLock();
                if (tryLock) {//成功上锁
                    //缓存中没有数据,从数据库中获取
                    return addAllCache(jedis, catalog1Id);
                } else {//有锁,自旋
                    return getAll(catalog1Id);
                }
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return getAll(catalog1Id);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }
    }

    @Override
    public List<Catalog> getAll2(Integer catalog1Id) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK + catalog1Id);// 声明锁
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
                    addAllCache(jedis, catalog1Id);
                    return getCatalog(catalog1Id, jedis);
                } else {//有锁,自旋
                    return getAll2(catalog1Id);
                }
            }else{//有锁自旋
                //添加缓存，不然容易出错 穿透
                return getAll2(catalog1Id);
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
//    public MyPage<Catalog2> getAllPage(Integer pageNum, Integer pageSize) {
//
//        Jedis jedis = null;
//        RLock lock = null;
//        boolean tryLock = false;
//        try{
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//            boolean isLocked = lock.isLocked();
//            if (!isLocked) {//没有被锁
//                Map<String, String> catalog2Map = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
//                if (catalog2Map != null && catalog2Map.size() > 0) {//缓存中有数据
//                    if (!jedis.exists(CACHEALLSTRZSET)) {
//                        return null;
//                    }
//                    Set<String> catalog2StrSet = new HashSet<>();
//                    Set<Catalog2> catalog2Set = new HashSet<>();
////                catalog2Map.forEach((k,v)->{
////                    catalog2List.add(JSON.parseObject(v, Catalog2.class));
////                });
////                    catalog2Set = jedis.zrangeByScore(CACHEALLSTR + ":zset", 0, 100, 0, pageSize);//max 不能设置为-1
//                    catalog2StrSet = jedis.zrangeByScore(CACHEALLSTRZSET, "-inf", "+inf", (pageNum - 1) * pageSize, pageSize);//min max可以直接用数字字符串
//                    catalog2StrSet.forEach(catalog2Str -> {
//                        catalog2Set.add(JSON.parseObject(catalog2Str, Catalog2.class));
//                    });
//
//                    MyPage<Catalog2> myPage = new MyPage<>();
//                    myPage.setPageNum(pageNum);
//                    myPage.setPageSize(pageSize);
//                    myPage.setList(catalog2Set);
//                    myPage.setTotal(jedis.zcard(CACHEALLSTRZSET));
//                    return myPage;
//                }
//
//                //缓存没有数据则查询数据库，并将数据同步到缓存
//                // 设置分布式锁
//                lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//                tryLock = lock.tryLock();
//                if (tryLock) {//成功上锁
//                    List<Catalog2> list = addAllCache(jedis);
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
    private List<Catalog2> addAllCache(Jedis jedis, Integer catalog1Id){
        //缓存中没有数据,从数据库中获取
//        List<Catalog2> catalog2s = catalog2Mapper.selectAll();//没有值返回 []

        //没有缓存数据 -- 查询数据库
        Example example = new Example(Catalog2.class);
        Example.Criteria createCriteria1 = example.createCriteria();
        createCriteria1.andEqualTo("catalog1Id", catalog1Id);
        List<Catalog2> catalog2s= catalog2Mapper.selectByExample(example);//[]

        if (catalog2s != null && catalog2s.size() > 0) {
            for (Catalog2 catalog2 : catalog2s) {
                String str = "catalog2:" + catalog2.getName() + ":info";
                String jsonStr = JSON.toJSONString(catalog2);
                jedis.hset(CACHEALLSTR + catalog1Id, str, jsonStr);
                //用来分页
                jedis.zadd(CACHEALLSTRZSET, catalog2.getId(), jsonStr);
                jedis.hset(CACHELISTSTR + catalog1Id, catalog2.getZtreeId(), jsonStr);
            }
            return catalog2s;
        }else {
            // 数据库没有
            // 为了防止缓存穿透将，null或者空字符串值设置给redis
//                    jedis.hmset(CACHEALLSTR, null);//这样写报错 -- NullPointerException
            jedis.hset(CACHEALLSTR + catalog1Id, "", "");
            jedis.expire(CACHEALLSTR + catalog1Id, 3);
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
