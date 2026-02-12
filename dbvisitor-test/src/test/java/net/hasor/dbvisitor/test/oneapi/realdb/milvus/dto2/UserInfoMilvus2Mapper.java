package net.hasor.dbvisitor.test.oneapi.realdb.milvus.dto2;

import java.io.Serializable;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

@SimpleMapper
public interface UserInfoMilvus2Mapper extends BaseMapper<UserInfoMilvus2> {
    @Query("/*+ consistency_level=Strong */ SELECT * FROM tb_mapper_user_milvus WHERE uid = #{id}")
    UserInfoMilvus2 selectById(@Param("id") Serializable id);
}
