package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.BaseAttr;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.bean.Sku;
import com.tan.warehouse2.mapper.SkuMapper;
import com.tan.warehouse2.service.SkuService;
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
public class SkuServiceImpl implements SkuService{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    SkuMapper skuMapper;

    private final static String CACHEALLSTR = "sku:info:";//name 检查 -- sku:info:id(catalog3):brandId
    private final static String CACHEALLLOCK = "sku:lock:";//加锁 -- sku:lock:id(catalog3)
    private final static String CACHEALLSTRZSET = "sku:info:zset";//分页
//    private final static String CACHECATALOGZSET = "sku:catalog:";//catalog3Id -- 分页zset
//    private final static String CACHEBRANDZSET = "sku:brand:";//brandId -- 分页zset

    @Override
    public List<Sku> getAllByWarehouseId(Integer warehouseId) {
        Example e =  new Example(Sku.class);
        Example.Criteria criteria = e.createCriteria();
        criteria.andEqualTo("warehouseId", warehouseId);
        List<Sku> skus = skuMapper.selectByExample(e);
        return skus;
    }

    @Override
    public List<Sku> getSkuByCBWId(Integer brandId, Integer catalog3Id, Integer warehouseId) {
        
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("brandId", brandId);
        criteria.andEqualTo("catalog3Id", catalog3Id);
        criteria.andEqualTo("warehouseId", warehouseId);
        return skuMapper.selectByExample(example);

    }

    @Override
    public int delete(List<Integer> ids){
        skuMapper.deleteSkus(ids);
        return 1;
    }

    @Override
    public int update(Sku sku) {
        return skuMapper.updateByPrimaryKeySelective(sku);
    }

