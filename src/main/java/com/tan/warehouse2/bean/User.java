package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ums_user_info")
public class User {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;
    private String nickname;//昵称
//    private String password;
    private String phone;
    private String email;
    private String photo;
    @Column(name = "active_id")
    private Integer activeId;

    private String gender;

    @Transient
    private List<Role> roles;

}
