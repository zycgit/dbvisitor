/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport.Generated;

@SimpleMapper
public interface RedisKeyMapper {
    @Insert("HSET #{key} #{id} #{value}")
    @SelectKeySql(value = "INCR #{counter}", keyProperty = "id", order = Order.Before)
    int before(Generated record);

    @Insert("RPUSH #{key} #{value}")
    @SelectKeySql(value = "LLEN #{key}", keyProperty = "id", order = Order.After)
    int after(Generated record);

    @Insert("HSET #{key} #{id} #{value}")
    @SelectKeySql(value = "INCR #{counter}", keyProperty = "id", order = Order.Before,
            statementType = StatementType.Prepared, timeout = 30, fetchSize = 1, resultSetType = ResultSetType.DEFAULT)
    int options(Generated record);

    @Insert(value = "EVAL \"local id=redis.call('INCR',KEYS[1]); redis.call('HSET',KEYS[2],id,ARGV[1]); return id\" 2 #{counter} #{key} #{value}",
            useGeneratedKeys = true, generatedKeySource = GeneratedKeySource.ResultSet, keyProperty = "id", keyColumn = "VALUE")
    int resultKey(Generated record);
}
