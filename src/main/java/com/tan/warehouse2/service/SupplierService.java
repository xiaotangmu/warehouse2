package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.MyPage;
import com.tan.warehouse2.bean.Supplier;

import java.util.List;

public interface SupplierService {

    public List<Supplier> getAll();

    public Supplier getSupplierByName(String name);

    public int add(Supplier brand);

    public int update(Supplier brand);

    public int delete(List<String> name, List<Integer> ids);//可以批量删除

    public MyPage<Supplier> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Supplier> getSupplierLikeName(String name, Integer pageNum, Integer pageSize);

}
