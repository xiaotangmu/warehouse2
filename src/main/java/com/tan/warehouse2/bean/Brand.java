package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "bmms_brand")
public class Brand implements Serializable {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;
    private String description;
    private String logo;

    @Transient
    private List<Product> products;

}
