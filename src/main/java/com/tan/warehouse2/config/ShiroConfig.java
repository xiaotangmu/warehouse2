package com.tan.warehouse2.config;

import com.tan.warehouse2.manager.CustomSessionManager;
import com.tan.warehouse2.realm.UserRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    /**
     * 开启Shiro注解(如@RequiresRoles,@RequiresPermissions),
     * 需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
     * 配置以下两个bean(DefaultAdvisorAutoProxyCreator和AuthorizationAttributeSourceAdvisor)
     */
//    @Bean
//    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator(){
//        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
//        advisorAutoProxyCreator.setProxyTargetClass(true);
//        return advisorAutoProxyCreator;
//    }

    /**
     * 开启aop注解支持
     */
//    @Bean
//    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
//        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
//        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
//        return authorizationAttributeSourceAdvisor;
//    }



    /*
 	* 创建 ShiroFilterFactoryBean
 	*/
    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        //设置安全管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        //添加 Shiro 内置过滤器
        /**
         * Shiro内置过滤器，可以实现权限相关的拦截器
         * 	常用的过滤器：
         *		anon：无需认证（登录）可以访问
         *		authc：必须认证才可以访问
         *		user：如果使用rememberMe 的功能可以直接访问
         * 		perms： 该资源必须得到资源权限才可以访问
         *		role：该资源必须得到角色权限才可以访问
         *
         *      logout： 退出 清除 session 数据
         */
        Map<String,String> filterMap = new LinkedHashMap<String,String>();
//        filterMap.put("/add", "authc");
//        filterMap.put("/update", "authc");

        //无需拦截
        filterMap.put("/register", "anon");
        filterMap.put("/login", "anon");
        filterMap.put("/user/nameCheck", "anon");
        filterMap.put("/validateCode", "anon");
        filterMap.put("/unLogin", "anon");

        //授权过滤器
//        filterMap.put("/add", "perms[user:add]");
//        filterMap.put("/update", "perms[user:update]");

        filterMap.put("/**", "authc");

        //修改调整的登录页面 -- 拦截后跳转
        shiroFilterFactoryBean.setLoginUrl("/unLogin");
        //设置未授权跳转页面
//        shiroFilterFactoryBean.setUnauthorizedUrl("/unAuth");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);
        System.out.println("shiroConfig");

        return shiroFilterFactoryBean;
    }

    /*
     * 创建 DefaultWebSecrityManager
     */
//    @Bean( name = "securityManager")
//    public DefaultWebSecurityManager getDefaultWebSecurityManager(@Qualifier("userRealm") UserRealm userRealm){
//        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
//        //关联realm
//        securityManager.setRealm(userRealm);
//        return securityManager;
//    }
//
//    /*
//     * 引入自定义realm
//     */
//    @Bean(name = "userRealm")
//    public UserRealm getRealm(){
//        return new UserRealm();
//    }


    //1.创建realm
    @Bean
    public UserRealm userRealm() {
        return new UserRealm();
    }

//    @Bean
//    public IhrmRealm ihrmRealm() {
//        return new UserRealm();
//    }
//
//    //2.创建安全管理器
    @Bean
    public SecurityManager securityManager(UserRealm realm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
//        securityManager.setRealm(realm);
        securityManager.setRealm(userRealm());

        //将自定义的会话管理器注册到安全管理器中
        securityManager.setSessionManager(sessionManager());
        //将自定义的redis缓存管理器注册到安全管理器中
//        securityManager.setCacheManager(cacheManager());

        return securityManager;
    }
