package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Delivery;
import com.tan.warehouse2.bean.Out;

/**
 * @Description:
 * @date: 2020-05-06 10:36:59
 * @author: Tan.WL
 */
public interface DeliveryService {


    int add(Delivery delivery);

    Delivery checkBatchByDeliveryNum(String deliveryNum);

    PageInfo<Delivery> checkByCondition(Delivery out, Integer pageNum, Integer pageSize);

    PageInfo<Delivery> getAllPage(Integer pageNum, Integer pageSize);

    int description(Delivery out);

    PageInfo<Delivery> checkByConditionForm(Delivery delivery, Integer pageNum, Integer pageSize, String limit);
}
