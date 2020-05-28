package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Supplier;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SupplierMapper extends Mapper<Supplier> {

    public Boolean deleteSuppliers(@Param("ids") List<Integer> ids);//返回null ???
    public List<Supplier> getSupplierLikeName(@Param("name") String name);
}
