package com.tan.warehouse2.schedule;

import com.tan.warehouse2.bean.MySchedulerJob;
import com.tan.warehouse2.service.SchedulerService;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

//单任务
//<!--三个注释都要加上-->
@Configuration
@Component
@EnableScheduling
public class SchedulerTask {

    @Resource(name = "multitaskScheduler")
    private Scheduler scheduler;

    @Autowired
    SchedulerService schedulerService;

    public void start() throws InterruptedException, SchedulerException {

        //开机获取开启数据库定时任务

        System.err.println("开机两分钟后开启定时任务！！！" + new Date());


        List<MySchedulerJob> jobs = schedulerService.getAll();

        LocalDateTime dt = LocalDateTime.now();
        int year1 = dt.getYear();
        int monthValue1 = dt.getMonthValue();
        int dayOfMonth1 = dt.getDayOfMonth();

        for (MySchedulerJob job : jobs) {
            String jobName = job.getJobName();
            String cronStr = job.getCronStr();
            String status = job.getStatus();
            String year = job.getYear();
            String month = job.getMonth();
            String date = job.getDate();

            //开启了定时任务
            if(status.equals("1")){
                //判断日期时间是否有效
                if(StringUtils.isNotBlank(year) && !year.equals("*")){
                    if(Integer.parseInt(year) < year1){
                        continue;
                    }else if(Integer.parseInt(year) == year1){
                        if(StringUtils.isNotBlank(month) && !year.equals("*")){
                            if(Integer.parseInt(month) < monthValue1){
                                continue;
                            }else if(Integer.parseInt(month) == monthValue1){
                                if(StringUtils.isNotBlank(date) && !date.equals("*")){
                                    if(Integer.parseInt(date) < dayOfMonth1){
                                        continue;
                                    }else if(Integer.parseInt(date) == dayOfMonth1){
                                        //当天的也不要了
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }

                //配置定时任务对应的Job，这里执行的是ScheduledJob类中定时的方法
                JobDetail jobDetail = JobBuilder
                        .newJob(SchedulerJob2.class)
                        .usingJobData("jobName",jobName)
                        .withIdentity(jobName, "group1")
                        .build();

                //不能设置过期时间 -- the given trigger 'group1.trigger2out' will never fire.
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronStr);

                CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                        .withIdentity("trigger2"+jobName, "group1")
                        .withSchedule(scheduleBuilder)
                        .build();

                scheduler.scheduleJob(jobDetail,cronTrigger);
                System.err.println("开启了定时任务：" + jobName);
            }

        }

    }
}
