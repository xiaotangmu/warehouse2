package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Brand;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    public Boolean deleteBrands(@Param("ids") List<Integer> ids);//返回null ???
    public List<Brand> getBrandLikeName(@Param("name") String name);
}
