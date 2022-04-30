package net.hasor.db.example.quick.page3;
import net.hasor.db.dal.repository.Param;
import net.hasor.db.dal.repository.RefMapper;
import net.hasor.db.dal.session.BaseMapper;
import net.hasor.db.page.Page;
import net.hasor.db.page.PageResult;

import java.util.List;

@RefMapper("/mapper/quick_page3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {

    public List<TestUser> queryByAge1(@Param("beginAge") int beginAge, @Param("endAge") int endAge, Page pageInfo);

    public PageResult<TestUser> queryByAge2(@Param("beginAge") int beginAge, @Param("endAge") int endAge, Page pageInfo);

}
