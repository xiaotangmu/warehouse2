package com.tan.warehouse2.service;

import com.tan.warehouse2.bean.ActiveUser;
import com.tan.warehouse2.bean.User;

import java.util.Set;

public interface UserService {

    public ActiveUser getUserByName(String name);

    public int updatePassword(ActiveUser user, String oldPassword);

    public int update(User user);

    public int add(User user, String password);

    public User getUserById(int id);

    //验证码
    public void addValidateCode(Long l);
    public boolean getValidateCode(String validateCode);

    User getUserByName2(String name);

    User getUserAndRoleByName(String name);

    int updateUserRole(Integer userId, Set<Integer> set);

    User getUserRoleAndAuthorityByName(String name);

    Set<Integer> getUserIdByAuthority(String name);

    int updateInfo(User user);

    void updatePhoto(String name, String imgUrl);
}
