package com.tan.warehouse2.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Entry;
import com.tan.warehouse2.bean.Sku;
import com.tan.warehouse2.mapper.EntryMapper;
import com.tan.warehouse2.service.EntryService;
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
public class EntryServiceImpl implements EntryService{


    @Autowired
    EntryMapper entryMapper;

    @Autowired
    SkuService skuService;

    @Override
    public void deleteByLimit(String limit) {
        List<Integer> entryIdByLimit = entryMapper.findEntryIdByLimit(limit);
        entryMapper.deleteByLimit(limit);
        if(entryIdByLimit != null && entryIdByLimit.size() > 0){

            entryMapper.deleteRelationByIds(entryIdByLimit);
        }
    }

    @Override
    public int description(Entry entry) {

        return entryMapper.updateByPrimaryKeySelective(entry);
    }

    @Override
    public PageInfo<Entry> getAllPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Entry> pageInfo = new PageInfo<>(entryMapper.getAll());

        if (pageInfo.getList() != null && pageInfo.getList().size() > 0) {
            for (Entry entry : pageInfo.getList()) {
                List<Sku> skus = entryMapper.getSkusById(entry.getId());
                entry.setProductList(skus);
            }
        }
        return pageInfo;
    }

    @Override
    public PageInfo<Entry> checkByConditionForm(Entry entry, Integer pageNum, Integer pageSize, String limit) {

        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Entry> pageInfo = null;
        if(StringUtils.isNotBlank(entry.getEntryNum())){
            pageInfo = new PageInfo<>(entryMapper.getEntryByEntryNum(entry.getEntryNum()));
        }else{
            pageInfo = new PageInfo<>(entryMapper.getEntryByConditionForm(entry, limit));
        }

        if ( pageInfo.getList() == null || pageInfo.getList().size() < 1){
            return null;
        }
        for (Entry e : pageInfo.getList()) {
            //获取相应sku -- 联合查询得到 skus
            List<Sku> skus = entryMapper.getSkusById(e.getId());
            e.setProductList(skus);
        }

        return pageInfo;
    }

    @Override
    public PageInfo<Entry> checkByCondition(Entry entry, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        PageInfo<Entry> pageInfo = null;
//        List<Entry> entries = entryMapper.getEntryByCondition(entry);
        if(StringUtils.isNotBlank(entry.getEntryNum())){
            pageInfo = new PageInfo<>(entryMapper.getEntryByEntryNum(entry.getEntryNum()));
        }else{
            pageInfo = new PageInfo<>(entryMapper.getEntryByCondition(entry));
        }

        if ( pageInfo.getList() == null || pageInfo.getList().size() < 1){
            return null;
        }
        for (Entry e : pageInfo.getList()) {
            //获取相应sku -- 联合查询得到 skus
            List<Sku> skus = entryMapper.getSkusById(e.getId());
            e.setProductList(skus);
        }

        return pageInfo;

    }

    @Override
    public int add(Entry e) {

        //
        int insert = entryMapper.insert(e);
        System.out.println("insert");
        System.out.println(e);
        if(insert != 0){
            List<Sku> skus = e.getProductList();
            entryMapper.insertRelation(e.getId(), skus);
            for (Sku sku : skus) {
                //更新库存数量
                skuService.addNum(sku.getId(), sku.getNum());
            }
        }

        return insert;
    }

    @Override
    public Entry checkBatchByEntryNum(String entryNum) {
        Example example = new Example(Entry.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("entryNum", entryNum);
        List<Entry> entries = entryMapper.selectByExample(example);
        if(entries != null && entries.size() > 0){
            return entries.get(0);
        }
        return null;
    }
}
