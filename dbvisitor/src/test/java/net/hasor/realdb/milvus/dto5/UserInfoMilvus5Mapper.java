package net.hasor.realdb.milvus.dto5;

import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;

@RefMapper("/dbvisitor_realdb/milvus/user-mapper-milvus-5.xml")
public interface UserInfoMilvus5Mapper {
    int insertUser(@Param("info") UserInfoMilvus5 info);

    List<UserInfoMilvus5> queryAll();

    int deleteUser(@Param("uid") String uid);
}
