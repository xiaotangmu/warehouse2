package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.MySchedulerJob;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @date: 2020-05-08 18:25:02
 * @author: Tan.WL
 */
public interface SchedulerMapper extends Mapper<MySchedulerJob> {

    List<MySchedulerJob> getAll();

    void updateScheduler( MySchedulerJob mySchedulerJob);

    List<MySchedulerJob> getSchedulerByJobName(String jobName);
}
