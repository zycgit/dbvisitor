package net.hasor.realdb.mongo.dto5;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;

@RefMapper("dbvisitor_realdb/mongo/user-mapper-5.xml")
public interface UserInfo5Mapper {
    int saveUser(@Param("info") UserInfo5 info);

    UserInfo5 loadUser(@Param("userId") String userId);

    int deleteUser(@Param("userId") String userId);
}
