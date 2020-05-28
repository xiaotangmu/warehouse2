package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name ="wms_notification")
public class Notification implements Serializable {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String title;
    private String content;
    private String createTime;


    @Transient
    private List<User> users;
    @Transient
    private Integer userId;
    @Transient
    private String status;
}
