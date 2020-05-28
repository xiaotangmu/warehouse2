package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * @Description:
 * @date: 2020-04-09 09:34:20
 * @author: Tan.WL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "bmms_warehouse")
public class Warehouse {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;
    private String location;//位置信息
    private String description;

    @Transient
    private List<Product> products;
}
