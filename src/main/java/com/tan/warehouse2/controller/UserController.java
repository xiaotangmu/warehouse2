package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.*;
import com.tan.warehouse2.bean.User;
import com.tan.warehouse2.service.UserService;
import com.tan.warehouse2.utils.CookieUtil;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @Description:
 * @date: 2020-04-22 14:07:32
 * @author: Tan.WL
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

//    @PostMapping("/updatePassword")
//    public Object udpatePassword(User user, String oldPassword, String newPassword){
//        if(StringUtils.isBlank(user.getName()) || user.getId() == null || user.getId() < 1 || StringUtils.isBlank(oldPassword)
//                || StringUtils.isBlank(newPassword)){
//            return Msg.failError("提交的数据有误");
//        }
//        userService.getUser(user.getName(), oldPassword);
//        userService.updatePassword(user, oldPassword)
//    }

    @PostMapping("/updateInfo")
    public Object updateInfo(User user){
        if(user.getId() == null || user.getId() < 1 || StringUtils.isBlank(user.getName())){
            return Msg.failError("提交的数据有误");
        }
        userService.updateInfo(user);
        return Msg.success("");
    }

    @PostMapping("/updatePassword")
    public Object update(ActiveUser user, String oldPassword){
        if(StringUtils.isBlank(user.getName()) || StringUtils.isBlank(oldPassword)
                || StringUtils.isBlank(user.getPassword())){
            return Msg.failError("提交的数据有误");
        }
        int update = userService.updatePassword(user, oldPassword);
        if(update != 0){
            if (update == -1) {
                return Msg.success("-1");
            }
            return Msg.success("1");
        }
        return Msg.failError("更新失败");
    }

    @PostMapping("/update")
    public Object update(User user){
        System.out.println("update ..." + user);
        if (StringUtils.isNotBlank(user.getName()) && user.getId() != null && user.getId() > 0){
            int update = userService.update(user);
            if (update != 0) {
                return Msg.success("");
            }
        }
        return Msg.failError("更新失败！");
    }
    
    

    @PostMapping("/nameCheck")
//    @GetMapping("/nameCheck")
    public Object nameCheck(String name){
        System.out.println(name);
        if (StringUtils.isBlank(name)) {
            return Msg.failError("用户名不能为空！");
        }
        ActiveUser user = userService.getUserByName(name);
        if(user == null){
            return Msg.success("0");//表示该品牌并不存在
        }else{
            return Msg.success("1");//表示该品牌已经存在
        }
    }

    @PostMapping("/find")
    public Object find(String name){
        System.out.println(name);
        if (StringUtils.isBlank(name)) {
            return Msg.failError("用户名不能为空！");
        }
        return Msg.success(userService.getUserAndRoleByName(name));
    }

    @PostMapping("/updateRole")
    public Object updateRole(Integer userId, String roleIds){

        List<Integer> integers = MyStrUtil.intSplit(roleIds, "-");
        Set<Integer> set = new HashSet<>();
        set.addAll(integers);
        int i = userService.updateUserRole(userId, set);
        if(i != 0){
            return Msg.success("更新成功");
        }
        return Msg.failError("更新失败！");
    }

    @PostMapping("/getUserRoleAndAuthority")
    public Object getUserRoleAndAuthority(String name){
        System.out.println(name);
        if(StringUtils.isBlank(name)){
            return Msg.failError("用户数据有误");
        }
        User user = userService.getUserRoleAndAuthorityByName(name);
        if(user != null){
            return Msg.success(user);
        }
        return Msg.failError("获取用户数据失败！");
    }

}
