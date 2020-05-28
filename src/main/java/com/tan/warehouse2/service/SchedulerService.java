package com.tan.warehouse2.service;

import com.tan.warehouse2.bean.MySchedulerJob;

import java.util.List;

/**
 * @Description:
 * @date: 2020-05-08 18:26:35
 * @author: Tan.WL
 */
public interface SchedulerService {

    void update(MySchedulerJob mySchedulerJob);

    MySchedulerJob getSchedulerByJobName(String s);

    List<MySchedulerJob> getAll();
}
