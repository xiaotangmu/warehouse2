package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
public class Catalog {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    private String name;
    private String pId;//用于前端ztree识别父子关系
    private String ztreeId;
    private Integer catalog1Id;
    private Integer level;//分类等级
    private boolean leaf = false;

    private List<Catalog> list;

}
