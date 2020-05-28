package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Role;
import com.tan.warehouse2.bean.MyPage;

import java.util.List;
import java.util.Set;

public interface RoleService {

    public List<Role> getAll();

    public Role getRoleByName(String name);

    public int add(Role brand, Set<Integer> authIds);

    public int update(Role brand, String oldName, Set<Integer> authIds);

    public int delete(List<Integer> ids);//可以批量删除

//    public MyPage<Role> getAllPage(Integer pageNum, Integer pageSize);

    public List<Role> getRoleLikeName(String name);

}
