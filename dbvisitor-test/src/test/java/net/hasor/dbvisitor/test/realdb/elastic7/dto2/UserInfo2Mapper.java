package net.hasor.dbvisitor.test.realdb.elastic7.dto2;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper()
public interface UserInfo2Mapper {
    @Insert("POST /user_info/_doc #{info}")
    int saveUser(@Param("info") UserInfo2 info);

    @Query("POST /user_info/_search {\"query\": {\"match\": {\"uid\": #{uid}}}}")
    UserInfo2 loadUser(@Param("uid") String uid);

    @Delete("POST /user_info/_delete_by_query {\"query\": {\"match\": {\"uid\": #{uid}}}}")
    int deleteUser(@Param("uid") String uid);
}
