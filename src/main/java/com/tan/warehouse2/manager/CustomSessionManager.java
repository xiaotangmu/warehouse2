package com.tan.warehouse2.manager;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * @Description:
 * @date: 2020-04-24 15:47:16
 * @author: Tan.WL
 */
public class CustomSessionManager extends DefaultWebSessionManager {

//    /**
//     * 头信息中具有sessionid
//     *      请求头：Authorization： sessionid
//     *
//     * 指定sessionId的获取方式
//     */
//    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
//
//        //获取请求头Authorization中的数据
//        String id = WebUtils.toHttp(request).getHeader("Authorization");
//        if(org.springframework.util.StringUtils.isEmpty(id)) {
//            //如果没有携带，生成新的sessionId
//            return super.getSessionId(request,response);
//        }else{
//            //请求头信息：bearer sessionid
//            id = id.replaceAll("Bearer ","");
//            //返回sessionId；
//            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, "header");
//            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, id);
//            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
//            return id;
//        }
//    }
    private static final String AUTHORIZATION = "Authorization";

    private static final String REFERENCED_SESSION_ID_SOURCE = "cookie";

    public CustomSessionManager() {
        super();
    }

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        String id = WebUtils.toHttp(request).getHeader(AUTHORIZATION);
        //如果请求头中有 Authorization 则其值为sessionId
        if (!StringUtils.isEmpty(id)) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, REFERENCED_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, id);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            return id;
        } else {
            //否则按默认规则从cookie取sessionId
            return super.getSessionId(request, response);
        }
    }

}
