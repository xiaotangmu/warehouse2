package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.ActiveUser;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.bean.User;
import com.tan.warehouse2.service.UserService;
import com.tan.warehouse2.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

/**
 * @Descion:
 * @date: 2020-04-25 19:35:11
 * @author: Tan.WL
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class PassportController {

//    @Autowired
//    CookieUtil cookieUtil;
    @Autowired
    UserService userService;

    @GetMapping("/unLogin")
    public Object unLogin(HttpServletRequest request,  HttpServletResponse response){
        CookieUtil cookieUtil = new CookieUtil();
        cookieUtil.deleteCookie(request, response, "token");
        return Msg.unLogin();
    }

    @PostMapping("/register")
    public Object register(User user, String password){
        String name = user.getName();
        String email = user.getEmail();
        if (StringUtils.isBlank(name) || StringUtils.isBlank(password)){
            return Msg.failError("您提交的数据有误！");
        }
        String nameFormat = "^[\\w-]{3,16}$";//[A-Za-z0-9_] 3-16位
        String emailFormat = "^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$";//邮箱格式
        String passwordFormat = "^[a-zA-Z0-9_\\-!\\?\\.]{6,18}$";//密码格式
        Pattern r = Pattern.compile(nameFormat);
        Pattern r2 = Pattern.compile(emailFormat);
        Pattern r3 = Pattern.compile(passwordFormat);

        //判断数据格式 -- 跳过页面 直接访问
        if(!r.matcher(name).matches()){//name 只包含 英文字符和数字_ 8 -16 位
            return Msg.failError("name 格式不正确");
        }
        if(!r2.matcher(email).matches()){//name 只包含 英文字符和数字_ 8 -16 位
            return Msg.failError("email 格式不正确");
        }
        if(!r3.matcher(password).matches()){//name 只包含 英文字符和数字_ 8 -16 位
            return Msg.failError("密码格式不正确");
        }

        //判断名字是否重复
        ActiveUser user2 = userService.getUserByName(name);
        if (user2 != null){
            return Msg.noCondition("该name已经存在");
        }
        int add = userService.add(user, password);
        if (add != 0 && add != -1) {
            if (user.getId() != null) {
                return Msg.success(user);
            }
        }else if (add == -1){
            return Msg.noCondition("该name已经存在");
        }
        return Msg.failError("添加失败！");
    }

    /**
     * 登录逻辑处理
     */
    @PostMapping("/login")
    public Object login(String validateCode, String name, String password, HttpServletRequest request, HttpServletResponse response){
        System.out.println("login");

        //判断是否有验证码
        if (StringUtils.isBlank(validateCode)) {
            return Msg.noCondition("请先通过滑动验证");
        }else {//判断验证码是否正确
            boolean flag = userService.getValidateCode(validateCode);
            if (!flag) {
                return Msg.noCondition("请先通过滑动验证");
            }
        }

        //判断提交的数据格式是否正确
        String nameFormat = "^[\\w-]{3,16}$";//[A-Za-z0-9_] 3-16位
        String passwordFormat = "^[a-zA-Z0-9_\\-!\\?\\.]{6,18}$";//密码格式
        Pattern r = Pattern.compile(nameFormat);
        Pattern r3 = Pattern.compile(passwordFormat);

        //判断数据格式 -- 跳过页面 直接访问
        if(!r.matcher(name).matches()){//name 只包含 英文字符和数字_ 8 -16 位
            return Msg.noCondition("name 格式不正确");
        }
        if(!r3.matcher(password).matches()){
            return Msg.noCondition("password 格式不正确");
        }

        try{
            //1. 构造登录令牌 UesrNamePasswordToken
            //加密密码
//            password = new Md5Hash(password,name,3).toString();//1.密码，salt，加密次数
            UsernamePasswordToken upToken = new UsernamePasswordToken(name,password);
            //2.获取subject
            Subject subject = SecurityUtils.getSubject();
            //3.调用login 方法，进入realm 完成认证
            subject.login(upToken);
            //4. 获取sessionId
            String sessionId = (String)subject.getSession().getId();
            //5.构造返回结果

//    /*
//	 * 使用 Shiro 编写认证操作
//	 */
//        //1. 获取 Subject
//        Subject subject = SecurityUtils.getSubject();
//
//        //2. 封装用户数据
//        //... 一系列加密&解密
//        UsernamePasswordToken token = new UsernamePasswordToken(name, password);
//
//        //3. 执行登录方法
//        try {
//            subject.login(token);
//
//            //登录成功
//            String ip = request.getHeader("x-forwarded-for");// 通过nginx转发的客户端ip
//            if (StringUtils.isBlank(ip)) {
//                ip = request.getRemoteAddr();// 从request中获取ip
//                if (StringUtils.isBlank(ip)) {
//                    ip = "127.0.0.1";//都没有，出错，这里直接给了
//                }
//            }
//
//            Map<String, Object> userMap = new HashMap<>();
//            userMap.put("name", name);
//            userMap.put("password", password);
//            // 按照设计的算法对参数进行加密后，生成token
//            String tokenStr = JwtUtil.encode("warehouse22020", userMap, ip);
//            CookieUtil.setCookie(request,response,"token", tokenStr,60*60*3,true);//有效时间3个钟
//             //... 一系列加密解密
//            return Msg.success(tokenStr);
            CookieUtil.setCookie(request,response,"token", sessionId,60*60*3,true);//有效时间3个钟
            //... 一系列加密解密
            return Msg.success(sessionId);
        } catch (UnknownAccountException e) {
//            model.addAttribute("msg", "用户名不存在");
            return Msg.noCondition("用户不存在");
        } catch (IncorrectCredentialsException e) {
            //登录失败：密码错误
            return Msg.noCondition("密码错误");
        }
    }

    @GetMapping("/logout")
    public Object logout(){
        System.out.println("logout");
        //1. 获取 Subject
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return Msg.success("");
    }


    @PostMapping("/validateCode")
    public Object validateCode(HttpServletRequest request, HttpServletResponse response){
        System.out.println("validateCode");
        long l = System.currentTimeMillis();
        userService.addValidateCode(l);
        CookieUtil.setCookie(request, response, "validateCode", l + "", 60, true);//设置60s后过期

        return Msg.success("");
    }


}
