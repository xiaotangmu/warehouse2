package com.tan.warehouse2.controller;

import com.tan.warehouse2.bean.Msg;
import com.tan.warehouse2.bean.MySchedulerJob;
import com.tan.warehouse2.schedule.SchedulerJob2;
import com.tan.warehouse2.service.SchedulerService;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @date: 2020-05-08 09:55:49
 * @author: Tan.WL
 */
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    @Resource(name = "multitaskScheduler")
    private Scheduler scheduler;

    @Autowired
    SchedulerService schedulerService;

    @PostMapping("/getAll")
    public Object getAll(){
        List<MySchedulerJob> jobs = schedulerService.getAll();
        return Msg.success(jobs);
    }

    //关闭任务/删除任务
    @PostMapping("/closeTask")
    @RequiresPermissions("scheduler:close")
    public Object closeTask(String jobName) throws SchedulerException {
        if(StringUtils.isBlank(jobName)){
            return Msg.failError("提交的数据有误！");
        }

        //更新
        JobKey jobKey = new JobKey(jobName, "group1");
        JobDetail jobDetail2 = scheduler.getJobDetail(jobKey);
        if (jobDetail2 != null){
            scheduler.deleteJob(jobKey);
            System.out.println("关闭定时任务：" + jobName);
            //更新数据
            MySchedulerJob job = new MySchedulerJob();
            job.setJobName(jobName);
            job.setStatus("0");
            schedulerService.update(job);
        }

        return Msg.success(jobName);
    }

    @PostMapping("/setTask")
    @RequiresPermissions("schuduler:set")
    public Object task2(MySchedulerJob mySchedulerJob) throws SchedulerException {
        if(mySchedulerJob == null){
            return Msg.failError("提交的数据有误！");
        }
        System.out.println(mySchedulerJob);

        String jobName = mySchedulerJob.getJobName();
        String limit = mySchedulerJob.getLimit();
        String year = mySchedulerJob.getYear();
        String month = mySchedulerJob.getMonth();
        String date = mySchedulerJob.getDate();
        String time = mySchedulerJob.getTime();

        if(StringUtils.isBlank(jobName) || StringUtils.isBlank(limit)){
            return Msg.failError("提交的数据有误");
        }
        if(StringUtils.isBlank(year) && StringUtils.isBlank(month) && StringUtils.isBlank(date) && StringUtils.isBlank(time)){
            return Msg.failError("提交的数据有误");
        }


        //配置定时任务对应的Job，这里执行的是ScheduledJob类中定时的方法
        JobDetail jobDetail = JobBuilder
                .newJob(SchedulerJob2.class)
                .usingJobData("jobName",jobName)
                .withIdentity(jobName, "group1")
                .build();

        //更新
        JobKey jobKey = new JobKey(jobName, "group1");
        JobDetail jobDetail2 = scheduler.getJobDetail(jobKey);
        if (jobDetail2 != null){
            scheduler.deleteJob(jobKey);
        }

        List<String> timeStr = new ArrayList<>();
        if(StringUtils.isBlank(time)){
            timeStr.add("0");
            timeStr.add("0");
            timeStr.add("0");
        }else {
            List<String> strings = MyStrUtil.strSplit(time, ":");
            timeStr.addAll(strings);
        }

        if(StringUtils.isBlank(year)){
            year = "*";
        }
        if(StringUtils.isBlank(month)){
            month = "*";
        }
        if(StringUtils.isBlank(date)){
            date = "*";
        }

        String cronStr = timeStr.get(2) + " " + timeStr.get(1) + " " + timeStr.get(0) + " " +
                date + " " + month + " " + "? " + year;

        System.out.println(cronStr);
        mySchedulerJob.setCronStr(cronStr);
        mySchedulerJob.setStatus("1");

        //不能设置过期时间 -- the given trigger 'group1.trigger2out' will never fire.
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronStr);

        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger2"+jobName, "group1")
                .withSchedule(scheduleBuilder)
                .build();

        scheduler.scheduleJob(jobDetail,cronTrigger);

        //将数据保存到数据库
        schedulerService.update(mySchedulerJob);

        return Msg.success(jobName);
    }

    //获取所有在线job
    @PostMapping("/jobs")
    public Object Jobs() throws SchedulerException {

        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());

        //获取所有的job集合
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());

        //可以在这进行线上任务和数据库任务匹配操作，确保该进行的活动进行活动

        return Msg.success(jobKeys);
    }


}
