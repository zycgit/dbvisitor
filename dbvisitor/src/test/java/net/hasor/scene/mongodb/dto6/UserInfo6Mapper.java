package net.hasor.scene.mongodb.dto6;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper()
public interface UserInfo6Mapper {
    @Insert("test.user_info.insert({" +             //
            "\"uid\": #{info.userId}, " +           //
            "\"name\": #{info.userName}, " +        //
            "\"loginName\": #{info.account}, " +    //
            "\"loginPassword\": #{info.password}" + //
            "})")
    int saveUser(@Param("info") UserInfo6 info);

    @Query("test.user_info.find({uid: #{uid}})")
    UserInfo6 loadUser(@Param("uid") String uid);

    @Delete("test.user_info.remove({uid: #{uid}})")
    int deleteUser(@Param("uid") String uid);
}
