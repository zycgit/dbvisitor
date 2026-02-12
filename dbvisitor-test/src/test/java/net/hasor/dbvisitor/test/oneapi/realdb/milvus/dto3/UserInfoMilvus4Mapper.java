package net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto3;

import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto1.UserInfoMilvus1;

@RefMapper("/oneapi/realdb/milvus/user-mapper-milvus-4.xml")
public interface UserInfoMilvus4Mapper {
    int insertUser(@Param("info") UserInfoMilvus1 info);

    UserInfoMilvus1 findUser(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
