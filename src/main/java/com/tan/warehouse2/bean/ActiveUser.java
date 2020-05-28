package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ums_user")
public class ActiveUser implements Serializable {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;
    private String password;

}
