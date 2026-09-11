/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus.material.user;

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
