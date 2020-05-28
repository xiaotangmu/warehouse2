package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

/**
 * @Description:
 * @date: 2020-04-12 16:25:01
 * @author: Tan.WL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table( name = "ums_authority")
public class Authority {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    @Column(name = "auth_name")
    private String authName;
    private String description;
    private String icon;//图标
    private String resource;
    private Integer level;//等级
    @Column(name = "p_id")
    private Integer pId;

    @Transient
    private boolean leaf = false;

    @Transient
    private List<Authority> list;
}
