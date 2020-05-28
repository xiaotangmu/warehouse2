package com.tan.warehouse2.newspush;

import com.alibaba.fastjson.JSON;
import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.bean.Notification;
import com.tan.warehouse2.service.NotificationService;
import com.tan.warehouse2.service.UserService;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yeauty.annotation.*;
import org.yeauty.pojo.ParameterMap;
import org.yeauty.pojo.Session;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

@ServerEndpoint(prefix = "netty-websocket")
@Component
public class MyWebSocket {

    public static NotificationService notificationService;
    public static UserService userService;

    @Autowired
    NotificationService notificationService2;
    @Autowired
    UserService userService2;

    static Map<String, Object> returnMap = null;

    @PostConstruct
    public void beforeInit() {
    	notificationService = notificationService2;
    	userService = userService2;
    }

    private static final Logger logger = LoggerFactory.getLogger(MyWebSocket.class);

    /**
     * 用线程安全的CopyOnWriteArraySet来存放客户端连接的信息
     */
    private static CopyOnWriteArraySet<SocketClient> socketServers = new CopyOnWriteArraySet<>();
//    private static Map<String, CopyOnWriteArraySet<SocketClient>> socketMap = new HashMap<>();//分店管理

    /**
     * websocket封装的session,信息推送，就是通过它来信息推送
     */
    private Session session;

    @OnOpen//有客户端连接进来
//    HttpServletRequest request 协议不同使用不了，会报错
    public void onOpen(Session session, HttpHeaders headers, ParameterMap parameterMap) throws IOException {

    	String userId = parameterMap.getParameter("userId");
        String userName = parameterMap.getParameter("userName");//没有该参数会报空指针异常
        userName = URLDecoder.decode(userName, "UTF-8");//解决请求路径的中文乱码

        String pushAuthority = null;
        try{
            pushAuthority  = parameterMap.getParameter("pushAuthority");//没有该参数会报空指针异常
            pushAuthority = URLDecoder.decode(pushAuthority, "UTF-8");//解决请求路径的中文乱码
        }catch (NullPointerException e){
            //没有权限直接退出
            return;
        }

//        判断是否有权限
        if(StringUtils.isBlank(pushAuthority)){
            return;
        }

        //添加client
        this.session = session;
        session.setAttribute("userId", userId);
        SocketClient client = new SocketClient(userId, userName, session);
        socketServers.add(client);
        //发送用户数据库通知
        List<Notification> notifications = notificationService.getNotificationsByUserId(Integer.parseInt(userId));

        if(notifications != null && notifications.size() > 0){
            returnMap = new HashMap<>();
            returnMap.put("notifications", notifications);
            String jsonStr = JSON.toJSONString(returnMap);
            session.sendText(jsonStr);
            logger.info(jsonStr);
        }

        logger.info("客户端:【{}】连接成功", "userId : " + userId + ", userName: " + userName);
    }

    @OnClose//客户端关闭连接时触发
    public void onClose(Session session) throws IOException {// session为当前触发操作的客户端
        socketServers.forEach(client -> {
            if (client.getSession().id().equals(session.id())) {

                logger.info("客户端:【{}】断开连接", "userId : " + client.getUserId() + ", userName: " + client.getUserName());
                socketServers.remove(client);//移出在线客户端集合

            }
        });
    }

    @OnError//session 发生错误时触发
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    /*
     * @OnMessage
     * 当接收到字符串消息时，对该方法进行回调 注入参数的类型:Session、String
     */
    @OnMessage//接收消息
    public void onMessage(Session session, String message) {
        if (StringUtils.isBlank(message)){
            return ;
        }
        Notification notification = JSON.parseObject(message, Notification.class);

        //更新为已读
        notificationService.updateStatuWithUser(notification.getUserId(), notification.getId());

        //转发消息
//        MyWebSocket.sendAll(message);
    }


    //创建消息
    public synchronized static void createNotificatioin(Notification notification){
        //创建时间
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        notification.setCreateTime(sdf.format(new Date()));
//        notification.setUserId(Integer.parseInt(session.getAttribute("userId")));
        //保存到数据库
        notification = notificationService.add(notification);

        System.out.println(notification);
        if(notification.getId() != 0){//插入成功
            pushNews(notification);
        }
    }

