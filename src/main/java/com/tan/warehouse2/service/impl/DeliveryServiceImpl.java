package com.tan.warehouse2.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Delivery;
import com.tan.warehouse2.bean.Out;
import com.tan.warehouse2.bean.Sku;
import com.tan.warehouse2.mapper.DeliveryMapper;
import com.tan.warehouse2.service.DeliveryService;
import com.tan.warehouse2.service.OutService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Description:
 * @date: 2020-05-06 10:37:22
 * @author: Tan.WL
 */
@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    DeliveryMapper deliveryMapper;

    @Autowired
    OutService outService;


    @Override
    public PageInfo<Delivery> checkByConditionForm(Delivery delivery, Integer pageNum, Integer pageSize, String limit) {

        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Delivery> pageInfo = null;
//        List<Out> entries = outMapper.getOutByCondition(out);
        if(StringUtils.isNotBlank(delivery.getDeliveryNum())){
            pageInfo = new PageInfo<>(deliveryMapper.getDeliveryByDeliveryNum(delivery.getDeliveryNum()));
        }else{
            pageInfo = new PageInfo<>(deliveryMapper.getDeliveryByConditionForm(delivery,limit));
        }
        if(pageInfo.getList() != null && pageInfo.getList().size() > 0){
            for (Delivery d : pageInfo.getList()) {
                Out out = new Out();
                out.setOutNum(d.getOutNum());
                PageInfo<Out> outPageInfo = outService.checkByCondition(out, 1, 5);
                Out out1 = outPageInfo.getList().get(0);
                d.setOut(out1);
            }
        }

        return pageInfo;
    }

    @Override
    public PageInfo<Delivery> checkByCondition(Delivery delivery, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Delivery> pageInfo = null;
//        List<Out> entries = outMapper.getOutByCondition(out);
        if(StringUtils.isNotBlank(delivery.getDeliveryNum())){
            pageInfo = new PageInfo<>(deliveryMapper.getDeliveryByDeliveryNum(delivery.getDeliveryNum()));
        }else{
            pageInfo = new PageInfo<>(deliveryMapper.getDeliveryByCondition(delivery));
        }
        if(pageInfo.getList() != null && pageInfo.getList().size() > 0){
            for (Delivery d : pageInfo.getList()) {
                Out out = new Out();
                out.setOutNum(d.getOutNum());
                PageInfo<Out> outPageInfo = outService.checkByCondition(out, 1, 5);
                Out out1 = outPageInfo.getList().get(0);
                d.setOut(out1);
            }
        }

        return pageInfo;
    }

    @Override
    public PageInfo<Delivery> getAllPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Delivery> pageInfo = new PageInfo<>(deliveryMapper.getAll());

        if(pageInfo.getList() != null && pageInfo.getList().size() > 0){
            for (Delivery d : pageInfo.getList()) {
                Out out = new Out();
                out.setOutNum(d.getOutNum());
                PageInfo<Out> outPageInfo = outService.checkByCondition(out, 1, 5);
                Out out1 = outPageInfo.getList().get(0);
                d.setOut(out1);
            }
        }
        return pageInfo;
    }

    @Override
    public int description(Delivery delivery) {
        return deliveryMapper.updateDesc(delivery);
    }

    @Override
    public Delivery checkBatchByDeliveryNum(String deliveryNum) {
        Example example = new Example(Delivery.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("deliveryNum", deliveryNum);
        List<Delivery> entries = deliveryMapper.selectByExample(example);
        if(entries != null && entries.size() > 0){
            return entries.get(0);
        }
        return null;
    }

    @Override
    public int add(Delivery delivery) {
        return deliveryMapper.insert(delivery);
    }
}
