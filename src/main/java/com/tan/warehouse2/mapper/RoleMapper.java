package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Authority;
import com.tan.warehouse2.bean.Role;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface RoleMapper extends Mapper<Role> {

    public Boolean deleteRoles(@Param("ids") List<Integer> ids);//返回null ???
    public List<Role> getRoleLikeName(@Param("name") String name);

    public List<Authority> getRoleAuthorities(Integer id);

    public void insertRoleAndAuthority(@Param("roleId") Integer roleId, @Param("authIds") Set<Integer> authIds);

    public void deleteAuthorities(@Param("roleIds") List<Integer> roleIds);


}
