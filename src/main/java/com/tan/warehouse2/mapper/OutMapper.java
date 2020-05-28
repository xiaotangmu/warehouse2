package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Out;
import com.tan.warehouse2.bean.Sku;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @date: 2020-04-30 09:15:29
 * @author: Tan.WL
 */
public interface OutMapper extends Mapper<Out> {

    int insertRelation(@Param("id") Integer id, @Param("skus") List<Sku> skus);

    List<Out> getOutByCondition(Out out);
    List<Out> getOutByOutNum(@Param("outNum") String outNum);

    List<Sku> getSkusById(@Param("id") Integer id);

    List<Out> getAll();

//    List<Out> getOutByDateWarehouse(String outDate, Integer warehouseId);


    void deleteByLimit(@Param("limit") String limit);

    List<Integer> findOutIdByLimit(@Param("limit") String limit);

    void deleteRelationByIds(@Param("ids") List<Integer> ids);

    void deleteDeliveryByOutIds(@Param("outIds") List<Integer> outIds);

    List<Out> getOutByConditionForm(@Param("out") Out out, @Param("limit") String limit);
}
