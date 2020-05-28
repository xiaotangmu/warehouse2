package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Brand;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.mapper.BrandMapper;
import com.tan.warehouse2.service.BrandService;
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
public class BrandServiceImpl implements BrandService{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    BrandMapper brandMapper;

//    Jedis jedis = null;//直接写出来可能造成，多个访问，上个jedis未用完，就被重新创建了，或者关闭后，因为对象还在，没有重新创建，而报错
//    RLock lock = null;
//    boolean tryLock = false;
//    Map<String, String> brandMap = null;
//    List<Brand> brands = null;
    
    private final static String CACHEALLSTR = "brand:all:info";
    private final static String CACHEALLSTRZSET = "brand:all:info:zset";
    private final static String CACHEALLLOCK = "brand:all:lock";


    @Override
    public int delete(List<String> names, List<Integer> ids) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                try{
                    brandMapper.deleteBrands(ids);//返回null
                }catch (Exception e){
                    throw e;
                }
                //判断是否要更新缓存
                Boolean flag = jedis.exists(CACHEALLSTR);
                if (flag) {//缓存中有数据
                    for (String name : names) {
                        String str = name;
                        jedis.hdel(CACHEALLSTR, str);
                    }
                    for (Integer id : ids) {
                        jedis.zremrangeByScore(CACHEALLSTRZSET, id, id);
                    }
                }
                return 1;
            }else{//有锁自旋
                return delete(names,ids);
            }
        }finally {
            destroy(jedis, lock,tryLock);
        }

    }

    @Override
    public int update(Brand brand) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                int i = brandMapper.updateByPrimaryKeySelective(brand);
                if (i != 0){//更新成功
                    //判断是否要更新缓存
                    Boolean flag = jedis.exists(CACHEALLSTR);
                    if (flag) {//缓存中有数据
                        //缓存中有数据，更新缓存
                        String s = JSON.toJSONString(brand);
                        //先删除再添加
                        jedis.hset(CACHEALLSTR, brand.getName(), s);
//                        jedis.zadd(CACHEALLSTRZSET, brand.getId(), s);//直接添加不会覆盖

                        jedis.zremrangeByScore(CACHEALLSTRZSET, brand.getId(), brand.getId());
                        jedis.zadd(CACHEALLSTRZSET, brand.getId(), s);
                    }
                    return i;
                }
                return 0;
            }else{//有锁自旋
                return update(brand);
            }
        }finally {
            destroy(jedis,lock,tryLock);
        }

    }

    @Override
    public PageInfo<Brand> getBrandLikeName(String name, Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        Example example = new Example(Brand.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andLike("name", name);
//        PageInfo<Brand> pageInfo = new PageInfo<>(brandMapper.selectByExample(example));//[]
        PageInfo<Brand> pageInfo = new PageInfo<>(brandMapper.getBrandLikeName(name));
        return pageInfo;
    }

    @Override
    public Brand getBrandByName(String name) {

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
                return getBrandByName2(jedis, name);
            }else{//有锁自旋
                return getBrandByName(name);//自旋
            }
        }finally {
//            destroy();
//            if (tryLock) {
//                lock.unlock();//解锁
//            }
//            if (jedis != null) {
//                jedis.close();
//            }
            destroy(jedis, lock, tryLock);
        }

    }

    private Brand getBrandByName2(Jedis jedis, String name){//复用
        //判断是否要更新缓存
        boolean flag = jedis.exists(CACHEALLSTR);//null
//                Long hlen = jedis.hlen(CACHEALLSTR);
        if (!flag) {
            //没有缓存数据 -- 查询数据库
            Example example = new Example(Brand.class);
            Example.Criteria createCriteria1 = example.createCriteria();
            createCriteria1.andEqualTo("name", name);
            List<Brand> brands2= brandMapper.selectByExample(example);//[]
            if(brands2.size() > 0){
                return brands2.get(0);
            }
        }else{
            //缓存中有数据
            String str1 = jedis.hget(CACHEALLSTR,name);
            if (StringUtils.isNotBlank(str1)) {
                return JSON.parseObject(str1, Brand.class);
            }
        }
        return null;
    }

    @Override
