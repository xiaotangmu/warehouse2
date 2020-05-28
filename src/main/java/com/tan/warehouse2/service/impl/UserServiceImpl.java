package com.tan.warehouse2.service.impl;

import com.alibaba.fastjson.JSON;
import com.tan.warehouse2.bean.ActiveUser;
import com.tan.warehouse2.bean.Authority;
import com.tan.warehouse2.bean.Role;
import com.tan.warehouse2.bean.User;
import com.tan.warehouse2.mapper.ActiveUserMapper;
import com.tan.warehouse2.mapper.RoleMapper;
import com.tan.warehouse2.mapper.UserMapper;
import com.tan.warehouse2.service.RoleService;
import com.tan.warehouse2.service.UserService;
import com.tan.warehouse2.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ActiveUserMapper activeUserMapper;

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RoleMapper roleMapper;
    
    private final static String CACHEINFOSTR = "user:info:";//name 检查 -- spu:info:id(catalog3):brandId
    private final static String CACHEPASSWORD = "user:password:";//name 检查 -- spu:info:id(catalog3):brandId
    private final static String CACHEALLLOCK = "user:lock:";//加锁 -- spu:lock:id(catalog3)
    private final static String CACHEINFOSTRZSET = "user:info:zset";//分页


    @Override
    public void updatePhoto(String name, String imgUrl) {
        Jedis jedis = null;
        try{
            jedis = initCache();
            userMapper.updatePhotoByName(name, imgUrl);
            if(jedis.exists(CACHEINFOSTR + name)){
                String s = jedis.get(CACHEINFOSTR + name);
                User user1 = JSON.parseObject(s, User.class);
                user1.setPhoto(imgUrl);
                jedis.del(CACHEINFOSTR + name);
                jedis.setex(CACHEINFOSTR + name, 60*60*6,JSON.toJSONString(user1));
            }
        }finally {
            destroy(jedis,null,false);
        }
    }

    @Override
    public int updateInfo(User user) {
        Jedis jedis = null;
        try{
            jedis = initCache();
            int i = userMapper.updateByPrimaryKeySelective(user);
            if(i != 0){
                if(jedis.exists(CACHEINFOSTR + user.getName())){
                    String s = jedis.get(CACHEINFOSTR + user.getName());
                    User user1 = JSON.parseObject(s, User.class);
                    user1.setNickname(user.getNickname());
                    user1.setEmail(user.getEmail());
                    user1.setPhone(user.getPhone());
                    user1.setGender(user.getGender());
                    jedis.del(CACHEINFOSTR + user.getName());
                    jedis.setex(CACHEINFOSTR + user.getName(), 60*60*6,JSON.toJSONString(user1));
                }
                return i;
            }
            //缓存数据
            return 0;
        }finally {
            destroy(jedis,null,false);
        }

    }

    @Override
    public Set<Integer> getUserIdByAuthority(String name) {

        return userMapper.getUserIdByAuthority(name);
    }

    @Override
    public User getUserRoleAndAuthorityByName(String name) {
        User user1 = getUserAndRoleByName(name);
        List<Role> roles = user1.getRoles();
        if(roles != null || roles.size() > 0){
            for (Role role : roles) {
                List<Authority> authorities = roleMapper.getRoleAuthorities(role.getId());
                if(authorities == null){
                    authorities = new ArrayList<>();
                }
                Set<Authority> set = new HashSet<>();
                set.addAll(authorities);
                List<Authority> as = new ArrayList<>();
                as.addAll(set);
                role.setAuthorities(as);
            }
        }else{
            roles = new ArrayList<>();
        }
        user1.setRoles(roles);
        return user1;
    }

    @Override
    public int updateUserRole(Integer userId, Set<Integer> set){
        Set<Integer> userIds = new HashSet<>();
        userIds.add(userId);
        userMapper.deleteUserRole(userIds);
        int i = userMapper.insertUserRole(userId, set);
        return i;
    }

    @Override
    public User getUserByName2(String name) {

        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name", name);
        List<User> users = userMapper.selectByExample(example);
        if(users != null || users.size() > 0){
            return users.get(0);
        }
        return null;
    }

    @Override
    public User getUserAndRoleByName(String name) {

        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name", name);
        List<User> users = userMapper.selectByExample(example);
        if(users != null || users.size() > 0){

            User user = users.get(0);
            List<Role> roles = userMapper.getRoleByUserId(user.getId());
            if(roles == null || roles.size() < 1){
                roles = new ArrayList<>();
            }

            user.setRoles(roles);
            return user;
        }
        return null;
    }

    @Override
    public int updatePassword(ActiveUser user, String oldPassword) {
        Jedis jedis = null;
        try{
            jedis = initCache();
            //判断密码是否正确
            String s = jedis.get(CACHEINFOSTR + user.getName());
            if(StringUtils.isNotBlank(s)){
                ActiveUser activeUser = JSON.parseObject(s, ActiveUser.class);
                if (!activeUser.getPassword().equals(oldPassword)) {
                    return -1;
                }
            }else{
                Example example = new Example(ActiveUser.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andEqualTo("name", user.getName());
                criteria.andEqualTo("password", oldPassword);
                List<ActiveUser> activeUsers = activeUserMapper.selectByExample(example);
                if(activeUsers == null || activeUsers.size() <= 0){
                    return -1;
                }
            }

            //更新密码
            int i = activeUserMapper.updateByPrimaryKey(user);
            if(i != 0){
                if(jedis.exists(CACHEPASSWORD + user.getName())){
                    jedis.del(CACHEPASSWORD + user.getName());
                }
                jedis.setex(CACHEPASSWORD + user.getName(), 60 * 60 * 6, JSON.toJSONString(user));
                return i;
            }
            //缓存数据
            return 0;
        }finally {
            destroy(jedis,null,false);
        }
    }

    @Override
    public int update(User user) {
        Jedis jedis = null;
        try{
            jedis = initCache();
            int i = userMapper.updateByPrimaryKey(user);
            if(i != 0){
                if(jedis.exists(CACHEINFOSTR + user.getName())){
                    jedis.del(CACHEINFOSTR + user.getName());
                }
                jedis.setex(CACHEINFOSTR + user.getName(), 60 * 60 * 6, JSON.toJSONString(user));
                return i;
            }
            //缓存数据
            return 0;
        }finally {
            destroy(jedis,null,false);
        }
    }

    @Override
    public int add(User user, String password) {
        Jedis jedis = null;
        try{
            jedis = initCache();
            ActiveUser activeUser = new ActiveUser();
            activeUser.setName(user.getName());
            activeUser.setPassword(password);
            int insert = activeUserMapper.insert(activeUser);
            if(insert != 0){
                user.setActiveId(activeUser.getId());
                userMapper.insert(user);
                //缓存数据
                jedis.setex(CACHEINFOSTR + user.getName(), 60 * 60 * 6, JSON.toJSONString(user));
                jedis.setex(CACHEPASSWORD + user.getName(),60 * 60 * 6,JSON.toJSONString(activeUser));
                return insert;
            }
            return 0;
        }finally {
            destroy(jedis,null,false);
        }

    }

    @Override
    public User getUserById(int id) {
        return userMapper.findById(id);
    }


    @Override
    public void addValidateCode(Long l) {
        Jedis jedis = null;
        try{
            jedis = initCache();
            jedis.setex("validate:" + l+"",60, "");
        }finally {
            destroy(jedis,null,false);
        }
    }

    @Override
    public boolean getValidateCode(String validateCode) {
        Jedis jedis = null;
        RLock lock = null;
        boolean tryLock = false;
        try{
            jedis = initCache();
            lock = redissonClient.getLock("validate:" + validateCode + ":lock");// 声明锁
            tryLock = lock.tryLock();
            if(tryLock){
                Boolean exists = jedis.exists("validate:" + validateCode);
                if (exists != null && exists){
                    jedis.del("validate:" + validateCode);
                    return true;
                }
                return false;
            }else{
                return getValidateCode(validateCode);//自旋
            }
        }finally {
            destroy(jedis,lock,tryLock);
        }
    }

    @Override
    public ActiveUser getUserByName(String name) {
        return userMapper.findByName(name);
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
