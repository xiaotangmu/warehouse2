package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Catalog1;
import com.tan.warehouse2.bean.MyPage;

import java.util.List;

public interface Catalog1Service {

    public List<Catalog1> getAll();

    public Catalog1 getCatalog1ByName(String name);

    public int add(Catalog1 brand);

    public int update(Catalog1 brand, String oldName);

    public int delete(Catalog1 catalog1);

    public MyPage<Catalog1> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Catalog1> getCatalog1LikeName(String name, Integer pageNum, Integer pageSize);

}
