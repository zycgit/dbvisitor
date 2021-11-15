package net.hasor.db.example.quick.dao3;
import net.hasor.db.dal.repository.Param;
import net.hasor.db.dal.repository.RefMapper;
import net.hasor.db.dal.session.BaseMapper;

import java.util.List;

@RefMapper("/mapper/quick_dao3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {

    public int insertUser(@Param("name") String name, @Param("age") int age);

    public int updateAge(@Param("id") int userId, @Param("age") int newAge);

    public int deleteByAge(@Param("age") int age);

    public List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);

    public List<TestUser> queryAll();
}
