package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Delivery;
import com.tan.warehouse2.bean.Out;
import com.tan.warehouse2.bean.Sku;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @date: 2020-05-06 10:36:17
 * @author: Tan.WL
 */
public interface DeliveryMapper extends Mapper<Delivery> {


    List<Delivery> getDeliveryByCondition(Delivery delivery);
    List<Delivery> getDeliveryByDeliveryNum(@Param("deliveryNum") String deliveryNum);

    List<Delivery> getAll();

    int updateDesc(Delivery delivery);

    List<Delivery> getDeliveryByConditionForm(@Param("delivery") Delivery delivery, @Param("limit") String limit);
}
