package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Authority;
import com.tan.warehouse2.bean.MyPage;

import java.util.List;

public interface AuthorityService {

    public List<Authority> getLevel1();
    public List<Authority> getLevel2(Integer pId);
    public List<Authority> getLevel3(Integer pId);

    public List<Authority> getAll();

    public Authority getAuthorityByName(Authority name);

    public Authority add(Authority brand);

    public int update(Authority brand, String oldName);

    public int delete(List<Integer> ids);//可以批量删除

    public PageInfo<Authority> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Authority> getAuthorityLikeName(String name, Integer pageNum, Integer pageSize);

}
