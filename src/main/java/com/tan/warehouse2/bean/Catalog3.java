package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

/**
 * @Description:
 * @date: 2020-04-09 20:29:51
 * @author: Tan.WL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "bmms_catalog3")
public class Catalog3 {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;

    @Column(name = "catalog2_id")
    private Integer catalog2Id;
    @Column(name = "p_id")
    private String pId;
    @Column(name = "ztree_id")
    private String ztreeId;

    @Column(name = "catalog1_id")
    private Integer catalog1Id;
    private Integer level;

    @Transient
    private boolean leaf = false;

    @Transient
    private List<Product> products;

}
