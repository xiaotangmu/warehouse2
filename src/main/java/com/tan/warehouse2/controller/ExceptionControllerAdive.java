package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Msg;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionControllerAdive {

    @ResponseBody
    @ExceptionHandler(UnauthorizedException.class)
    public Object handleShiroException(HttpServletRequest req, Exception ex) {
        return Msg.failError("权限不足！");
    }
    @ResponseBody
    @ExceptionHandler(AuthorizationException.class)
    public Object AuthorizationException(HttpServletRequest req, Exception ex) {
        return Msg.failError("权限认证失败！");
    }

    @ResponseBody
    @ExceptionHandler(SchedulerException.class)
    public Object SchedulerException(HttpServletRequest req, Exception ex) {
        return Msg.failError("日期已过期或日期格式不对！");
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Object ThrowException(HttpServletRequest req, Exception ex) {
        ex.printStackTrace();
        return Msg.failError("服务器异常！");
    }


}
