package net.hasor.dbvisitor.test.realdb.milvus.material.user;

import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;

@RefMapper("realdb/milvus/material/user-mapper-milvus-5.xml")
public interface UserInfoMilvus5Mapper {
    int insertUser(@Param("info") UserInfoMilvus5 info);

    List<UserInfoMilvus5> queryAll();

    int deleteUser(@Param("uid") String uid);
}
