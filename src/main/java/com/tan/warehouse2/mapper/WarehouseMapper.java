package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Warehouse;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface WarehouseMapper extends Mapper<Warehouse> {

    public Boolean deleteWarehouses(@Param("ids") List<Integer> ids);//返回null ???
    public List<Warehouse> getWarehouseLikeName(@Param("name") String name);
}
