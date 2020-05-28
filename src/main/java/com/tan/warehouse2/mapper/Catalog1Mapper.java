package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Catalog;
import com.tan.warehouse2.bean.Catalog1;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface Catalog1Mapper extends Mapper<Catalog1> {

    public Boolean deleteCatalog1s(@Param("ids") List<Integer> ids);//返回null ???
    public List<Catalog1> getCatalog1LikeName(@Param("name") String name);

    public int insertCatalog(Catalog catalog);
}
