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
 * @date: 2020-04-21 09:55:51
 * @author: Tan.WL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "sms_sku")
public class Sku {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;

    @Column(name = "spu_id")
    private Integer spuId;

    @Column(name = "catalog3_id")
    private Integer catalog3Id;
    @Column(name = "brand_id")
    private Integer brandId;

    private BigDecimal price;//单价

    private String address;//货架/位置信息

    private String unit;//单位

    @Column(name = "brand_name")
    private String brandName;
    @Column(name = "catalog_name")
    private String catalogName;

    private Integer num;

    @Column(name = "warehouse_id")
    private Integer warehouseId;
    @Column(name = "warehouse_name")
    private String warehouseName;

    private String description;//用作备注

    @Transient
    private List<BaseAttr> baseAttrs;

    @Column(name = "attr_value_str")
    private String attrValueStr;//用作判断是否该规格已经存在

    @Column(name = "alarm_value")
    private Integer alarmValue;

    @Transient
    private BigDecimal totalPrice;

    //用于盘点
    //账面数据
    @Transient
    private Integer accountNum;
    @Transient
    private BigDecimal accountPrice;
    @Transient
    private BigDecimal accountTotalPrice;
    //盘点数据
    @Transient
    private Integer checkNum;
    @Transient
    private BigDecimal checkTotalPrice;
    //相差
    @Transient
    private Integer differenceNum;
    @Transient
    private BigDecimal differencePrice;
    @Transient
    private String remark;

}
