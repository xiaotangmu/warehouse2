package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Catalog;
import com.tan.warehouse2.bean.Catalog3;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface Catalog3Mapper extends Mapper<Catalog3> {

    public Boolean deleteCatalogs(@Param("ids") List<Integer> ids);//返回null ???

    public List<Catalog3> getCatalog3LikeName(@Param("name") String name);

    public int insertCatalog(Catalog catalog);
}
