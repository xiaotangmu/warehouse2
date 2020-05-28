package com.tan.warehouse2.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Check;
import com.tan.warehouse2.bean.Delivery;
import com.tan.warehouse2.bean.Out;
import com.tan.warehouse2.bean.Sku;
import com.tan.warehouse2.mapper.CheckMapper;
import com.tan.warehouse2.service.CheckService;
import com.tan.warehouse2.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Description:
 * @date: 2020-05-07 17:31:13
 * @author: Tan.WL
 */
@Service
public class CheckServiceImpl implements CheckService{

    @Autowired
    CheckMapper checkMapper;

    @Autowired
    SkuService skuService;

    @Override
    public PageInfo<Check> checkByConditionForm(Check check, Integer pageNum, Integer pageSize, String limit) {

        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Check> pageInfo = null;
//        List<Out> entries = outMapper.getOutByCondition(out);
        if(StringUtils.isNotBlank(check.getCheckSn())){
            pageInfo = new PageInfo<>(checkMapper.getCheckByCheckSn(check.getCheckSn()));
        }else{
            pageInfo = new PageInfo<>(checkMapper.getCheckByConditionForm(check, limit));
        }
        if ( pageInfo.getList() == null || pageInfo.getList().size() < 1){
            return null;
        }
        for (Check e : pageInfo.getList()) {
            //获取相应sku -- 联合查询得到 skus
            List<Sku> skus = checkMapper.getSkusById(e.getId());
            e.setProductList(skus);
        }

        return pageInfo;
    }

    @Override
    public void deleteByLimit(String limit) {
        List<Integer> entryIdByLimit = checkMapper.findCheckIdByLimit(limit);
        checkMapper.deleteByLimit(limit);
        if(entryIdByLimit != null && entryIdByLimit.size() > 0){
            checkMapper.deleteRelationByIds(entryIdByLimit);
        }
    }

    @Override
    public int add(Check e) {
        int insert = checkMapper.insert(e);
        System.out.println("insert");
        System.out.println(e);
        if(insert != 0){
            List<Sku> skus = e.getProductList();
            checkMapper.insertRelation(e.getId(), skus);
        }

        return insert;
    }

    @Override
    public Check checkBatchByCheckSn(String checkSn) {
        Example example = new Example(Check.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("checkSn", checkSn);
        List<Check> entries = checkMapper.selectByExample(example);
        if(entries != null && entries.size() > 0){
            return entries.get(0);
        }
        return null;
    }

    @Override
    public PageInfo<Check> checkByCondition(Check check, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Check> pageInfo = null;
//        List<Out> entries = outMapper.getOutByCondition(out);
        if(StringUtils.isNotBlank(check.getCheckSn())){
            pageInfo = new PageInfo<>(checkMapper.getCheckByCheckSn(check.getCheckSn()));
        }else{
            pageInfo = new PageInfo<>(checkMapper.getCheckByCondition(check));
        }
        if ( pageInfo.getList() == null || pageInfo.getList().size() < 1){
            return null;
        }
        for (Check e : pageInfo.getList()) {
            //获取相应sku -- 联合查询得到 skus
            List<Sku> skus = checkMapper.getSkusById(e.getId());
            e.setProductList(skus);
        }

        return pageInfo;
    }

    @Override
    public PageInfo<Check> getAllPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Check> pageInfo = new PageInfo<>(checkMapper.getAll());

        if (pageInfo.getList() != null && pageInfo.getList().size() > 0) {
            for (Check out : pageInfo.getList()) {
                List<Sku> skus = checkMapper.getSkusById(out.getId());
                out.setProductList(skus);
            }
        }
        return pageInfo;
    }

    @Override
    public void remark(Integer checkId, Integer skuId, String remark) {
        checkMapper.remark(checkId, skuId, remark);
    }

    @Override
    public int description(Check out) {

        return 0;
    }

    @Override
    public List<Check> getOutByDateWarehouse(String outDate, Integer warehouseId) {
        return null;
    }

    @Override
    public List<Sku> getSkus(Integer outId) {
        return null;
    }
}
