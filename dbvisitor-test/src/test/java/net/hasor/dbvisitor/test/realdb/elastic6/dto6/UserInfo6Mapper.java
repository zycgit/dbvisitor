package net.hasor.dbvisitor.test.realdb.elastic6.dto6;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper()
public interface UserInfo6Mapper {
    @Insert("POST /user_info/_doc {" + "\"uid\": #{info.userId}, " + "\"name\": #{info.userName}, " + "\"loginName\": #{info.account}, " + "\"loginPassword\": #{info.password}" + "}")
    int saveUser(@Param("info") UserInfo6 info);

    @Query("POST /user_info/_search {\"query\": {\"term\": {\"uid\": #{uid}}}}")
    UserInfo6 loadUser(@Param("uid") String uid);

    @Delete("POST /user_info/_delete_by_query {\"query\": {\"term\": {\"uid\": #{uid}}}}")
    int deleteUser(@Param("uid") String uid);
}
