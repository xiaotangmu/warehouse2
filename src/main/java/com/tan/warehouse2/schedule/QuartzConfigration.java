package com.tan.warehouse2.schedule;

import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;

/**
 * @Description:
 * @date: 2020-05-08 09:53:20
 * @author: Tan.WL
 */
@Configuration
public class QuartzConfigration {

    //单任务类型
    @Bean(name = "jobDetail")
    public MethodInvokingJobDetailFactoryBean detailFactoryBean(SchedulerTask task) {
        // ScheduleTask为需要执行的任务
        MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
        /*
         *  是否并发执行
         *  例如每5s执行一次任务，但是当前任务还没有执行完，就已经过了5s了，
         *  如果此处为true，则下一个任务会bing执行，如果此处为false，则下一个任务会等待上一个任务执行完后，再开始执行
         */
        jobDetail.setConcurrent(true);

        jobDetail.setName("scheduler");// 设置任务的名字
        jobDetail.setGroup("scheduler_group");// 设置任务的分组，这些属性都可以存储在数据库中，在多任务的时候使用

        /*
         * 这两行代码表示执行task对象中的scheduleTest方法。定时执行的逻辑都在scheduleTest。
         */
        jobDetail.setTargetObject(task);

        jobDetail.setTargetMethod("start");
        return jobDetail;
    }
    @Bean(name = "jobTrigger")
    public CronTriggerFactoryBean cronJobTrigger(MethodInvokingJobDetailFactoryBean jobDetail) {
        CronTriggerFactoryBean tigger = new CronTriggerFactoryBean();
        tigger.setJobDetail(jobDetail.getObject());

        LocalDateTime dt = LocalDateTime.now();
        int year = dt.getYear();
        int monthValue = dt.getMonthValue();
        int dayOfMonth = dt.getDayOfMonth();
        int hour = dt.getHour();
        int minute = dt.getMinute();
        int second = dt.getSecond();

        int minute2 = minute + 2;//两分钟后执行
        if(minute >= 58){
            minute2 = 0;
            hour++;
        }
        String cronStr = second + " " + minute2 + " " + hour + " " + dayOfMonth + " " + monthValue + " " + "? "+ year;

        System.err.println(cronStr);
        tigger.setCronExpression(cronStr);// 表示每隔2秒钟执行一次
        //tigger.set
        tigger.setName("myTigger");// trigger的name
        return tigger;
    }
    @Bean(name = "scheduler")
    public SchedulerFactoryBean schedulerFactory(Trigger cronJobTrigger) {
        SchedulerFactoryBean bean = new SchedulerFactoryBean();
        //设置是否任意一个已定义的Job会覆盖现在的Job。默认为false，即已定义的Job不会覆盖现有的Job。
        bean.setOverwriteExistingJobs(true);
        // 延时启动，应用启动5秒后  ，定时器才开始启动
        bean.setStartupDelay(5);
        // 注册定时触发器
        bean.setTriggers(cronJobTrigger);
        return bean;
    }




    //多任务时的Scheduler，动态设置Trigger。一个SchedulerFactoryBean可能会有多个Trigger
    @Bean(name = "multitaskScheduler")
    public SchedulerFactoryBean schedulerFactoryBean(){
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        //启动时更新己存在的Job，这样就不用每次修改targetObject后删除qrtz_job_details表对应记录了
        factory.setOverwriteExistingJobs(true);
        // 延时启动(秒)
        factory.setStartupDelay(5);
        factory.setJobFactory(jobFactory);
        return factory;
    }


    private JobFactory jobFactory;

    public QuartzConfigration(JobFactory jobFactory){
        this.jobFactory = jobFactory;
    }

    /**
     * 配置SchedulerFactoryBean
     *
     * 将一个方法产生为Bean并交给Spring容器管理
     */
//    @Bean
//    public SchedulerFactoryBean schedulerFactoryBean() {
//        // Spring提供SchedulerFactoryBean为Scheduler提供配置信息,并被Spring容器管理其生命周期
//        SchedulerFactoryBean factory = new SchedulerFactoryBean();
//        // 设置自定义Job Factory，用于Spring管理Job bean
//        factory.setJobFactory(jobFactory);
//        return factory;
//    }

//    @Bean(name = "scheduler")
//    public Scheduler scheduler() {
//        return schedulerFactoryBean().getScheduler();
//    }
}
