package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
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
@Table(name = "sms_entry")
public class Entry implements Serializable{

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    @Column(name = "entry_num")
    private String entryNum;//单号

    @Column(name = "entry_date")
    private String entryDate;

    private String operator;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "warehouse_id")
    private Integer warehouseId;
    @Column(name = "warehouse_name")
    private String warehouseName;

    private Integer batch;//批次

    @Column(name = "supplier_id")
    private Integer supplierId;
    @Column(name = "supplier_name")
    private String supplierName;
    @Column(name = "description")
    private String description;

    @Transient
    private List<Sku> productList;


}
