package com.tan.warehouse2.schedule;

import com.tan.warehouse2.bean.MySchedulerJob;
import com.tan.warehouse2.bean.SkuEdit;
import com.tan.warehouse2.mapper.SchedulerMapper;
import com.tan.warehouse2.mapper.SkuEditMapper;
import com.tan.warehouse2.service.*;
import com.tan.warehouse2.service.impl.SchedulerServiceImpl;
import com.tan.warehouse2.utils.MyStrUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @date: 2020-05-08 09:54:35
 * @author: Tan.WL
 */
public class SchedulerJob2 implements Job {

    @Autowired
    SchedulerService schedulerService;

    @Autowired
    SkuEditService skuEditService;

    @Autowired
    EntryService entryService;
    @Autowired
    OutService outService;
    @Autowired
    CheckService checkService;
    @Autowired
    NotificationService notificationService;

//    SchedulerService schedulerService = new SchedulerServiceImpl();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        //这里可以获取控制器绑定的值，实际应用中可以设置为某个活动的id,以便进行数据库操作
        Object jobName = jobExecutionContext.getJobDetail().getKey();

//        System.out.println("toString...");
//        System.out.println(jobName.toString());//group1.entry

        String str = jobName.toString();
//        System.out.println(str);
        List<String> strings = MyStrUtil.strSplit(str, "group1.");//直接用. 分割失败 -- []
        System.out.println(strings);//[, entry] -- 前面数据为空字符串
//        System.out.println(strings.get(0));//'' 空字符串
//        System.out.println(strings.get(1));
        if(strings.size() < 2){
            return;
        }
//        System.out.println("hello");
//        System.out.println(schedulerService);
        String strName = strings.get(1);
        //从数据库中获取数据
        MySchedulerJob schedulerJob = schedulerService.getSchedulerByJobName(strName);
        System.out.println(schedulerJob);

        if(schedulerJob == null){
            return;
        }

        System.err.println("这是"+jobName+"任务"+new Date());
//        System.err.println(strName);
        strName = strName.trim();

        if(strName.equals("entry")){
            System.err.println(strName);

            //入库表删除
            entryService.deleteByLimit(schedulerJob.getLimit());

        }else if(strName.equals("out")){
            System.err.println(strName);
            //出库表和送货表删除
            outService.deleteByLimit(schedulerJob.getLimit());

        }else if(strName.equals("check")){
            System.err.println(strName);
            //盘点表删除
            checkService.deleteByLimit(schedulerJob.getLimit());

        }else if(strName.equals("notification")){
            System.err.println(strName);
            //消息表删除
            notificationService.deleteByLimit(schedulerJob.getLimit());

        }else if(strName.equals("record")){
            System.err.println(strName);

            String limit = schedulerJob.getLimit();
            System.out.println(limit);
            skuEditService.deleteRecord(limit);

        }
    }
}
