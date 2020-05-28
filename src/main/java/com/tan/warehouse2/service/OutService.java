package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Out;
import com.tan.warehouse2.bean.Sku;

import java.util.List;

public interface OutService {


    int add(Out e);

    Out checkBatchByOutNum(String outNum);

    PageInfo<Out> checkByCondition(Out out, Integer pageNum, Integer pageSize);

    PageInfo<Out> getAllPage(Integer pageNum, Integer pageSize);

    int description(Out out);

    List<Out> getOutByDateWarehouse(String outDate, Integer warehouseId);

    List<Sku> getSkus(Integer outId);

    //出库和送货表删除
    void deleteByLimit(String limit);

    PageInfo<Out> checkByConditionForm(Out out, Integer pageNum, Integer pageSize, String limit);
}
