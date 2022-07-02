package com.example.demo.quick.page3;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.Param;
import net.hasor.dbvisitor.dal.repository.RefMapper;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageResult;

import java.util.List;

@RefMapper("/mapper/quick_page3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {

    public List<TestUser> queryByAge1(@Param("beginAge") int beginAge, @Param("endAge") int endAge, Page pageInfo);

    public PageResult<TestUser> queryByAge2(@Param("beginAge") int beginAge, @Param("endAge") int endAge, Page pageInfo);

}
