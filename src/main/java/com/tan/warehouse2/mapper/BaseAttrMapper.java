package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.BaseAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrMapper extends Mapper<BaseAttr> {

    public Boolean deleteBaseAttrs(@Param("ids") List<Integer> ids);//返回null ???
    public List<BaseAttr> getBaseAttrLikeName(@Param("name") String name);

    List<BaseAttr> getAttrBySpuId(Integer spuId);
    List<String> getValueByAttrIdAndSpuId(@Param("attrId") Integer attrId, @Param("spuId") Integer spuId);
}
