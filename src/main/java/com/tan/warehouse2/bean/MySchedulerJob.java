package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @Description:
 * @date: 2020-05-08 15:34:16
 * @author: Tan.WL
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "sms_scheduler")
public class MySchedulerJob implements Serializable{

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    @Column(name = "job_name")
    private String jobName;

    private String year;
    private String month;
    private String date;
    private String time;
    private String limit;

    private String status;//'0' -- 未开启定时任务， '1' --开启了定时任务

    @Column(name = "cron_str")
    private String cronStr;

}
