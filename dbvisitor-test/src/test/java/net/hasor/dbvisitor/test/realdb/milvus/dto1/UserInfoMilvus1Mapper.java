package net.hasor.dbvisitor.test.realdb.milvus.dto1;

import net.hasor.dbvisitor.mapper.*;

@SimpleMapper
public interface UserInfoMilvus1Mapper {

    @Insert("INSERT INTO tb_mapper_user_milvus (uid, name, loginName, loginPassword, v) VALUES (#{uid}, #{name}, #{loginName}, #{loginPassword}, #{v})")
    int insertUser(UserInfoMilvus1 user);

    @Query("/*+ consistency_level=Strong */ SELECT * FROM tb_mapper_user_milvus WHERE uid = #{uid}")
    UserInfoMilvus1 selectUser(@Param("uid") String uid);

    @Delete("DELETE FROM tb_mapper_user_milvus WHERE uid = #{uid}")
    int deleteUser(@Param("uid") String uid);
}
