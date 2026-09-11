/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.dto1;

import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.NativeCrudMapper;

@SimpleMapper
public interface RedisCrudMapper extends NativeCrudMapper {
    @Override
    @Query("@{macro, nxnCrudSelect}")
    RedisParameterUser selectById(@Param("id") Integer id);
}
