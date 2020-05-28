package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.bean.Sku;

import java.util.List;
import java.util.Set;

public interface SkuService {

    public List<Sku> getAll(Integer catalog3Id, Integer brandId);

    public Sku getSkuByName(String name, Integer catalog3Id, Integer brandId);

    public int add(Sku brand);

    public PageInfo<Sku> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Sku> getSkuLikeName(String name, Integer pageNum, Integer pageSize);

    public int update(Sku sku);

    public int delete(List<Integer> ids);//可以批量删除

    List<Sku> checkSkuByAll(Sku sku);

    List<Sku> getSkuByCBWId(Integer brandId, Integer catalog3Id, Integer warehouseId);

    int addNum(Integer id, Integer num);

    void minusNum(Integer id, Integer num);

    Sku getSkuById(Integer id);

    List<Sku> getAllByWarehouseId(Integer warehouseId);

//    public PageInfo<Sku> getSkuLikeName(String name, Integer pageNum, Integer pageSize);

}
