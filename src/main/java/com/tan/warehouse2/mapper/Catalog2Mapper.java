package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Catalog2;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface Catalog2Mapper extends Mapper<Catalog2> {

    public Boolean deleteCatalogs(@Param("ids") List<Integer> ids);//返回null ???
    public List<Catalog2> getCatalog2LikeName(@Param("name") String name);

//    public int insertCatalog(Catalog catalog);
//
//    public List<Catalog> selectCatalog(Integer catalog1Id);
}
