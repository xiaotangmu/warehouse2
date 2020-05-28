package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.BaseAttr;
import com.tan.warehouse2.bean.Spu;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuMapper extends Mapper<Spu> {

    public Boolean deleteSpus(@Param("ids") List<Integer> ids);//返回null ???
    public List<Spu> getSpuLikeName(@Param("name") String name);

    public List<BaseAttr> getAttr(@Param("spuId") Integer spuId);

    public Boolean deleteAttr(@Param("ids") List<Integer> ids);
    public Boolean deleteAttrValues(@Param("ids") List<Integer> ids);
    public Boolean insertAttr(@Param("attrs") List<BaseAttr> attrs, @Param("spuId") Integer spuId);
    public Boolean insertAttrValue(@Param("values") List<String> values, @Param("spuId") Integer spuId, @Param("attrId") Integer attrId);

    public List<String> getAttrValues(@Param("spuId") Integer spuId, @Param("attrId") Integer attrId);
}
