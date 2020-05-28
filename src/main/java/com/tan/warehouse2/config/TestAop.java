package com.tan.warehouse2.config;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

//@Aspect
//@Component
public class TestAop {
    @Pointcut("execution(public * com.tan.warehouse2.controller.*.*(..))")
    public void log(){}

    @Before("log()")
    public void initBefore(){
        System.out.println("initBefore....");
    }
    @After("log()")
    public void initAfter(){
        System.out.println("initAfter....");
    }

    //测试对自身类方法切面 -- 无效
//    @Pointcut("execution(public * com.tan.warehouse.config.TestAop.*(..)) && !execution(* com.tan.warehouse.config.TestAop.init*(..))")
//    public void log(){}
//
//    @Before("log()")
//    public void initBefore(){
//        System.out.println("initBefore....");
//    }
//    @After("log()")
//    public void initAfter(){
//        System.out.println("initAfter....");
//    }
//
//    public void test(){
//        System.out.println("test ....");
//    }

}
