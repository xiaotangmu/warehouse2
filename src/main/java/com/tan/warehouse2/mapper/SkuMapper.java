package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.BaseAttr;
import com.tan.warehouse2.bean.Sku;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuMapper extends Mapper<Sku> {

    public Boolean deleteSkus(@Param("ids") List<Integer> ids);//返回null ???
    public List<Sku> getSkuLikeName(@Param("name") String name);

    public List<BaseAttr> getAttr(@Param("skuId") Integer skuId);

    public Boolean deleteAttr(@Param("ids") List<Integer> ids);
    public Boolean insertAttr(@Param("attrIds") List<Integer> attrIds, @Param("skuId") Integer skuId);

    List<Sku> findSkuBySku(@Param("sku") Sku sku);

    void addNum(@Param("id") Integer id, @Param("num") Integer num);

    void minusNum(@Param("id") Integer id, @Param("num") Integer num);
}
