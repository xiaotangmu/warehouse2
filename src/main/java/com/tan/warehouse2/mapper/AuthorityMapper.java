package com.tan.warehouse2.mapper;

import com.tan.warehouse2.bean.Authority;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

public interface AuthorityMapper extends Mapper<Authority> {

    public Boolean deleteAuthoritys(@Param("ids") Set<Integer> ids);//返回null ???
    public List<Authority> getAuthorityLikeName(@Param("name") String name);

    public List<Authority> getLevel1();
    public List<Authority> getLevel2(Integer pId);
    public List<Authority> getLevel3(Integer pId);

    public List<Integer> getAuthorityChildrenId(@Param("ids") List<Integer> ids);
}
