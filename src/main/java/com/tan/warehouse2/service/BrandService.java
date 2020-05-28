package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Brand;
import com.tan.warehouse2.bean.MyPage;

import java.util.List;

public interface BrandService {

    public List<Brand> getAll();

    public Brand getBrandByName(String name);

    public int add(Brand brand);

    public int update(Brand brand);

    public int delete(List<String> name, List<Integer> ids);//可以批量删除

    public MyPage<Brand> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Brand> getBrandLikeName(String name, Integer pageNum, Integer pageSize);

}
