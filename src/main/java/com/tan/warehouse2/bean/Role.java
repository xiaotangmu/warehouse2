package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.List;

/**
 * @Description:
 * @date: 2020-04-25 14:05:01
 * @author: Tan.WL
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name="ums_role")
public class Role {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    @Column(name = "role_name")
    private String roleName;
    private String description;

    @Transient
    private List<Authority> authorities;
}
