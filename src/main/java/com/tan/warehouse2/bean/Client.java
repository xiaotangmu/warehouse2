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
@Table(name = "bmms_client")
public class Client implements Serializable {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;//客户名字
    private String address;//具体地址
    private String phone;//联系电话
    private String city;//所在城市
    private String postcode;//邮编
    private String contact;//联系电话
    private String email;//邮箱
    @Transient
    private List<Product> products;

}
