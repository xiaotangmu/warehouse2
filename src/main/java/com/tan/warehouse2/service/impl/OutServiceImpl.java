package com.tan.warehouse2.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Out;
import com.tan.warehouse2.bean.Sku;
import com.tan.warehouse2.mapper.OutMapper;
import com.tan.warehouse2.service.OutService;
import com.tan.warehouse2.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Description:
 * @date: 2020-04-30 09:14:32
 * @author: Tan.WL
 */
@Service
public class OutServiceImpl implements OutService{


    @Autowired
    OutMapper outMapper;

    @Autowired
    SkuService skuService;

    @Override
    public PageInfo<Out> checkByConditionForm(Out out, Integer pageNum, Integer pageSize, String limit) {

        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Out> pageInfo = null;
//        List<Out> entries = outMapper.getOutByCondition(out);
        if(StringUtils.isNotBlank(out.getOutNum())){
            pageInfo = new PageInfo<>(outMapper.getOutByOutNum(out.getOutNum()));
        }else{
            pageInfo = new PageInfo<>(outMapper.getOutByConditionForm(out, limit));
        }

        if ( pageInfo.getList() == null || pageInfo.getList().size() < 1){
            return null;
        }
        for (Out e : pageInfo.getList()) {
            //获取相应sku -- 联合查询得到 skus
            List<Sku> skus = outMapper.getSkusById(e.getId());
            e.setProductList(skus);
        }

        return pageInfo;
    }

    @Override
    public void deleteByLimit(String limit) {
        List<Integer> entryIdByLimit = outMapper.findOutIdByLimit(limit);
        outMapper.deleteByLimit(limit);

        if(entryIdByLimit != null && entryIdByLimit.size() > 0){
            outMapper.deleteRelationByIds(entryIdByLimit);
            //删除送货
            outMapper.deleteDeliveryByOutIds(entryIdByLimit);
        }

    }

    @Override
    public List<Sku> getSkus(Integer outId) {
        return outMapper.getSkusById(outId);
    }

    @Override
    public List<Out> getOutByDateWarehouse(String outDate, Integer warehouseId) {
        Example example = new Example(Out.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("outDate", outDate);
        criteria.andEqualTo("warehouseId", warehouseId);
        List<Out> outs = outMapper.selectByExample(example);
        return outs;
    }

    @Override
    public int description(Out out) {

        return outMapper.updateByPrimaryKeySelective(out);
    }

    @Override
    public PageInfo<Out> getAllPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Out> pageInfo = new PageInfo<>(outMapper.getAll());

        if (pageInfo.getList() != null && pageInfo.getList().size() > 0) {
            for (Out out : pageInfo.getList()) {
                List<Sku> skus = outMapper.getSkusById(out.getId());
                out.setProductList(skus);
            }
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Out> checkByCondition(Out out, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Out> pageInfo = null;
//        List<Out> entries = outMapper.getOutByCondition(out);
        if(StringUtils.isNotBlank(out.getOutNum())){
            pageInfo = new PageInfo<>(outMapper.getOutByOutNum(out.getOutNum()));
        }else{
            pageInfo = new PageInfo<>(outMapper.getOutByCondition(out));
        }

        if ( pageInfo.getList() == null || pageInfo.getList().size() < 1){
            return null;
        }
        for (Out e : pageInfo.getList()) {
            //获取相应sku -- 联合查询得到 skus
            List<Sku> skus = outMapper.getSkusById(e.getId());
            e.setProductList(skus);
        }

        return pageInfo;

    }

    @Override
    public int add(Out e) {

        //
        int insert = outMapper.insert(e);
        System.out.println("insert");
        System.out.println(e);
        if(insert != 0){
            List<Sku> skus = e.getProductList();
            outMapper.insertRelation(e.getId(), skus);
            for (Sku sku : skus) {
                //更新库存数量
//                skuService.addNum(sku.getId(), sku.getNum());
                skuService.minusNum(sku.getId(), sku.getNum());
            }

        }

        return insert;
    }

    @Override
    public Out checkBatchByOutNum(String outNum) {
        Example example = new Example(Out.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("outNum", outNum);
        List<Out> entries = outMapper.selectByExample(example);
        if(entries != null && entries.size() > 0){
            return entries.get(0);
        }
        return null;
    }
}
