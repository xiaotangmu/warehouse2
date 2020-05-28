package com.tan.warehouse2.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

//@Controller
public class ShiroController {

    @RequestMapping("unAuth")
    @ResponseBody
    public String unAuth(){
        return "You have not the authority!";
    }

    /**
     * 登录逻辑处理
     */
    @RequestMapping("login")
    public String login(String name, String password, Model model){
        System.out.println("login");
    /*
	 * 使用 Shiro 编写认证操作
	 */
        //1. 获取 Subject
        Subject subejct = SecurityUtils.getSubject();

        //2. 封装用户数据
        UsernamePasswordToken token = new UsernamePasswordToken(name, password);

        //3. 执行登录方法
        try {
            subejct.login(token);

            //登录成功
            //跳转到 test.html
            return "redirect:main.html";
        } catch (UnknownAccountException e) {
            //e.printStackTrace();
            //登录失败：用户名不存在
            model.addAttribute("msg", "用户名不存在");
            return "login";
        } catch (IncorrectCredentialsException e) {
            //e.printStackTrace();
            //登录失败：密码错误
            model.addAttribute("msg", "密码错误");
            return "login";
        }
    }

    @RequestMapping("update")
    @ResponseBody
    public String update(){
        return "Update Page!";
    }

    @RequestMapping("add")
    @ResponseBody
    @RequiresPermissions("user:add")    //注解版拦截
    public String add(){
        return "Add Page!";
    }

    @RequestMapping("toLogin")
    @ResponseBody
    public String toLogin(){
        return "ToLogin Page!";
    }

    @RequestMapping("index")
    public String index(){
        return "login.html";
    }


}
