package com.tan.warehouse2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MyPage<T> {//用来封装redis 分页数据

    private int pageNum;//当前页码
    private int pageSize;//每页记录数
    private long total;//记录总数
    private int pages;//总页数
    private int size;//当前页记录数
    private int prePage;//上一页
    private int nextPage;//下一页
    private boolean isFirstPage;//是否是第一页
    private boolean isLastPage;//是否最后一页
    private boolean hasPreviousPage;//是否有上一页
    private boolean hasNextPage;//是否有下一页
    private int navigatePages;//导航页码个数
    private int[] navigatepageNums;//导航页码集
    private int navigateFirstPage;//第一个导航页码
    private int navigateLastPage;//最后一个导航页码

    private Set<T> list;//分页数据

}
