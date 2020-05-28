package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @Description:
 * @date: 2020-05-08 21:04:09
 * @author: Tan.WL
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "sms_sku_edit")
public class SkuEdit {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;
    @Column(name = "sku_id")
    private Integer skuId;
    @Column(name = "warehouse_id")
    private Integer warehouseId;
    @Column(name = "warehouse_name")
    private String warehouseName;
    @Column(name = "catalog_name")
    private String catalogName;
    @Column(name = "brand_name")
    private String brandName;
    private String unit;
    @Column(name = "attr_value_str")
    private String attrValueStr;

    private Integer num;
    private BigDecimal price;
    @Column(name = "alarm_value")
    private Integer alarmValue;
    private String description;
    private String address;

    @Column(name = "new_num")
    private Integer newNum;
    @Column(name = "new_price")
    private BigDecimal newPrice;
    @Column(name = "new_alarm_value")
    private Integer newAlarmValue;
    @Column(name = "new_desc")
    private String newDesc;
    @Column(name = "new_address")
    private String newAddress;

    private String remark;
    private String type;//0编辑 1 删除
    @Column(name = "edit_date")
    private String editDate;
    private String operator;
}
