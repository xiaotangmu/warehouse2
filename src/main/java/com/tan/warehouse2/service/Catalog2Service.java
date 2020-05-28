package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Catalog;
import com.tan.warehouse2.bean.Catalog2;

import java.util.List;

public interface Catalog2Service {

    public List<Catalog2> getAll(Integer catalog1Id);

    public List<Catalog> getAll2(Integer catalog1Id);//树

    public Catalog2 getCatalog2ByName(String name, Integer level1Id);

    public int add(Catalog2 brand);

    public int update(Catalog2 brand, String oldName);

    public int delete(List<String> name, List<Integer> ids, List<String> ztreeIds, Integer catalog1Id);//可以批量删除

//    public MyPage<Catalog2> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Catalog2> getCatalog2LikeName(String name, Integer pageNum, Integer pageSize);

}
