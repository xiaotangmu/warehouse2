package com.tan.warehouse2.realm;

import com.tan.warehouse2.bean.ProfileResult;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.Set;

/**
 * @Description:
 * @date: 2020-04-24 15:52:25
 * @author: Tan.WL
 */
//公共reelm：获取安全数据，构造权限信息
public class IhrmRealm extends AuthorizingRealm {

    public void setName(String name){ super.setName("ihrmRealm"); }


    //授权方法
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection){
        //1.获取安全数据
        ProfileResult result = (ProfileResult)principalCollection.getPrimaryPrincipal();
        //2.获取权限信息
        Set<String> authNames = result.getAuthNames();
//        Set<String> apisPerms = (Set<String>) result.getRoles().get("apis");
        //3.构造权限数据，返回值
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(authNames);
        return info;
    }

    //认证方法
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        System.out.println("hello" );
        return null;
    }

}
