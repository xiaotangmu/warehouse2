package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Catalog;
import com.tan.warehouse2.bean.Catalog3;

import java.util.List;

public interface Catalog3Service {

    public List<Catalog3> getAll(Integer catalog2Id);

    public List<Catalog> getAll2(Integer catalog2Id, Integer catalog1Id);

    public Catalog3 getCatalog3ByName(String name, Integer catalog2Id);

    public int add(Catalog3 brand);

    public int update(Catalog3 brand, String oldName);

    public int delete(List<String> names, List<Integer> ids, List<String> ztreeIds, Integer catalog1Id, Integer catalog2Id);//可以批量删除

//    public MyPage<Catalog3> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Catalog3> getCatalog3LikeName(String name, Integer pageNum, Integer pageSize);

}
