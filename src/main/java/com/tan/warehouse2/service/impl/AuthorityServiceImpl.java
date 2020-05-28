package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Authority;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.mapper.AuthorityMapper;
import com.tan.warehouse2.service.AuthorityService;
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
public class AuthorityServiceImpl implements AuthorityService{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AuthorityMapper authorityMapper;

    private final static String CACHEALLSTR = "authority:all:info";
    private final static String CACHEALLSTRZSET = "authority:all:info:zset";
    private final static String CACHEALLLOCK = "authority:all:lock";


    @Override
    public List<Authority> getLevel1() {
        return authorityMapper.getLevel1();
    }
    @Override
    public List<Authority> getLevel2(Integer pId) {
        return authorityMapper.getLevel2(pId);
    }
    @Override
    public List<Authority> getLevel3(Integer pId) {
        return authorityMapper.getLevel3(pId);
    }

    @Override
    public int delete(List<Integer> ids) {

        //先获取所有子类id
        List<Integer> authorityChildrenId = authorityMapper.getAuthorityChildrenId(ids);
        Set<Integer> idsSet = new HashSet<>();
        idsSet.addAll(ids);
        idsSet.addAll(authorityChildrenId);
        Boolean aBoolean = authorityMapper.deleteAuthoritys(idsSet);
        System.out.println(aBoolean);//false;
        return 1;
//        Jedis jedis = null;
//        RLock lock = null;
//        boolean tryLock = false;
//        try{
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//            tryLock = lock.tryLock();
//            if (tryLock) {//成功上锁
//                try{
//                    authorityMapper.deleteAuthoritys(ids);//返回null
//                }catch (Exception e){
//                    throw e;
//                }
//                //判断是否要更新缓存
//                Boolean flag = jedis.exists(CACHEALLSTR);
//                if (flag) {//缓存中有数据
//                    for (String name : names) {
//                        String str = "authority:" + name + ":info";
//                        jedis.hdel(CACHEALLSTR, str);
//                    }
//                    for (Integer id : ids) {
//                        jedis.zremrangeByScore(CACHEALLSTRZSET, id, id);
//                    }
//                }
//                return 1;
//            }else{//有锁自旋
//                return delete(names,ids);
//            }
//        }finally {
//            destroy(jedis, lock,tryLock);
//        }

    }

    @Override
    public int update(Authority authority, String oldName) {

        String name = authority.getAuthName();
        Integer level = authority.getLevel();
        if(!name.equals(oldName)){
            List<Authority> authorities = null;
            //判断是否在该目录下已经存在oldName
            Authority a = getAuthorityByName(authority);
            if(a != null){
                return -1;
            }
        }
        int i = authorityMapper.updateByPrimaryKey(authority);
        return i;
    }