    //推送消息给分店店员，包括分店店长自己
    public synchronized static void pushNews(Notification notification){
        int notificationId = notification.getId();
        //将消息与会员的关系保存到数据库
        //获取需要推送的user
        Set<Integer> userIds = userService.getUserIdByAuthority("接收库存报警");
        if (userIds == null || userIds.size() < 1){
            return;
        }

        notificationService.addNotificationToUser(notification.getId(),userIds);
        returnMap = new HashMap<>();
        Set<Integer> readUsers = new HashSet<>();

        //将消息推送给在线接收员
        socketServers.forEach(client ->{

            if (userIds.contains(Integer.parseInt(client.getUserId()))) {
//                    String jsonStr = JSON.toJSONString(notification);
                List<Notification> notifications = new ArrayList<>();
                notification.setStatus("0");
                notifications.add(notification);
                returnMap.put("notifications", notifications);
                String jsonStr = JSON.toJSONString(returnMap);
                client.getSession().sendText(jsonStr);
                readUsers.add(Integer.parseInt(client.getUserId()));
            }
        });

        //更新已读数据 -- 应该是读了之后才更新，不是发过去就算读了
//        if(readUsers.size() > 0){
//            notificationService.updateStatusWithUser(readUsers, notificationId);
//        }
    }


    /**
     * 服务端的userName,因为用的是set，每个客户端的username必须不一样，否则会被覆盖。
     * 要想完成ui界面聊天的功能，服务端也需要作为客户端来接收后台推送用户发送的信息
     */
    private final static String SYS_USERNAME = "niezhiliang9595";

    /**
     * 信息发送的方法，通过客户端的userId
     * 拿到其对应的session，调用信息推送的方法
     */
    public synchronized static void sendMessage(String message, String userName, int typeCode) {//typeCode 100 代表onopen 200 代表onmessage

        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("num", getOnlineNum());
        map.put("typeCode", typeCode);

        String json = JSON.toJSONString(map);//转为json字符串，前台再转为json对象
        System.out.println(json);
        socketServers.forEach(client -> {
            if (userName.equals(client.getUserName())) {
                try {
                    client.getSession().sendText(json);

                    logger.info("服务端推送给客户端 :【{}】", client.getUserName(), message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取服务端当前客户端的连接数量，
     * 因为服务端本身也作为客户端接受信息，
     * 所以连接总数还要减去服务端
     * 本身的一个连接数
     * <p>
     * 这里运用三元运算符是因为客户端第一次在加载的时候
     * 客户端本身也没有进行连接，-1 就会出现总数为-1的情况，
     * 这里主要就是为了避免出现连接数为-1的情况
     *
     * @return
     */
    public synchronized static int getOnlineNum() {
        return socketServers.stream().filter(client -> !client.getUserName().equals(SYS_USERNAME))
                .collect(Collectors.toList()).size();
    }

    /**
     * 获取在线用户名，前端界面需要用到
     *
     * @return
     */
    public synchronized static List<String> getOnlineUsers() {

        List<String> onlineUsers = socketServers.stream()
                .filter(client -> !client.getUserName().equals(SYS_USERNAME))
                .map(client -> client.getUserName())
                .collect(Collectors.toList());

        return onlineUsers;
    }

    /**
     * 信息群发，我们要排除服务端自己不接收到推送信息
     * 所以我们在发送的时候将服务端排除掉
     *
     * @param message
     */
    public synchronized static void sendAll(String message) {
        //群发，不能发送给服务端自己
        socketServers.stream().filter(cli -> cli.getUserName() != SYS_USERNAME)
                .forEach(client -> {
                    try {
                        System.out.println("sendAll: " + client);
                        Map<String, Object> map = new HashMap<>();
                        map.put("message", message);
                        map.put("num", 0);
                        map.put("typeCode", 200);

                        String json = JSON.toJSONString(map);//转为json字符串，前台再转为json对象

                        client.getSession().sendText(json);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        logger.info("服务端推送给所有客户端 :【{}】", message);
    }

    /**
     * 多个人发送给指定的几个用户
     *
     * @param message
     * @param persons
     */
    public synchronized static void SendMany(String message, String[] persons) {
        for (String userName : persons) {
            sendMessage(message, userName, 200);
        }
    }

}