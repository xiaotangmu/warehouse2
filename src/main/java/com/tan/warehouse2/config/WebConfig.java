package com.tan.warehouse2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description:
 * @date: 2020-04-25 20:16:26
 * @author: Tan.WL
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
//    @Autowired
//    private EnvConfig envConfig;
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new HandlerInterceptor() {
//            @Override
//            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
//                    throws Exception {
//                boolean isTrue = envConfig.getIsDev();//判断是测试服才需要解决跨域问题
//                if (isTrue) {
//                    response.addHeader("Access-Control-Allow-Origin", "*");
//　　　　　　　　　　   response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//                    response.addHeader("Access-Control-Allow-Headers",
//                            "Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,token");
//                }
//                return true;
//            }
//
//            @Override
//            public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
//                                   ModelAndView modelAndView) throws Exception {
//
//            }
//
//            @Override
//            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
//                                        Exception ex) throws Exception {
//            }
//        });
//    }
}