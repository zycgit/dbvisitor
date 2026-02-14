package net.hasor.dbvisitor.test.realdb.mongo.dto3;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.realdb.mongo.dto1.UserInfo1;

@RefMapper("realdb/mongo/user-mapper-4.xml")
public interface UserInfo4Mapper {
    int saveUser(@Param("info") UserInfo1 info);

    UserInfo1 loadUser(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
