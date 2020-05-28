package com.tan.warehouse2.realm;

import com.tan.warehouse2.bean.*;
import com.tan.warehouse2.realm.IhrmRealm;
import com.tan.warehouse2.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

//独用realm UserRealm
public class UserRealm extends IhrmRealm {

    @Autowired
    UserService userService;

    //认证方法
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException{
        //1.获取用户的手机号和密码
        System.out.println("hello");
        UsernamePasswordToken upToken = (UsernamePasswordToken) authenticationToken;
        String name = upToken.getUsername();
        String password = new String( upToken.getPassword());
        //2.根据name查询用户
        ActiveUser user = userService.getUserByName(name);
        //3.判断用户是否存在，用户密码是否和输入密码一致
        if(user != null) {
            //4.构造安全数据并返回（安全数据：用户基本数据，权限信息 profileResult）
            ProfileResult result = new ProfileResult(user);
            User user2 = userService.getUserRoleAndAuthorityByName(name);
            List<Role> roles = user2.getRoles();
            if(roles != null || roles.size() > 0){
                Set<String> roleNames = new HashSet<>();
                Set<String> authNames = new HashSet<>();
                for (Role role : roles) {
                    roleNames.add(role.getRoleName());
                    List<Authority> authorities = role.getAuthorities();
                    Set<Authority> set = new HashSet<>();
                    set.addAll(authorities);
                    if(set.size() > 0){
                        for (Authority a : set) {
                            if(StringUtils.isNotBlank(a.getResource())){
                                authNames.add(a.getResource());
                            }
                        }
                    }
                }
                result.setAuthNames(authNames);
                result.setRoleNames(roleNames);
            }
//            if("user".equals(user.getLevel())) {
//                result = new ProfileResult(user);
//            }else {
//                Map map = new HashMap();
//                if("coAdmin".equals(user.getLevel())) {
//                    map.put("enVisible","1");
//                }
//                List<Permission> list = permissionService.findAll(map);
//                result = new ProfileResult(user,list);
//            }
//            if(!user.getPassword().equals(password)){
//
//            }
            //构造方法：安全数据，密码，realm域名
            return new SimpleAuthenticationInfo(result,user.getPassword(),this.getName());
        }
        //返回null，会抛出异常，标识用户名和密码不匹配
        return null;
    }

}
