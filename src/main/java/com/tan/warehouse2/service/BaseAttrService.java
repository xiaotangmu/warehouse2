package com.tan.warehouse2.service;

import com.tan.warehouse2.bean.BaseAttr;

import java.util.List;
import java.util.Set;

public interface BaseAttrService {

    public List<BaseAttr> getAll(Integer catalog3Id);

    public BaseAttr getBaseAttrByName(String name, Integer catalog3Id);

    public int add(BaseAttr brand);

    public int update(BaseAttr brand, String oldName);

    public int delete(List<String> names, List<Integer> ids, Integer catalog3Id);//可以批量删除

    Set<BaseAttr> getAttrAndValueBySpuId(Integer spuId);

//    public PageInfo<BaseAttr> getBaseAttrLikeName(String name, Integer pageNum, Integer pageSize);

}
