package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Client;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.mapper.ClientMapper;
import com.tan.warehouse2.service.ClientService;
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
public class ClientServiceImpl implements ClientService{

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    ClientMapper clientMapper;

    private final static String CACHEALLSTR = "client:all:info";
    private final static String CACHEALLSTRZSET = "client:all:info:zset";
    private final static String CACHEALLLOCK = "client:all:lock";


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
                    clientMapper.deleteClients(ids);//返回null
                }catch (Exception e){
                    throw e;
                }
                //判断是否要更新缓存
                Boolean flag = jedis.exists(CACHEALLSTR);
                if (flag) {//缓存中有数据
                    for (String name : names) {
                        String str = "client:" + name + ":info";
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
    public int update(Client client) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                int i = clientMapper.updateByPrimaryKeySelective(client);
                if (i != 0){//更新成功
                    //判断是否要更新缓存
                    Boolean flag = jedis.exists(CACHEALLSTR);
                    if (flag) {//缓存中有数据
                        //缓存中有数据，更新缓存
                        String s = JSON.toJSONString(client);
                        jedis.hset(CACHEALLSTR, "client:" + client.getName() + ":info", s);
//                        jedis.zadd(CACHEALLSTRZSET, client.getId(), s);//直接添加不会覆盖
                        //先删除再添加
                        jedis.zremrangeByScore(CACHEALLSTRZSET, client.getId(), client.getId());
                        jedis.zadd(CACHEALLSTRZSET, client.getId(), s);
                    }
                    return i;
                }
                return 0;
            }else{//有锁自旋
                return update(client);
            }
        }finally {
            destroy(jedis,lock,tryLock);
        }

    }

    @Override
    public PageInfo<Client> getClientLikeName(String name, Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
//        Example example = new Example(Client.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andLike("name", name);
//        PageInfo<Client> pageInfo = new PageInfo<>(clientMapper.selectByExample(example));//[]
        PageInfo<Client> pageInfo = new PageInfo<>(clientMapper.getClientLikeName(name));
        return pageInfo;
    }

    @Override
    public Client getClientByName(String name) {

        Example example = new Example(Client.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name", name);
        List<Client> clients = clientMapper.selectByExample(example);
        if(clients != null || clients.size() > 0){
            return clients.get(0);
        }
        return null;
//        Jedis jedis = null;
//        RLock lock = null;
//        boolean tryLock = false;
//        try{
//
////            jedis = redisUtil.getJedis();
////            jedis.select(1);
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//            boolean locked = lock.isLocked();
//            if (!locked) {//没有被锁
//                return getClientByName2(jedis, name);
//            }else{//有锁自旋
//                return getClientByName(name);//自旋
//            }
//        }finally {
//            destroy(jedis, lock, tryLock);
//        }

    }

    private Client getClientByName2(Jedis jedis, String name){//复用
        //判断是否要更新缓存
        boolean flag = jedis.exists(CACHEALLSTR);//null
//                Long hlen = jedis.hlen(CACHEALLSTR);
        if (!flag) {
            //没有缓存数据 -- 查询数据库
            Example example = new Example(Client.class);
            Example.Criteria createCriteria1 = example.createCriteria();
            createCriteria1.andEqualTo("name", name);
            List<Client> clients2= clientMapper.selectByExample(example);//[]
            if(clients2.size() > 0){
                return clients2.get(0);
            }
        }else{
            //缓存中有数据
            String str1 = jedis.hget(CACHEALLSTR,"client:" + name + ":info");
            if (StringUtils.isNotBlank(str1)) {
                return JSON.parseObject(str1, Client.class);
            }
        }
        return null;
    }

    @Override
//    @Transactional()//事务回滚//注释了没错也插入不了
    public int add(Client client) {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            tryLock = lock.tryLock();
            if (tryLock) {//成功上锁
                //防止快速多次点击，某部分跳过name检查，所以再次检查是否已经存在数据
                Client clientByName2 = getClientByName2(jedis, client.getName());
                if (clientByName2 == null) {//确认没有数据再添加
                    int insert = clientMapper.insert(client);
                    System.out.println("client" + client);
                    if (insert != 0){
                        //判断是否要更新缓存
                        Boolean flag = jedis.exists(CACHEALLSTR);
                        if (flag) {//缓存中有数据
                            String str = "client:" + client.getName() + ":info";
                            jedis.hset(CACHEALLSTR, str,JSON.toJSONString(client));
                            jedis.zadd(CACHEALLSTRZSET, client.getId(), JSON.toJSONString(client));
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
                return add(client);
            }
        }finally {
            destroy(jedis, lock, tryLock);
        }

    }

    //当前页 一页多少个  mysql通过limit分页的哈
    public PageInfo<Client> findClientList(int page, int size) {
        // 开启分页插件,放在查询语句上面 帮助生成分页语句
        PageHelper.startPage(page, size); //底层实现原理采用改写语句   将下面的方法中的sql语句获取到然后做个拼接 limit  AOPjishu
        List<Client> listClient = getAll();
        // 封装分页之后的数据  返回给客户端展示  PageInfo做了一些封装 作为一个类
        PageInfo<Client> pageInfoDemo = new PageInfo<Client>(listClient);
        //所有分页属性都可以冲pageInfoDemo拿到；
        return pageInfoDemo;
    }


    @Override
    public List<Client> getAll2() {

        return clientMapper.selectAll();
//        Jedis jedis = null;
//        RLock lock = null;
//        boolean tryLock = false;
//        try{
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//            boolean isLocked = lock.isLocked();
//            if (!isLocked) {//没有被锁
//                Map<String, String> clientMap = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
//                if (clientMap != null && clientMap.size() > 0) {//缓存中有数据
//                    List<Client> clientList = new ArrayList<>();
//                    clientMap.forEach((k,v)->{
//                        clientList.add(JSON.parseObject(v, Client.class));
//                    });
//                    return clientList;
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
    public List<Client> getAll() {

        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
            boolean isLocked = lock.isLocked();
            if (!isLocked) {//没有被锁
                Map<String, String> clientMap = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
                if (clientMap != null && clientMap.size() > 0) {//缓存中有数据
                    List<Client> clientList = new ArrayList<>();
                    clientMap.forEach((k,v)->{
                        clientList.add(JSON.parseObject(v, Client.class));
                    });
                    return clientList;
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
    public PageInfo<Client> getAllPage(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        PageInfo<Client> pageInfo = new PageInfo<>(clientMapper.selectAll());
        return pageInfo;
//        Jedis jedis = null;
//        RLock lock = null;
//        boolean tryLock = false;
//        try{
//            jedis = initCache();
//            lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//            boolean isLocked = lock.isLocked();
//            if (!isLocked) {//没有被锁
//                Map<String, String> clientMap = jedis.hgetAll(CACHEALLSTR);//不存在返回 {}
//                if (clientMap != null && clientMap.size() > 0) {//缓存中有数据
//                    if (!jedis.exists(CACHEALLSTRZSET)) {
//                        return null;
//                    }
//                    Set<String> clientStrSet = new HashSet<>();
//                    Set<Client> clientSet = new HashSet<>();
////                clientMap.forEach((k,v)->{
////                    clientList.add(JSON.parseObject(v, Client.class));
////                });
////                    clientSet = jedis.zrangeByScore(CACHEALLSTR + ":zset", 0, 100, 0, pageSize);//max 不能设置为-1
//                    clientStrSet = jedis.zrangeByScore(CACHEALLSTRZSET, "-inf", "+inf", (pageNum - 1) * pageSize, pageSize);//min max可以直接用数字字符串
//                    clientStrSet.forEach(clientStr -> {
//                        clientSet.add(JSON.parseObject(clientStr, Client.class));
//                    });
//
//                    MyPage<Client> myPage = new MyPage<>();
//                    myPage.setPageNum(pageNum);
//                    myPage.setPageSize(pageSize);
//                    myPage.setList(clientSet);
//                    myPage.setTotal(jedis.zcard(CACHEALLSTRZSET));
//                    return myPage;
//                }
//
//                //缓存没有数据则查询数据库，并将数据同步到缓存
//                // 设置分布式锁
//                lock = redissonClient.getLock(CACHEALLLOCK);// 声明锁
//                tryLock = lock.tryLock();
//                if (tryLock) {//成功上锁
//                    List<Client> list = addAllCache(jedis);
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
    private List<Client> addAllCache(Jedis jedis){
        //缓存中没有数据,从数据库中获取
        List<Client> clients = clientMapper.selectAll();//没有值返回 []

        if (clients != null && clients.size() > 0) {
            // mysql查询结果存入redis
            for (Client client : clients) {
                String str = "client:" + client.getName() + ":info";
                jedis.hset(CACHEALLSTR, str, JSON.toJSONString(client));
                //用来分页
                jedis.zadd(CACHEALLSTRZSET, client.getId(), JSON.toJSONString(client));
            }
            return clients;
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
