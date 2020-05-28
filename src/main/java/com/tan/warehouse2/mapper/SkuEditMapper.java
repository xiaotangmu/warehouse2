package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.SkuEdit;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @date: 2020-05-08 21:29:31
 * @author: Tan.WL
 */
public interface SkuEditMapper extends Mapper<SkuEdit> {

    List<SkuEdit> getAllEdit();
    List<SkuEdit> getSkuEditByCondition(@Param("limit") String limit, @Param("warehouseId") Integer warehouseId);
    List<SkuEdit> getSkuEditByCondition2(@Param("warehouseId") Integer warehouseId, @Param("skuEditDate") String skuEditDate);

    void deleteRecord(@Param("limit") String limit);//有if test等一定要Param 或者 对象的属性 getter
}
