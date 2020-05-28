package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Client;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ClientMapper extends Mapper<Client> {

    public Boolean deleteClients(@Param("ids") List<Integer> ids);//返回null ???
    public List<Client> getClientLikeName(@Param("name") String name);
}