    @Override
    public PageInfo<Sku> getSkuLikeName(String name, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        Example example = new Example(Sku.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andLike("name", name);
//        PageInfo<Sku> pageInfo = new PageInfo<>(skuMapper.selectByExample(example));//[]
        PageInfo<Sku> pageInfo = new PageInfo<>(skuMapper.getSkuLikeName(name));

        return pageInfo;
    }

    @Override
    public Sku getSkuByName(String name, Integer catalog3Id, Integer brandId) {

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
                return getSkuByName2(jedis, name, catalog3Id,brandId);
            }else{//有锁自旋
                return getSkuByName(name,catalog3Id, brandId);//自旋
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    @Override
    public PageInfo<Sku> getAllPage(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        PageInfo<Sku> pageInfo = new PageInfo<>(skuMapper.selectAll());
        return pageInfo;
//        Jedis jedis = null;
//        RLock lock = null;
//        boolean tryLock = false;
//        try{
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//            boolean isLocked = lock.isLocked();
//            if (!isLocked) {//没有被锁
//                Boolean exists = jedis.exists(CACHEALLSTRZSET);
//                if (exists) {
//                    Set<String> brandStrSet = new HashSet<>();
//                    Set<Sku> brandSet = new HashSet<>();
////                brandMap.forEach((k,v)->{
////                    brandList.add(JSON.parseObject(v, Brand.class));
////                });
////                    brandSet = jedis.zrangeByScore(CACHEALLSTR + ":zset", 0, 100, 0, pageSize);//max 不能设置为-1
//                    brandStrSet = jedis.zrangeByScore(CACHEALLSTRZSET, "-inf", "+inf", (pageNum - 1) * pageSize, pageSize);//min max可以直接用数字字符串
//                    brandStrSet.forEach(brandStr -> {
//                        brandSet.add(JSON.parseObject(brandStr, Sku.class));
//                    });
//
//                    MyPage<Sku> myPage = new MyPage<>();
//                    myPage.setPageNum(pageNum);
//                    myPage.setPageSize(pageSize);
//                    myPage.setList(brandSet);
//                    myPage.setTotal(jedis.zcard(CACHEALLSTRZSET));
//                    return myPage;
//                }else{
//                    //缓存没有数据则查询数据库，并将数据同步到缓存
//                    // 设置分布式锁
//                    lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//                    tryLock = lock.tryLock();
//                    if (tryLock) {//成功上锁
//                        List<Sku> list = addAllCache(jedis);
//                        if (list == null) {//数据库数据为空
//                            return null;
//                        }
//                        return getAllPage(pageNum, pageSize);//自旋重新获取数据
//                    } else {//有锁,自旋
//                        return getAllPage(pageNum, pageSize);
//                    }
//                }
//
//
//
//            }else{//有锁自旋
//                //添加缓存，不然容易出错 穿透
//                return getAllPage(pageNum, pageSize);
//            }
//
//        }finally {
//            destroy(jedis, lock, tryLock);
//        }

    }

    private Sku getSkuByName2(Jedis jedis, String name, Integer catalog3Id, Integer brandId){//复用
        //判断是否要更新缓存
        String strKey = CACHEALLSTR + catalog3Id + ":" + brandId;
        boolean flag = jedis.exists(CACHEALLSTRZSET);//null
//                Long hlen = jedis.hlen(CACHEALLSTR);
        if (!flag) {
            //没有缓存数据 -- 查询数据库
            Example example = new Example(Sku.class);
            Example.Criteria createCriteria1 = example.createCriteria();
            createCriteria1.andEqualTo("name", name);
            createCriteria1.andEqualTo("catalog3Id", catalog3Id);
            createCriteria1.andEqualTo("brandId", brandId);
            List<Sku> skus2= skuMapper.selectByExample(example);//[]
            if(skus2.size() > 0){
                return skus2.get(0);
            }
        }else{
            //缓存中有数据
            String str1 = jedis.hget(strKey,name);
            if (StringUtils.isNotBlank(str1)) {
                return JSON.parseObject(str1, Sku.class);
            }
        }
        return null;
    }

    @Override
    public Sku getSkuById(Integer id) {
        return skuMapper.selectByPrimaryKey(id);
    }

    @Override
    public int addNum(Integer id, Integer num) {
        skuMapper.addNum(id, num);
        return 1;
    }

    @Override
    public void minusNum(Integer id, Integer num) {
        skuMapper.minusNum(id, num);
    }

    @Override
    public List<Sku> checkSkuByAll(Sku sku) {
        //利用 仓库 - warehouseId 商品 - spuId 属性规格 -- attrId valueStr 和 单位 - unit 查重
        //先由仓库 商品 单位 查重 有继续由spuId 和 warehouseId 调出 attrs 再比较 attrs
        List<Sku> list = skuMapper.findSkuBySku(sku);

        if(list != null && list.size() > 0){
            return list;
        }
        return null;
    }

    @Override
//    @Transactional()//事务回滚//注释了没错也插入不了
    public int add(Sku sku) {

        int i = skuMapper.insert(sku);
        System.out.println("insert");
        System.out.println(sku);
        if(i != 0){
//            skuMapper.insertAttrValue();
            return i;
        }
        return 0;
//        Jedis jedis = null;
//        RLock lock = null;
//        RLock lock2 = null;
//        boolean tryLock = false;
//        boolean tryLock2 = false;
//        try{
//
//            Integer catalog3Id = sku.getCatalog3Id();
//            Integer brandId = sku.getBrandId();
//            String str1 = catalog3Id + ":" + brandId;
//            String strKey = CACHEALLSTR + str1;
//
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK + str1);// 声明锁
//            lock2 = redissonClient.getLock(CACHEALLLOCK + str1);// 声明锁
//            tryLock = lock.tryLock();
//            tryLock2 = lock2.tryLock();
//            if (tryLock && tryLock2) {//成功上锁
//                //防止快速多次点击，某部分跳过name检查，所以再次检查是否已经存在数据
//                Sku skuByName2 = getSkuByName2(jedis, sku.getName(), catalog3Id, brandId);
//                if (skuByName2 == null) {//确认没有数据再添加
//                    int insert = skuMapper.insert(sku);
//                    List<BaseAttr> baseAttrs = sku.getBaseAttrs();
//                    System.out.println("add"  + sku);
//                    if(baseAttrs != null && baseAttrs.size() > 0){
//                        List<Integer> list = new ArrayList<>();
//                        baseAttrs.forEach(item -> {
//                            list.add(item.getId());
//                        });
//                        skuMapper.insertAttr(list, sku.getId());
//                    }
//
//                    if (insert != 0){
//                        //判断是否要更新缓存
//                        Boolean flag = jedis.exists(CACHEALLSTRZSET);
//                        if (flag) {//缓存中有数据
//                            String str = sku.getName();
//                            String jsonStr = JSON.toJSONString(sku);
//                            jedis.hset(strKey, str,jsonStr);
//                            jedis.zadd(CACHEALLSTRZSET, sku.getId(), jsonStr);
//                        }else{
//                            //添加缓存，不然容易出错 穿透
//                            addAllCache(jedis);
//                        }
//                        return insert;
//                    }
//
//                    return 0;//没有插入
//                }
//                return -1;//已经存在
//            }else{//有锁自旋
//                //添加缓存，不然容易出错 穿透
//                return add(sku);
//            }
//        }finally {
//            if(tryLock2 && lock2 != null){
//                lock2.unlock();
//            }
//            destroy(jedis, lock, tryLock);
//        }

    }

    @Override
    public List<Sku> getAll(Integer catalog3Id, Integer brandId) {

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
                    Map<String, String> skuMap = jedis.hgetAll(CACHEALLSTR + str);//不存在返回 {}
                    if (skuMap != null && skuMap.size() > 0) {//缓存中有数据
                        List<Sku> skuList = new ArrayList<>();
                        skuMap.forEach((k, v) -> {
                            skuList.add(JSON.parseObject(v, Sku.class));
                        });
                        return skuList;
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
    private List<Sku> addAllCache(Jedis jedis){
        //缓存中没有数据,从数据库中获取
//        Example example = new Example(Sku.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andEqualTo("catalog3Id", catalog3Id);
//        criteria.andEqualTo("brandId", brandId);
//        List<Sku> skus = skuMapper.selectByExample(example);
        List<Sku> skus = skuMapper.selectAll();

        if (skus != null && skus.size() > 0) {

            for (Sku s: skus){
                List<BaseAttr> attrs = skuMapper.getAttr(s.getId());
                s.setBaseAttrs(attrs);

                Integer catalog3Id = s.getCatalog3Id();
                Integer brandId = s.getBrandId();
                String strKey = CACHEALLSTR + catalog3Id + ":" + brandId;
                String str = s.getName();

                String jsonStr = JSON.toJSONString(s);
                jedis.hset(strKey, str, jsonStr);
                jedis.zadd(CACHEALLSTRZSET, s.getId(), jsonStr);
            }
            return skus;
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