//
//    //3.配置shiro的过滤器工厂
//
//    /**
//     * 再web程序中，shiro进行权限控制全部是通过一组过滤器集合进行控制
//     *
//     */
//    @Bean
//    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
//        //1.创建过滤器工厂
//        ShiroFilterFactoryBean filterFactory = new ShiroFilterFactoryBean();
//        //2.设置安全管理器
//        filterFactory.setSecurityManager(securityManager);
//        //3.通用配置（跳转登录页面，未授权跳转的页面）
//        filterFactory.setLoginUrl("http://localhost:8080/");//跳转url地址
////        filterFactory.setUnauthorizedUrl("/autherror?code=2");//未授权的url
//        //4.设置过滤器集合
//        Map<String,String> filterMap = new LinkedHashMap<>();
//        //anon -- 匿名访问
//        filterMap.put("/login","anon");
//        filterMap.put("/register","anon");
//        filterMap.put("/validateCode","anon");
//        filterMap.put("/logout","logout");
//        //注册
//        //authc -- 认证之后访问（登录）
//        filterMap.put("/**","authc");
//        //perms -- 具有某中权限 (使用注解配置授权)
//        filterFactory.setFilterChainDefinitionMap(filterMap);
//
//        return filterFactory;
//    }
//
    @Bean
    public SimpleCookie cookie() {
        //  cookie的name,对应的默认是 JSESSIONID
        SimpleCookie cookie = new SimpleCookie("SHAREJSESSIONID");
        cookie.setHttpOnly(true);
        //  path为 / 用于多个系统共享JSESSIONID
        cookie.setPath("/");
        return cookie;
    }
////    //自定义sessionManager
////    @Bean
////    public SessionManager sessionManager() {
////        CustomSessionManager sessionManager = new CustomSessionManager();
//////        mySessionManager.setSessionDAO(redisSessionDAO());
//////        //将修改的cookie放入sessionManager中
//////        mySessionManager.setSessionIdCookie(cookieDAO());
////
////
////        // 设置session超时
////        sessionManager.setGlobalSessionTimeout(60*60 * 1000);
////        // 删除无效session
////        sessionManager.setDeleteInvalidSessions(true);
////        // 设置JSESSIONID
////        sessionManager.setSessionIdCookie(cookie());
////        // 设置sessionDAO
////        sessionManager.setSessionDAO(redisSessionDAO());
////        return sessionManager;
////    }
//
//
//    @Value("${spring.redis.host}")
//    private String host;
//    @Value("${spring.redis.port}")
//    private int port;
//
//    /**
//     * 1.redis的控制器，操作redis
//     */
////    public RedisManager redisManager() {
////        RedisManager redisManager = new RedisManager();
////        redisManager.setHost(host);
////        redisManager.setPort(port);
////        redisManager.setDatabase(1);// 配置缓存过期时间
////        return redisManager;
////    }
//
//    /**
//     * 2.sessionDao
//     */
////    public RedisSessionDAO redisSessionDAO() {
////        RedisSessionDAO sessionDAO = new RedisSessionDAO();
////        sessionDAO.setRedisManager(redisManager());
////        return sessionDAO;
////    }
//
//    /**
//     * 3.会话管理器
//     */
    public DefaultWebSessionManager sessionManager() {
        CustomSessionManager sessionManager = new CustomSessionManager();
//        sessionManager.setSessionDAO(redisSessionDAO());
        sessionManager.setSessionDAO(new EnterpriseCacheSessionDAO());

        sessionManager.setGlobalSessionTimeout(1800000);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionValidationSchedulerEnabled(true);
//        sessionManager.setSessionValidationScheduler(sessionValidationScheduler);
        //禁用cookie
//        sessionManager.setSessionIdCookieEnabled(false);
        sessionManager.setSessionIdCookieEnabled(true);
        //禁用url重写   url;jsessionid=id
//        sessionManager.setSessionIdUrlRewritingEnabled(false);
        sessionManager.setSessionIdCookie(cookie());
        return sessionManager;
    }
//
//    /**
//     * 4.缓存管理器
//     */
//    public RedisCacheManager cacheManager() {
//        RedisCacheManager redisCacheManager = new RedisCacheManager();
//        redisCacheManager.setRedisManager(redisManager());
//        return redisCacheManager;
//    }

    //开启aop注解
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
