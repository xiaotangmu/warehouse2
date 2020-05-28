package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.mapper.NotificationMapper;
import com.tan.warehouse2.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @date: 2020-05-04 22:57:26
 * @author: Tan.WL
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    //全部标记已读
    @PostMapping("/flagAll")
    public Object flagAll(Integer userId){
        if (userId == null || userId < 1 ) {
            return Msg.failError("提交的数据有误！");
        }
        notificationService.flagAll(userId);
        return Msg.success("");
    }

    //删除全部
    @PostMapping("/deleteAll")
    public Object deleteAll(Integer userId){
        if (userId == null || userId < 1 ) {
            return Msg.failError("提交的数据有误！");
        }
        notificationService.deleteAll(userId);
        return Msg.success("");
    }

    //删除已读
    @PostMapping("/deleteFlag")
    public Object deleteFlag(Integer userId){
        if (userId == null || userId < 1 ) {
            return Msg.failError("提交的数据有误！");
        }
        notificationService.deleteFlag(userId);
        return Msg.success("");
    }

    //删除
    @PostMapping("/delete")
    public Object delete(Integer userId, Integer notId){
        if (userId == null || userId < 1 || notId == null || notId < 1) {
            return Msg.failError("提交的数据有误！");
        }
        notificationService.deleteOne(userId, notId);
        return Msg.success("");
    }

    //标记已读
    @PostMapping("/flag")
    public Object flag(Integer userId, Integer notId){
        if (userId == null || userId < 1 || notId == null || notId < 1) {
            return Msg.failError("提交的数据有误！");
        }
        notificationService.updateStatuWithUser(userId, notId);
        return Msg.success("");
    }

    @PostMapping("/getAllPage")
    public Object getAllPage(Integer userId, Integer pageNum, Integer pageSize){
        if(userId == null || userId < 1 || pageNum == null || pageNum < 1 || pageSize == null || pageSize < 1){
            return Msg.failError("提交的数据有误！");
        }

        return Msg.success(notificationService.getAllPage(userId, pageNum,pageSize));
    }
}
