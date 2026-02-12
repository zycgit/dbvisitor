package net.hasor.dbvisitor.test.oneapi.realdb.mongo.dto1;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper()
public interface UserInfo1Mapper {
    @Insert("test.user_info.insert(#{info, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})")
    int saveUser(@Param("info") UserInfo1 info);

    @Query("test.user_info.find({uid: #{uid}})")
    UserInfo1 loadUser(@Param("uid") String uid);

    @Delete("test.user_info.remove({uid: #{uid}})")
    int deleteUser(@Param("uid") String uid);
}
