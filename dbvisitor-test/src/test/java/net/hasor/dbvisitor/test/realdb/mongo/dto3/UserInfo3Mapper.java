package net.hasor.dbvisitor.test.realdb.mongo.dto3;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.realdb.mongo.dto1.UserInfo1;

@RefMapper("realdb/mongo/user-mapper-3.xml")
public interface UserInfo3Mapper {
    int saveUser(@Param("info") UserInfo3 info);
    UserInfo3 loadUser1(@Param("uid") String uid);
    UserInfo1 loadUser2(@Param("uid") String uid);
    int deleteUser(@Param("uid") String uid);
}
