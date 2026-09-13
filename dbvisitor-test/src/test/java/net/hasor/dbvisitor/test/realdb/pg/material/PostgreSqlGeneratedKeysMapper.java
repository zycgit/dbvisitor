/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.pg.material;

import net.hasor.dbvisitor.mapper.GeneratedKeySource;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface PostgreSqlGeneratedKeysMapper extends AnnotationAttributesMapper {
    @Override
    @Insert(value = """
        INSERT INTO user_info (name, age, email, create_time)
        VALUES (#{name}, #{age}, #{email}, #{createTime}) RETURNING id
        """, useGeneratedKeys = true, keyProperty = "id", keyColumn = "id", generatedKeySource = GeneratedKeySource.ResultSet)
    int insertWithGeneratedKeyResultSet(UserInfo user);
}
