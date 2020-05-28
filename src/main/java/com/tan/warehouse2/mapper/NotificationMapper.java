package com.tan.warehouse2.mapper;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Notification;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @date: 2020-05-04 09:15:19
 * @author: Tan.WL
 */
public interface NotificationMapper extends Mapper<Notification>{

    List<Notification> findNotificationsByUserId(int userId);

    List<Notification> findNotificationsByUserId2(int userId);

    void insertNotificationToUser(@Param("notiId") Integer notiId, @Param("userIds") Set<Integer> userIds);

    void updateStatusWithUser(@Param("readUserIds") Set<Integer> readUserIds, @Param("notificationId") int notificationId);

    void updateStatuWithUser(@Param("userId") Integer userId, @Param("id") Integer id);


    void deleteOne(@Param("userId") Integer userId, @Param("notId") Integer notId);

    void deleteFlag(Integer userId);

    void deleteAllByUserId(Integer userId);

    Set<Integer> findNotificationsByUserStatus(Integer userId);

    void deleteNotifications(@Param("ids") Set<Integer> ids);

    Set<Integer> findNotiIdByUserId(Integer userId);

    void flagAll(Integer userId);



    void deleteByLimit(@Param("limit") String limit);

    List<Integer> findNotificationIdByLimit(@Param("limit") String limit);

    void deleteRelationByIds(@Param("ids") List<Integer> ids);
}
