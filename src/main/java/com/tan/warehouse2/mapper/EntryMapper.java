package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Entry;
import com.tan.warehouse2.bean.Sku;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Description:
 * @date: 2020-04-30 09:15:29
 * @author: Tan.WL
 */
public interface EntryMapper extends Mapper<Entry> {

    int insertRelation(@Param("id") Integer id, @Param("skus") List<Sku> skus);

    List<Entry> getEntryByCondition( Entry entry);
    List<Entry> getEntryByEntryNum(@Param("entryNum") String entryNum);

    List<Sku> getSkusById(@Param("id") Integer id);

    List<Entry> getAll();

    void deleteByLimit(@Param("limit") String limit);

    List<Integer> findEntryIdByLimit(@Param("limit") String limit);

    void deleteRelationByIds(@Param("ids") List<Integer> ids);

    List<Entry> getEntryByConditionForm(@Param("entry") Entry entry, @Param("limit") String limit);
}
