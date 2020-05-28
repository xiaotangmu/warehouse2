package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.bean.Supplier;
import com.tan.warehouse2.mapper.SupplierMapper;
import com.tan.warehouse2.service.SupplierService;
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
public class SupplierServiceImpl implements SupplierService{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    SupplierMapper supplierMapper;

    private final static String CACHEALLSTR = "supplier:all:info";
    private final static String CACHEALLSTRZSET = "supplier:all:info:zset";
    private final static String CACHEALLLOCK = "supplier:all:lock";

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
                    supplierMapper.deleteSuppliers(ids);//返回null
                }catch (Exception e){
                    throw e;
                }
                //判断是否要更新缓存
                Boolean flag = jedis.exists(CACHEALLSTR);
                if (flag) {//缓存中有数据
                    for (String name : names) {
                        String str = "supplier:" + name + ":info";
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
    public int update(Supplier supplier) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                int i = supplierMapper.updateByPrimaryKeySelective(supplier);
                if (i != 0){//更新成功
                    //判断是否要更新缓存
                    Boolean flag = jedis.exists(CACHEALLSTR);
                    if (flag) {//缓存中有数据
                        //缓存中有数据，更新缓存
                        String s = JSON.toJSONString(supplier);
                        jedis.hset(CACHEALLSTR, "supplier:" + supplier.getName() + ":info", s);
//                        jedis.zadd(CACHEALLSTRZSET, supplier.getId(), s);//直接添加不会覆盖
                        //先删除再添加
                        jedis.zremrangeByScore(CACHEALLSTRZSET, supplier.getId(), supplier.getId());
                        jedis.zadd(CACHEALLSTRZSET, supplier.getId(), s);
                    }
                    return i;
                }
                return 0;
            }else{//有锁自旋
                return update(supplier);
            }
        }finally {
            destroy(jedis,lock,tryLock);
        }

    }

    @Override
    public PageInfo<Supplier> getSupplierLikeName(String name, Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        Example example = new Example(Supplier.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andLike("name", name);
//        PageInfo<Supplier> pageInfo = new PageInfo<>(supplierMapper.selectByExample(example));//[]
        PageInfo<Supplier> pageInfo = new PageInfo<>(supplierMapper.getSupplierLikeName(name));
        return pageInfo;
    }

    @Override
    public Supplier getSupplierByName(String name) {

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
                return getSupplierByName2(jedis, name);
            }else{//有锁自旋
                return getSupplierByName(name);//自旋
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    private Supplier getSupplierByName2(Jedis jedis, String name){//复用
        //判断是否要更新缓存
        boolean flag = jedis.exists(CACHEALLSTR);//null
//                Long hlen = jedis.hlen(CACHEALLSTR);
        if (!flag) {
            //没有缓存数据 -- 查询数据库
            Example example = new Example(Supplier.class);
            Example.Criteria createCriteria1 = example.createCriteria();
            createCriteria1.andEqualTo("name", name);
            List<Supplier> suppliers2= supplierMapper.selectByExample(example);//[]
            if(suppliers2.size() > 0){
                return suppliers2.get(0);
            }
        }else{
            //缓存中有数据
            String str1 = jedis.hget(CACHEALLSTR,"supplier:" + name + ":info");
            if (StringUtils.isNotBlank(str1)) {
                return JSON.parseObject(str1, Supplier.class);
            }
        }
        return null;
    }

    @Override
//    @Transactional()//事务回滚//注释了没错也插入不了
    public int add(Supplier supplier) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                //防止快速多次点击，某部分跳过name检查，所以再次检查是否已经存在数据
                Supplier supplierByName2 = getSupplierByName2(jedis, supplier.getName());
                if (supplierByName2 == null) {//确认没有数据再添加
                    int insert = supplierMapper.insert(supplier);
                    System.out.println("supplier" + supplier);
                    if (insert != 0){
                        //判断是否要更新缓存
                        Boolean flag = jedis.exists(CACHEALLSTR);
                        if (flag) {//缓存中有数据
                            String str = "supplier:" + supplier.getName() + ":info";
                            jedis.hset(CACHEALLSTR, str,JSON.toJSONString(supplier));
                            jedis.zadd(CACHEALLSTRZSET, supplier.getId(), JSON.toJSONString(supplier));
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
                return add(supplier);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    //当前页 一页多少个  mysql通过limit分页的哈
    public PageInfo<Supplier> findSupplierList(int page, int size) {
        // 开启分页插件,放在查询语句上面 帮助生成分页语句
        PageHelper.startPage(page, size); //底层实现原理采用改写语句   将下面的方法中的sql语句获取到然后做个拼接 limit  AOPjishu
        List<Supplier> listSupplier = getAll();
        // 封装分页之后的数据  返回给客户端展示  PageInfo做了一些封装 作为一个类
        PageInfo<Supplier> pageInfoDemo = new PageInfo<Supplier>(listSupplier);
        //所有分页属性都可以冲pageInfoDemo拿到；
        return pageInfoDemo;
    }

    @Override
    public List<Supplier> getAll() {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> supplierMap = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
                if (supplierMap != null && supplierMap.size() > 0) {//缓存中有数据
                    List<Supplier> supplierList = new ArrayList<>();
                    supplierMap.forEach((k,v)->{
                        supplierList.add(JSON.parseObject(v, Supplier.class));
                    });
                    return supplierList;
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
    public MyPage<Supplier> getAllPage(Integer pageNum, Integer pageSize) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> supplierMap = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
                if (supplierMap != null && supplierMap.size() > 0) {//缓存中有数据
                    if (!jedis.exists(CACHEALLSTRZSET)) {
                        return null;
                    }
                    Set<String> supplierStrSet = new HashSet<>();
                    Set<Supplier> supplierSet = new HashSet<>();
//                supplierMap.forEach((k,v)->{
//                    supplierList.add(JSON.parseObject(v, Supplier.class));
//                });
//                    supplierSet = jedis.zrangeByScore(CACHEALLSTR + ":zset", 0, 100, 0, pageSize);//max 不能设置为-1
                    supplierStrSet = jedis.zrangeByScore(CACHEALLSTRZSET, "-inf", "+inf", (pageNum - 1) * pageSize, pageSize);//min max可以直接用数字字符串
                    supplierStrSet.forEach(supplierStr -> {
                        supplierSet.add(JSON.parseObject(supplierStr, Supplier.class));
                    });

                    MyPage<Supplier> myPage = new MyPage<>();
                    myPage.setPageNum(pageNum);
                    myPage.setPageSize(pageSize);
                    myPage.setList(supplierSet);
                    myPage.setTotal(jedis.zcard(CACHEALLSTRZSET));
                    return myPage;
                }

                //缓存没有数据则查询数据库，并将数据同步到缓存
                // 设置分布式锁
                lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
                tryLock = lock.tryLock();
                if (tryLock) {//成功上锁
                    List<Supplier> list = addAllCache(jedis);
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
    private List<Supplier> addAllCache(Jedis jedis){
        //缓存中没有数据,从数据库中获取
        List<Supplier> suppliers = supplierMapper.selectAll();//没有值返回 []

        if (suppliers != null && suppliers.size() > 0) {
            for (Supplier supplier : suppliers) {
                String str = "supplier:" + supplier.getName() + ":info";
                jedis.hset(CACHEALLSTR, str, JSON.toJSONString(supplier));
                //用来分页
                jedis.zadd(CACHEALLSTRZSET, supplier.getId(), JSON.toJSONString(supplier));
            }
            return suppliers;
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
