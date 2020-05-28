package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Check;
import com.tan.warehouse2.bean.Out;
import com.tan.warehouse2.bean.Sku;

import java.util.List;

/**
 * @Description:
 * @date: 2020-05-07 17:30:53
 * @author: Tan.WL
 */
public interface CheckService {

    int add(Check e);

    Check checkBatchByCheckSn(String outNum);

    PageInfo<Check> checkByCondition(Check out, Integer pageNum, Integer pageSize);

    PageInfo<Check> getAllPage(Integer pageNum, Integer pageSize);

    int description(Check out);

    List<Check> getOutByDateWarehouse(String outDate, Integer warehouseId);

    List<Sku> getSkus(Integer outId);

    void remark(Integer checkId, Integer skuId, String remark);

    void deleteByLimit(String limit);

    PageInfo<Check> checkByConditionForm(Check check, Integer pageNum, Integer pageSize, String limit);
}
