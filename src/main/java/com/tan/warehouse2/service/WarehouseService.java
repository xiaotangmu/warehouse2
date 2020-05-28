package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.bean.Warehouse;

import java.util.List;

public interface WarehouseService {

    public List<Warehouse> getAll();

    public Warehouse getWarehouseByName(String name);

    public int add(Warehouse warehouse2);

    public int update(Warehouse warehouse2, String oldName);

    public int delete(List<String> name, List<Integer> ids);//可以批量删除

    public MyPage<Warehouse> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Warehouse> getWarehouseLikeName(String name, Integer pageNum, Integer pageSize);

}
