package com.tan.warehouse2.service.impl;

import com.tan.warehouse2.bean.MySchedulerJob;
import com.tan.warehouse2.mapper.SchedulerMapper;
import com.tan.warehouse2.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Description:
 * @date: 2020-05-08 18:26:49
 * @author: Tan.WL
 */
@Service
public class SchedulerServiceImpl implements SchedulerService {

    @Autowired
    SchedulerMapper schedulerMapper;

    @Override
    public List<MySchedulerJob> getAll() {
        return schedulerMapper.getAll();
    }

    @Override
    public MySchedulerJob getSchedulerByJobName(String s) {
//        System.out.println(s);

        List<MySchedulerJob> list = schedulerMapper.getSchedulerByJobName(s);
//        System.out.println(list);
        if(list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public void update(MySchedulerJob mySchedulerJob) {

        schedulerMapper.updateScheduler(mySchedulerJob);
    }
}
