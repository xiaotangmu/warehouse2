package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.SkuEdit; /**
 * @Description:
 * @date: 2020-05-08 21:30:09
 * @author: Tan.WL
 */
public interface SkuEditService {

    void insert(SkuEdit skuEdit1);

    PageInfo<SkuEdit> getAllEdit(Integer pageNum, Integer pageSize);

    PageInfo<SkuEdit> getSkuEditByCondition(String limit, Integer warehouseId, String skuEditDate, Integer pageNum, Integer pageSize);

    int updateEditRemark(SkuEdit editRemark);

    void deleteRecord(String limit);
}
