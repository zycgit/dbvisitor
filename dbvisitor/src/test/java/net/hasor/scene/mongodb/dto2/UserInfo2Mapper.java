package net.hasor.scene.mongodb.dto2;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper()
public interface UserInfo2Mapper {
    @Insert("test.user_info.insert(#{info, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})")
    int saveUser(@Param("info") UserInfo2 info);

    @Query(value = "test.user_info.find({uid: #{uid}})")
    UserInfo2 loadUser(@Param("uid") String uid);

    @Delete("test.user_info.remove({uid: #{uid}})")
    int deleteUser(@Param("uid") String uid);
}
