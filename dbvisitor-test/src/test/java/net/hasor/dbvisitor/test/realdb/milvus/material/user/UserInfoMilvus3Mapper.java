package net.hasor.dbvisitor.test.realdb.milvus.material.user;

import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;

@RefMapper("realdb/milvus/material/user-mapper-milvus-3.xml")
public interface UserInfoMilvus3Mapper {
    int insertUser(UserInfoMilvus3 user);

    List<UserInfoMilvus3> queryAll();

    int deleteUser(@Param("uid") String uid);
}
