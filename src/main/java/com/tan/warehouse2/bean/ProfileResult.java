package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.crazycake.shiro.AuthCachePrincipal;

import java.io.Serializable;
import java.util.Set;

/**
 * @Description:
 * @date: 2020-04-24 15:53:18
 * @author: Tan.WL
 */
//ProfileResult -- 返回的user数据包装类 -- 有个人信息 权限
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResult implements Serializable,AuthCachePrincipal {

    /**
     *
     */
    private static final long serialVersionUID = -3207880482640325843L;

//    private String mobile;
//    private String username;
//    private String company;
//    private String companyId;

    private String name;

    private Set<String> roleNames;
    private Set<String> authNames;
//    private Map<String,Object> roles = new HashMap<>();

    public ProfileResult(ActiveUser user) {
        this.name = user.getName();
    }

    /**
     *
     * @param user
     */
//    public ProfileResult(User user, List<Permission> list) {
//        this.mobile = user.getMobile();
//        this.username = user.getUsername();
//        this.company = user.getCompanyName();
//        this.companyId = user.getCompanyId();
//        Set<String> menus = new HashSet<>();
//        Set<String> points = new HashSet<>();
//        Set<String> apis = new HashSet<>();
//
//        for (Permission perm : list) {
//            String code = perm.getCode();
//            if(perm.getType() == 1) {
//                menus.add(code);
//            }else if(perm.getType() == 2) {
//                points.add(code);
//            }else {
//                apis.add(code);
//            }
//        }
//        this.roles.put("menus",menus);
//        this.roles.put("points",points);
//        this.roles.put("apis",apis);
//    }
//
//
//    public ProfileResult(User user) {
//        this.mobile = user.getMobile();
//        this.username = user.getUsername();
//        this.company = user.getCompanyName();
//        this.companyId = user.getCompanyId();
//        Set<Role> roles = user.getRoles();
//        Set<String> menus = new HashSet<>();
//        Set<String> points = new HashSet<>();
//        Set<String> apis = new HashSet<>();
//        for (Role role : roles) {
//            Set<Permission> perms = role.getPermissions();
//            for (Permission perm : perms) {
//                String code = perm.getCode();
//                if(perm.getType() == 1) {
//                    menus.add(code);
//                }else if(perm.getType() == 2) {
//                    points.add(code);
//                }else {
//                    apis.add(code);
//                }
//            }
//        }
//
//        this.roles.put("menus",menus);
//        this.roles.put("points",points);
//        this.roles.put("apis",apis);
//    }

    @Override
    public String getAuthCacheKey() {
        return null;
    }
}
