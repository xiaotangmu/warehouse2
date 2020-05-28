package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

/**
 * @Description:
 * @date: 2020-04-17 15:49:29
 * @author: Tan.WL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "bmms_base_attr")
public class BaseAttr {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;

    @Column(name = "catalog3_id")
    private Integer catalog3Id;

    @Transient
    private List<String> value;

    @Transient
    private String valueStr;


}
