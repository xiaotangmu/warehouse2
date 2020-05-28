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
@Table(name = "bmms_catalog1")
public class Catalog1 {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;
    @Column(name = "p_id")
    private String pId;//用于前端ztree识别父子关系
    @Column(name = "ztree_id")
    private String ztreeId;
    private Integer level;

    @Transient
    private boolean leaf = false;

    @Transient
    private List<Catalog2> list;

}
