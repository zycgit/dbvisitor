package net.hasor.dbvisitor.test.realdb.elastic7.dto3;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.realdb.elastic7.dto1.UserInfo1a;

@RefMapper("realdb/elastic7/user-mapper-4.xml")
public interface UserInfo4Mapper {
    int saveUser(@Param("info") UserInfo1a info);

    UserInfo1a loadUser(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