//    @Transactional()//事务回滚//注释了没错也插入不了
    public int add(Brand brand) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                //防止快速多次点击，某部分跳过name检查，所以再次检查是否已经存在数据
                Brand brandByName2 = getBrandByName2(jedis, brand.getName());
                if (brandByName2 == null) {//确认没有数据再添加
                    int insert = brandMapper.insert(brand);
                    System.out.println("brand" + brand);
                    if (insert != 0){
                        //判断是否要更新缓存
                        Boolean flag = jedis.exists(CACHEALLSTR);
                        if (flag) {//缓存中有数据
                            String str = brand.getName();
                            jedis.hset(CACHEALLSTR, str,JSON.toJSONString(brand));
                            jedis.zadd(CACHEALLSTRZSET, brand.getId(), JSON.toJSONString(brand));
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
                return add(brand);
            }
        }finally {
//            destroy();
//            if (tryLock) {
//                lock.unlock();//解锁
//            }
//            if (jedis != null) {
//                jedis.close();
//            }

            destroy(jedis, lock, tryLock);
        }

    }

    //当前页 一页多少个  mysql通过limit分页的哈
    public PageInfo<Brand> findBrandList(int page, int size) {
        // 开启分页插件,放在查询语句上面 帮助生成分页语句
        PageHelper.startPage(page, size); //底层实现原理采用改写语句   将下面的方法中的sql语句获取到然后做个拼接 limit  AOPjishu
        List<Brand> listBrand = getAll();
        // 封装分页之后的数据  返回给客户端展示  PageInfo做了一些封装 作为一个类
        PageInfo<Brand> pageInfoDemo = new PageInfo<Brand>(listBrand);
        //所有分页属性都可以冲pageInfoDemo拿到；
        return pageInfoDemo;
    }

    @Override
    public List<Brand> getAll() {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> brandMap = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
                if (brandMap != null && brandMap.size() > 0) {//缓存中有数据
                    List<Brand> brandList = new ArrayList<>();
                    brandMap.forEach((k,v)->{
                        brandList.add(JSON.parseObject(v, Brand.class));
                    });
                    return brandList;
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
//            destroy();
//            if (tryLock) {
//                lock.unlock();//解锁
//            }
//            if (jedis != null) {
//                jedis.close();
//            }
            destroy(jedis, lock, tryLock);
        }

    }

    @Override
    public MyPage<Brand> getAllPage(Integer pageNum, Integer pageSize) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> brandMap = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
                if (brandMap != null && brandMap.size() > 0) {//缓存中有数据
                    if (!jedis.exists(CACHEALLSTRZSET)) {
                        return null;
                    }
                    Set<String> brandStrSet = new HashSet<>();
                    Set<Brand> brandSet = new HashSet<>();
//                brandMap.forEach((k,v)->{
//                    brandList.add(JSON.parseObject(v, Brand.class));
//                });
//                    brandSet = jedis.zrangeByScore(CACHEALLSTR + ":zset", 0, 100, 0, pageSize);//max 不能设置为-1
                    brandStrSet = jedis.zrangeByScore(CACHEALLSTRZSET, "-inf", "+inf", (pageNum - 1) * pageSize, pageSize);//min max可以直接用数字字符串
                    brandStrSet.forEach(brandStr -> {
                        brandSet.add(JSON.parseObject(brandStr, Brand.class));
                    });

                    MyPage<Brand> myPage = new MyPage<>();
                    myPage.setPageNum(pageNum);
                    myPage.setPageSize(pageSize);
                    myPage.setList(brandSet);
                    myPage.setTotal(jedis.zcard(CACHEALLSTRZSET));
                    return myPage;
                }

                //缓存没有数据则查询数据库，并将数据同步到缓存
                // 设置分布式锁
                lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
                tryLock = lock.tryLock();
                if (tryLock) {//成功上锁
                    List<Brand> list = addAllCache(jedis);
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
//            destroy();
//            if (tryLock) {
//                lock.unlock();//解锁
//            }
//            if (jedis != null) {
//                jedis.close();
//            }

            destroy(jedis, lock, tryLock);
        }

    }

    //内部方法调用更新缓存
    private List<Brand> addAllCache(Jedis jedis){
        //缓存中没有数据,从数据库中获取
        List<Brand> brands = brandMapper.selectAll();//没有值返回 []

        if (brands != null && brands.size() > 0) {
            // mysql查询结果存入redis
//                    brandMap = new HashMap<>();
//                    brands.forEach(item -> {
////                        brandMap.put(item.getName(), JSON.toJSONString(item));
//                    });
//                    jedis.hmset(CACHEALLSTR, brandMap);

            for (Brand brand : brands) {
                String str = brand.getName();
                jedis.hset(CACHEALLSTR, str, JSON.toJSONString(brand));
                //用来分页
                jedis.zadd(CACHEALLSTRZSET, brand.getId(), JSON.toJSONString(brand));
            }
            return brands;
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

//    private void initCache(){//不能用共享数据，多个访问，造成jedis 对象丢失，而不能再继续操作
//        if (jedis != null) {//不能这样写，就算jedis 关闭了，该对象依然存在，而造成未重新创建，该对象又不能使用
//          //....
//        }
//        if (jedis == null || !jedis.isConnected()) {//这样也不行
//
//        }
//        jedis = redisUtil.getJedis();
//        jedis.select(1);//选择数据库1
//    }
//
//    private void destroy(){
//        if (tryLock) {
//            lock.unlock();// 解锁
//            tryLock = false;
//        }
//        if(jedis != null){
//            jedis.close();
//        }
//    }
}
