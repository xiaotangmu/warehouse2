package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.bean.Spu;

import java.util.List;

public interface SpuService {

    public List<Spu> getAll(Integer catalog3Id, Integer brandId);

    public Spu getSpuByName(String name, Integer catalog3Id, Integer brandId);

    public int add(Spu brand);

    public MyPage<Spu> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Spu> getSpuLikeName(String name, Integer pageNum, Integer pageSize);

    public int update(Spu brand, String oldName, Integer oldBrandId);

    public int delete(List<Spu> spus);//可以批量删除

//    public PageInfo<Spu> getSpuLikeName(String name, Integer pageNum, Integer pageSize);

}
