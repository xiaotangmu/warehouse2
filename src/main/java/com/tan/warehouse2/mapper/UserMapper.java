package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.ActiveUser;
import com.tan.warehouse2.bean.Role;
import com.tan.warehouse2.bean.User;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface UserMapper extends Mapper<User>{
    public ActiveUser findByName(String name);
    public User findById(int id);

    List<Role> getRoleByUserId(Integer id);

    void deleteUserRole(@Param("userIds") Set<Integer> userIds);

    int insertUserRole(@Param("userId") Integer userId, @Param("roleIds") Set<Integer> roleIds);

    Set<Integer> getUserIdByAuthority(String name);

    void updatePhotoByName(@Param("name") String name, @Param("imgUrl") String imgUrl);
}
