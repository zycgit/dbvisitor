package net.hasor.dbvisitor.test.realdb.elastic6.material.user;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo1a;

@RefMapper("realdb/elastic6/material/user-mapper-3.xml")
public interface UserInfo3Mapper {
    int saveUser(@Param("info") UserInfo3 info);

    UserInfo3 loadUser1(@Param("uid") String uid);

    UserInfo1a loadUser2(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