    @Override
    public PageInfo<Authority> getAuthorityLikeName(String name, Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        Example example = new Example(Authority.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andLike("name", name);

//        PageInfo<Authority> pageInfo = new PageInfo<>(authorityMapper.selectByExample(example));//[]
        PageInfo<Authority> pageInfo = new PageInfo<>(authorityMapper.getAuthorityLikeName(name));
        return pageInfo;
    }

    @Override
    public Authority getAuthorityByName(Authority authority) {
        String name = authority.getAuthName();
        Integer level = authority.getLevel();
        Integer pId = authority.getPId();
        List<Authority> authorities = null;
        //判断是否在该目录下已经存在
        if(level == 0){//一级目录
            Example example = new Example(Authority.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("level", 0);
            criteria.andEqualTo("authName", name);
            authorities = authorityMapper.selectByExample(example);
            if(authorities != null && authorities.size() > 0){
                return authorities.get(0);
            }
        }else{
            Example example = new Example(Authority.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("level", level);
            criteria.andEqualTo("authName", name);
            criteria.andEqualTo("pId", pId);
            authorities = authorityMapper.selectByExample(example);
            if(authorities != null && authorities.size() > 0){
                return authorities.get(0);
            }
        }
        return null;
    }

//    private Authority getAuthorityByName2(Jedis jedis, String name){//复用
//        //判断是否要更新缓存
//        boolean flag = jedis.exists(CACHEALLSTR);//null
////                Long hlen = jedis.hlen(CACHEALLSTR);
//        if (!flag) {
//            //没有缓存数据 -- 查询数据库
//            Example example = new Example(Authority.class);
//            Example.Criteria createCriteria1 = example.createCriteria();
//            createCriteria1.andEqualTo("name", name);
//            List<Authority> authoritys2= authorityMapper.selectByExample(example);//[]
//            if(authoritys2.size() > 0){
//                return authoritys2.get(0);
//            }
//        }else{
//            //缓存中有数据
//            String str1 = jedis.hget(CACHEALLSTR,"authority:" + name + ":info");
//            if (StringUtils.isNotBlank(str1)) {
//                return JSON.parseObject(str1, Authority.class);
//            }
//        }
//        return null;
//    }

    @Override
//    @Transactional()//事务回滚//注释了没错也插入不了
    public Authority add(Authority authority) {
        int i = authorityMapper.insertSelective(authority);
        if (i != 0){
            System.out.println(authority);
            return authority;
        }else{
            return null;
        }
    }

    //当前页 一页多少个  mysql通过limit分页的哈
//    public PageInfo<Authority> findAuthorityList(int page, int size) {
//        // 开启分页插件,放在查询语句上面 帮助生成分页语句
//        PageHelper.startPage(page, size); //底层实现原理采用改写语句   将下面的方法中的sql语句获取到然后做个拼接 limit  AOPjishu
//        List<Authority> listAuthority = getAll();
//        // 封装分页之后的数据  返回给客户端展示  PageInfo做了一些封装 作为一个类
//        PageInfo<Authority> pageInfoDemo = new PageInfo<Authority>(listAuthority);
//        //所有分页属性都可以冲pageInfoDemo拿到；
//        return pageInfoDemo;
//    }

    @Override
    public List<Authority> getAll() {

        return authorityMapper.selectAll();

//        Jedis jedis = null;
//        RLock lock = null;
//        boolean tryLock = false;
//        try{
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//            boolean isLocked = lock.isLocked();
//            if (!isLocked) {//没有被锁
//                Map<String, String> authorityMap = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
//                if (authorityMap != null && authorityMap.size() > 0) {//缓存中有数据
//                    List<Authority> authorityList = new ArrayList<>();
//                    authorityMap.forEach((k,v)->{
//                        authorityList.add(JSON.parseObject(v, Authority.class));
//                    });
//                    return authorityList;
//                }
//
//                //缓存没有数据则查询数据库，并将数据同步到缓存
//                // 设置分布式锁
//                tryLock = lock.tryLock();
//                if (tryLock) {//成功上锁
//                    //缓存中没有数据,从数据库中获取
//                    return addAllCache(jedis);
//                } else {//有锁,自旋
//                    return getAll();
//                }
//            }else{//有锁自旋
//                //添加缓存，不然容易出错 穿透
//                return getAll();
//            }
//        }finally {
//            destroy(jedis, lock, tryLock);
//        }

    }

    @Override
    public PageInfo<Authority> getAllPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Authority> authorities = authorityMapper.selectAll();
        PageInfo<Authority> pageInfoDemo = new PageInfo<Authority>(authorities);
        return pageInfoDemo;
//        Jedis jedis = null;
//        RLock lock = null;
//        boolean tryLock = false;
//        try{
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//            boolean isLocked = lock.isLocked();
//            if (!isLocked) {//没有被锁
//                Map<String, String> authorityMap = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
//                if (authorityMap != null && authorityMap.size() > 0) {//缓存中有数据
//                    if (!jedis.exists(CACHEALLSTRZSET)) {
//                        return null;
//                    }
//                    Set<String> authorityStrSet = new HashSet<>();
//                    Set<Authority> authoritySet = new HashSet<>();
////                authorityMap.forEach((k,v)->{
////                    authorityList.add(JSON.parseObject(v, Authority.class));
////                });
////                    authoritySet = jedis.zrangeByScore(CACHEALLSTR + ":zset", 0, 100, 0, pageSize);//max 不能设置为-1
//                    authorityStrSet = jedis.zrangeByScore(CACHEALLSTRZSET, "-inf", "+inf", (pageNum - 1) * pageSize, pageSize);//min max可以直接用数字字符串
//                    authorityStrSet.forEach(authorityStr -> {
//                        authoritySet.add(JSON.parseObject(authorityStr, Authority.class));
//                    });
//
//                    MyPage<Authority> myPage = new MyPage<>();
//                    myPage.setPageNum(pageNum);
//                    myPage.setPageSize(pageSize);
//                    myPage.setList(authoritySet);
//                    myPage.setTotal(jedis.zcard(CACHEALLSTRZSET));
//                    return myPage;
//                }
//
//                //缓存没有数据则查询数据库，并将数据同步到缓存
//                // 设置分布式锁
//                lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//                tryLock = lock.tryLock();
//                if (tryLock) {//成功上锁
//                    List<Authority> list = addAllCache(jedis);
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

    }

    //内部方法调用更新缓存
    private List<Authority> addAllCache(Jedis jedis){
        //缓存中没有数据,从数据库中获取
        List<Authority> authoritys = authorityMapper.selectAll();//没有值返回 []

        if (authoritys != null && authoritys.size() > 0) {
            // mysql查询结果存入redis
            for (Authority authority : authoritys) {
                String str = "authority:" + authority.getAuthName() + ":info";
                jedis.hset(CACHEALLSTR, str, JSON.toJSONString(authority));
                //用来分页
                jedis.zadd(CACHEALLSTRZSET, authority.getId(), JSON.toJSONString(authority));
            }
            return authoritys;
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
