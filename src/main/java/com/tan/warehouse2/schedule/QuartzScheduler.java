package com.tan.warehouse2.schedule;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * @Description:
 * @date: 2020-05-08 10:56:25
 * @author: Tan.WL
 */

//@Configuration
public class QuartzScheduler {

//    @Autowired
//    private Scheduler scheduler;
//    @Autowired
//    private NeTaskService neTaskService;
//
//    /**
//     * 开始执行所有任务
//     */
//    public void startJob() throws SchedulerException {
//        //启动时进行查库将任务添加至job
//
//        List<NeTask> netaskList = neTaskService.findAllByNeIdAndInUse();
//        List<NeTask> zctaskList = neTaskService.findAllByIpAndInUse();
//
//        //添加任务至调度器scheduler
//        startJob1(scheduler,netaskList);
//        //调度任务开始执行
//        //		scheduler.start();
//        startJob2(scheduler,zctaskList);
//        scheduler.start();
//    }
//
//    /*
//     * 重启所有任务
//     */
//    public void restartJob() throws SchedulerException, InterruptedException {
//        //不可以用shutdown，也不需要停止，直接清除，然后启动
//        //		scheduler.shutdown();
//        //		scheduler.pauseAll();
//        scheduler.clear();
//        this.startJob();
//    }
//
//    /**
//     * title:
//     * mentality:
//     * @throws
//     * @param scheduler2
//     * @param zctaskList
//     */
//    private void startJob2(Scheduler scheduler2, List<NeTask> zctaskList) throws SchedulerException{
//        // TODO Auto-generated method stub
//
//
//    }
//
//    /**
//     * title:计划任务1
//     * mentality:
//     * @throws
//     * @param scheduler2
//     * @param netaskList
//     */
//    private void startJob1(Scheduler scheduler2, List<NeTask> netaskList) throws SchedulerException{
//        // TODO Auto-generated method stub
//        // 通过JobBuilder构建JobDetail实例，JobDetail规定只能是实现Job接口的实例
//        // JobDetail 是具体Job实例
//        for(NeTask netask : netaskList){
//
//            JobDetail jobDetail = JobBuilder.newJob(NeTaskJob.class)//不同的业务，增加不同的.class
//                    .withIdentity(netask.getId().toString(), netask.getStationId()+netask.getNeId())
//                    .build();
//            jobDetail.getJobDataMap().put("id",netask);
//            List<Cron> cronList = CronUtil.getCronByTask(netask);
////			for(Cron cron : cronList ){
//            for(int i = 0;i< cronList.size();i++){
//                // 基于表达式构建触发器
//                CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(
//                        //下面的cron你可以直接写个cron表达式来做验证,入：每隔5秒执行一次：*/5 * * * * ?
//                        cronList.get(i).getCron()
//                );
//
//                // CronTrigger表达式触发器 继承于Trigger
//                // TriggerBuilder 用于构建触发器实例
//                CronTrigger cronTrigger = TriggerBuilder.newTrigger()
//                        //若一个jobdetail有多个trigger，则需要注意命名规则，便于后面修改任务
////						.withIdentity(netask.getNeId().toString(), netask.getStationId())
//                        .forJob(jobDetail)
//                        .withIdentity(netask.getId() + CronUtil.cronEndId[i], netask.getStationId()+netask.getNeId())
//                        .withSchedule(cronScheduleBuilder).build();
//
//                // scheduleJob该接口的作用是在将任务加入Quartz的同时绑定一个Trigger，Quartz会在加入该任务后自动设置Trigger的JobName与JobGroup为该JobDetail的name与group
//                if(i==0){
//                    scheduler2.scheduleJob(jobDetail, cronTrigger);//第一次必须有jobdetail
//                }else{
//                    scheduler2.scheduleJob(cronTrigger);
//                }
//
//
//                //rescheduleJob(String, String, Trigger)  替换一个指定的Trigger, 即解除指定Trigger与任务的绑定，并将新的Trigger与任务绑定，Quartz会自动调整新Trigger的JobName与JobGroup，而旧的Trigger将被移除
//                //Scheduler#triggerJob(String, String)   创建一个立即触发的Trigger，并将其与name与group指定的任务绑定
//            }
//
//        }
//
//    }
//
//
//    /**
//     * 获取Job信息
//     *
//     * @param name
//     * @param group
//     * @return
//     * @throws SchedulerException
//     */
//    public String getJobInfo(String name, String group) throws SchedulerException {
//        TriggerKey triggerKey = new TriggerKey(name, group);
//        CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
//        return String.format("time:%s,state:%s", cronTrigger.getCronExpression(),
//                scheduler.getTriggerState(triggerKey).name());
//    }
//
//    /**
//     * 修改某个任务的执行时间
//     * (修改的是具体的trigger，不是jobdetail）
//     * @param name
//     * @param group
//     * @param time
//     * @return
//     * @throws SchedulerException
//     */
//    public boolean modifyJob(String name, String group, String time) throws SchedulerException {
//        Date date = null;
//        TriggerKey triggerKey = new TriggerKey(name, group);
//        CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
//        String oldTime = cronTrigger.getCronExpression();
//        if (!oldTime.equalsIgnoreCase(time)) {
//            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(time);
//            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group)
//                    .withSchedule(cronScheduleBuilder).build();
//            date = scheduler.rescheduleJob(triggerKey, trigger);
//        }
//        return date != null;
//    }
//
//    /**
//     * 暂停所有任务
//     *
//     * @throws SchedulerException
//     */
//    public void pauseAllJob() throws SchedulerException {
//        scheduler.pauseAll();
//    }
//
//    /**
//     * 暂停某个任务
//     *
//     * @param name
//     * @param group
//     * @throws SchedulerException
//     */
//    public void pauseJob(String name, String group) throws SchedulerException {
//        JobKey jobKey = new JobKey(name, group);
//        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
//        if (jobDetail == null){
//            return;
//        }
//
//        scheduler.pauseJob(jobKey);
//    }
//
//    /**
//     * 恢复所有任务
//     *
//     * @throws SchedulerException
//     */
//    public void resumeAllJob() throws SchedulerException {
//        scheduler.resumeAll();
//    }
//
//    /**
//     * 恢复某个任务
//     *
//     * @param name
//     * @param group
//     * @throws SchedulerException
//     */
//    public void resumeJob(String name, String group) throws SchedulerException {
//        JobKey jobKey = new JobKey(name, group);
//        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
//        if (jobDetail == null){
//            return;
//        }
//        scheduler.resumeJob(jobKey);
//    }
//
//    /**
//     * 删除某个任务
//     *
//     * @param name
//     * @param group
//     * @throws SchedulerException
//     */
//    public void deleteJob(String name, String group) throws SchedulerException {
//        JobKey jobKey = new JobKey(name, group);
//        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
//        if (jobDetail == null){
//            return;
//        }
//        scheduler.deleteJob(jobKey);
//    }
}