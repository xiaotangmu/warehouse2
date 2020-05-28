package com.tan.warehouse2.service;

import com.github.pagehelper.PageInfo;
import com.tan.warehouse2.bean.Client;
import com.tan.warehouse2.bean.MyPage;

import java.util.List;

public interface ClientService {

    public List<Client> getAll();
    public List<Client> getAll2();

    public Client getClientByName(String name);

    public int add(Client brand);

    public int update(Client brand);

    public int delete(List<String> name, List<Integer> ids);//可以批量删除

    public PageInfo<Client> getAllPage(Integer pageNum, Integer pageSize);

    public PageInfo<Client> getClientLikeName(String name, Integer pageNum, Integer pageSize);

}
