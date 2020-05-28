package com.tan.warehouse2.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Sku;
import com.tan.warehouse2.bean.SkuEdit;
import com.tan.warehouse2.mapper.SkuEditMapper;
import com.tan.warehouse2.mapper.SkuMapper;
import com.tan.warehouse2.service.SkuEditService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @date: 2020-05-08 21:30:28
 * @author: Tan.WL
 */
@Service
public class SkuEditServiceImpl implements SkuEditService {

    @Autowired
    SkuEditMapper skuEditMapper;

    @Override
    public void deleteRecord(String limit) {
        skuEditMapper.deleteRecord(limit);
    }

    @Override
    public int updateEditRemark(SkuEdit skuEdit) {

        return skuEditMapper.updateByPrimaryKeySelective(skuEdit);
    }

    @Override
    public PageInfo<SkuEdit> getSkuEditByCondition(String limit, Integer warehouseId, String skuEditDate, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        PageInfo<SkuEdit> pageInfo = null;
        if(StringUtils.isNotBlank(limit)){
            pageInfo = new PageInfo<>(skuEditMapper.getSkuEditByCondition(limit, warehouseId));
        }else{
            pageInfo = new PageInfo<>(skuEditMapper.getSkuEditByCondition2(warehouseId, skuEditDate));
        }


        return pageInfo;
    }

    @Override
    public PageInfo<SkuEdit> getAllEdit(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        PageInfo<SkuEdit> pageInfo = new PageInfo<>(skuEditMapper.getAllEdit());
        return pageInfo;
    }

    @Override
    public void insert(SkuEdit skuEdit1) {
        skuEditMapper.insert(skuEdit1);
    }
}
