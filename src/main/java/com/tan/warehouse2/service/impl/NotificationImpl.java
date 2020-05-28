package com.tan.warehouse2.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Notification;
import com.tan.warehouse2.mapper.NotificationMapper;
import com.tan.warehouse2.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @date: 2020-05-04 09:16:54
 * @author: Tan.WL
 */
@Service
public class NotificationImpl implements NotificationService {

    @Autowired
    NotificationMapper notificationMapper;

    @Override
    public void deleteByLimit(String limit) {
        List<Integer> entryIdByLimit = notificationMapper.findNotificationIdByLimit(limit);
        notificationMapper.deleteByLimit(limit);
        if(entryIdByLimit != null && entryIdByLimit.size() > 0){

            notificationMapper.deleteRelationByIds(entryIdByLimit);
        }
    }

    @Override
    public void flagAll(Integer userId) {
        notificationMapper.flagAll(userId);
    }

    @Override
    public void deleteOne(Integer userId, Integer notId) {
        int i = notificationMapper.deleteByPrimaryKey(notId);
        if(i != 0){
            notificationMapper.deleteOne(userId, notId);
        }
    }

    @Override
    public void deleteFlag(Integer userId) {
        Set<Integer> ids = notificationMapper.findNotificationsByUserStatus(userId);
        if(ids != null && ids.size() > 0){
            notificationMapper.deleteNotifications(ids);
            notificationMapper.deleteFlag(userId);
        }
    }

    @Override
    public void deleteAll(Integer userId) {
        Set<Integer> ids = notificationMapper.findNotiIdByUserId(userId);
        if(ids != null && ids.size() > 0){
            notificationMapper.deleteNotifications(ids);
            notificationMapper.deleteAllByUserId(userId);
        }

    }

    @Override
    public PageInfo<Notification> getAllPage(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Notification> page = new PageInfo<>(notificationMapper.findNotificationsByUserId2(userId));
        return page;
    }

    @Override
    public void updateStatuWithUser(Integer userId, Integer id) {
        notificationMapper.updateStatuWithUser(userId, id);
    }

    @Override
    public void updateStatusWithUser(Set<Integer> readUserIds, int notificationId) {
        notificationMapper.updateStatusWithUser(readUserIds, notificationId);
    }

    @Override
    public void addNotificationToUser(Integer notiId, Set<Integer> userIds) {
        notificationMapper.insertNotificationToUser(notiId, userIds);
    }

    @Override
    public Notification add(Notification notification) {
        notificationMapper.insert(notification);
        System.out.println("insert " + notification);
        return notification;
    }

    @Override
    public List<Notification> getNotificationsByUserId(int userId) {

        return notificationMapper.findNotificationsByUserId(userId);

    }
}
