package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Check;
import com.tan.warehouse2.bean.Check;
import com.tan.warehouse2.bean.Sku;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @date: 2020-05-07 17:32:02
 * @author: Tan.WL
 */
public interface CheckMapper extends Mapper<Check> {

    void insertRelation(@Param("id") Integer id, @Param("skus") List<Sku> skus);

    List<Check> getCheckByCondition(Check check);

    List<Check> getCheckByCheckSn(@Param("checkSn") String checkSn);

    List<Sku> getSkusById(@Param("id") Integer id);

    List<Check> getAll();

    void remark(@Param("checkId") Integer checkId, @Param("skuId") Integer skuId, @Param("remark") String remark);

    void deleteByLimit(@Param("limit") String limit);

    List<Integer> findCheckIdByLimit(@Param("limit") String limit);

    void deleteRelationByIds(@Param("ids") List<Integer> ids);

    List<Check> getCheckByConditionForm(@Param("check") Check check, @Param("limit") String limit);
}
