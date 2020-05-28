package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @date: 2020-04-29 15:04:19
 * @author: Tan.WL
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "sms_out")
public class Out implements Serializable{

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name = "out_num")
    private String outNum;//单号

    @Column(name = "out_date")
    private String outDate;

    private String operator;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "warehouse_id")
    private Integer warehouseId;
    @Column(name = "warehouse_name")
    private String warehouseName;

    private Integer batch;//批次

    @Column(name = "client_id")
    private Integer clientId;
    @Column(name = "client_name")
    private String clientName;
    @Column(name = "description")
    private String description;

    @Transient
    private List<Sku> productList;


}
