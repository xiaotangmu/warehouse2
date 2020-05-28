package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;

/**
 * @Description:
 * @date: 2020-05-05 16:01:21
 * @author: Tan.WL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors( chain = true)
@Table(name = "sms_delivery")
public class Delivery {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    @Column(name = "delivery_num")
    private String deliveryNum;//送货单号
    @Column(name = "delivery_date")
    private String deliveryDate;//日期
    @Column(name = "out_num")
    private String outNum;//出货单号
    @Column(name = "out_id")
    private Integer outId;//出货id
    private String operator;//操作员
    private String city;//城市
    private String address;//具体地址
    private String description;//备注

    @Column(name = "client_id")
    private Integer clientId;
    @Column(name = "client_name")
    private String clientName;
    private String contact;//联系人
    private String phone;//联系电话
    private Integer batch;//批次

    @Transient
    private Out out;

    @Transient
    private Client client;
}
