package com.tan.warehouse2.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("websocket")
public class WebSocketController {

//    @Autowired
//    private MyWebSocket socketServer;
//
//    /**
//     *
//     * 客户端页面
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/index")
//    public Object index() {
//        List<Object> list = new ArrayList<>();
//        list.add("jj");
//        list.add(22);
//
//        Object json = JSON.toJSON(list);
//        return json;
//    }
//
//    /**
//     *
//     * 服务端页面
//     * @param model
//     * @return
//     */
//    @RequestMapping(value = "/admin")
//    public String admin(Model model) {
//        int num = socketServer.getOnlineNum();
//        List<String> list = socketServer.getOnlineUsers();
//
//        model.addAttribute("num",num);
//        model.addAttribute("users",list);
//        return "admin";
//    }
//
//    /**
//     * 个人信息推送
//     * @return
//     */
//    @RequestMapping("sendmsg")
//    @ResponseBody
//    public String sendmsg(String msg, String username){
//        //第一个参数 :msg 发送的信息内容
//        //第二个参数为用户长连接传的用户人数
//        String [] persons = username.split(",");
//        MyWebSocket.SendMany(msg,persons);
//        return "success";
//    }
//
//    /**
//     * 推送给所有在线用户
//     * @return
//     */
//    @RequestMapping("sendAll")
//    @ResponseBody
//    public String sendAll(String msg){
//        MyWebSocket.sendAll(msg);
//        return "success";
//    }
}
