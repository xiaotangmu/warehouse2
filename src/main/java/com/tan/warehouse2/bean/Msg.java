package com.tan.warehouse2.bean;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Msg {

    private String code;//返回的信息编码，101：成功处理；102：条件不足；201：服务器出错了，301：没有权限访问，401：没有登录
    //解析：102 会在什么情况下发生，登录后，跳过页面前端数据处理，直接访问后台

    private String message;//返回错误的提示信息
    private Object extend;//返回数据

    private Msg(){}

    //成功
    public static Msg success(Object extend){
        Msg msg = new Msg();
        msg.setCode("101");
        msg.setExtend(extend);
        return msg;
    }

    //条件不足
    public static Msg noCondition(String message){
        Msg msg = new Msg();
        msg.setCode("102");
        msg.setMessage(message);
        return msg;
    }

    //出错
    public static Msg failError(String message){
        Msg msg = new Msg();
        msg.setCode("201");
        msg.setMessage(message);
        return msg;
    }

    //无权
    public static Msg unAuth(){
        Msg msg = new Msg();
        msg.setCode("301");
        msg.setMessage("您没有该操作权限！");
        return msg;
    }
    //没有登录
    public static Msg unLogin(){
        Msg msg = new Msg();
        msg.setCode("401");
        msg.setMessage("您没有登录或者登录已过期，请重新登录！");
        return msg;
    }
}
