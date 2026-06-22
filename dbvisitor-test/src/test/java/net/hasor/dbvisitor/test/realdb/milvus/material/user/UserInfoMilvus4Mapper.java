package net.hasor.dbvisitor.test.realdb.milvus.material.user;

import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus1;

@RefMapper("realdb/milvus/material/user-mapper-milvus-4.xml")
public interface UserInfoMilvus4Mapper {
    int insertUser(@Param("info") UserInfoMilvus1 info);

    UserInfoMilvus1 findUser(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
