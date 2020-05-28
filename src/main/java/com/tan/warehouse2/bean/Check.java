package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @date: 2020-05-07 17:23:54
 * @author: Tan.WL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "sms_check")
public class Check {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name = "check_sn")
    private String checkSn;
    private String operator;

    @Column(name = "warehouse_name")
    private String warehouseName;
    @Column(name = "warehouse_id")
    private Integer warehouseId;
    private Integer batch;
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    @Column(name = "check_date")
    private String checkDate;

    @Transient
    private List<Sku> productList;
}
