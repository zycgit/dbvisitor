/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus.material.user;

import java.util.List;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper
public interface UserInfoMilvus1Mapper {

    @Insert("INSERT INTO tb_mapper_user_milvus (uid, name, loginName, loginPassword, v) VALUES (#{uid}, #{name}, #{loginName}, #{loginPassword}, #{v})")
    int insertUser(UserInfoMilvus1 user);

    @Update("UPDATE tb_mapper_user_milvus SET name = #{name} WHERE uid = #{uid}")
    int updateName(@Param("uid") String uid, @Param("name") String name);

    @Update("UPDATE tb_mapper_user_milvus SET name = #{name}, loginName = #{loginName} WHERE uid = #{uid}")
    int updateUser(@Param("uid") String uid, @Param("name") String name, @Param("loginName") String loginName);

    @Query("/*+ consistency_level=Strong */ SELECT * FROM tb_mapper_user_milvus WHERE uid = #{uid}")
    UserInfoMilvus1 selectUser(@Param("uid") String uid);

    @Query("/*+ consistency_level=Strong */ SELECT * FROM tb_mapper_user_milvus")
    List<UserInfoMilvus1> queryAll();

    @Query("/*+ consistency_level=Strong */ SELECT COUNT(*) FROM tb_mapper_user_milvus")
    int countAll();

    @Delete("DELETE FROM tb_mapper_user_milvus WHERE uid = #{uid}")
    int deleteUser(@Param("uid") String uid);
}
