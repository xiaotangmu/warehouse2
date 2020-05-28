package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

/**
 * @Description:
 * @date: 2020-04-18 18:27:25
 * @author: Tan.WL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "bmms_spu")
public class Spu {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;
    private String description;
    @Column(name = "catalog3_id")
    private Integer catalog3Id;
    @Column(name = "brand_id")
    private Integer brandId;

    @Column(name = "brand_name")
    private String brandName;
    @Column(name = "catalog_name")
    private String catalogName;

    @Transient
    private List<BaseAttr> baseAttrs;

    @Transient
    private String remark;//用作盘点备注

}
