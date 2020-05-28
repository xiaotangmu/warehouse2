package com.tan.warehouse2.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Notification;

import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @date: 2020-05-04 09:16:29
 * @author: Tan.WL
 */
public interface NotificationService {

    List<Notification> getNotificationsByUserId(int userId);

    Notification add(Notification notification);

    void addNotificationToUser(Integer notiId, Set<Integer> userIds);

    void updateStatusWithUser(Set<Integer> readUsers, int notificationId);

    void updateStatuWithUser(Integer userId, Integer id);

    PageInfo<Notification> getAllPage(Integer userId, Integer pageNum, Integer pageSize) ;

    void deleteOne(Integer userId, Integer notId);

    void deleteFlag(Integer userId);

    void deleteAll(Integer userId);

    void flagAll(Integer userId);

    void deleteByLimit(String limit);
}
