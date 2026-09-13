/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperSelectKeyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

public class ClickHouseAnnotationMapperSelectKeyTest extends AnnotationMapperSelectKeyCase {
    @Override
    protected Class<? extends AnnotationAttributesMapper> mapperType() {
        return ClickHouseSelectKeyMapper.class;
    }

    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @SimpleMapper
    public interface ClickHouseSelectKeyMapper extends AnnotationAttributesMapper {
        @Override
        @SelectKeySql(value = "SELECT toInt32(cityHash64(generateUUIDv4()))", keyProperty = "id", order = Order.Before)
        @Insert("INSERT INTO user_info (id, name, age, email) VALUES (#{id}, #{name}, #{age}, #{email})")
        int insertWithSelectKeyBefore(UserInfo user);

        @Override
        @SelectKeySql(value = "SELECT id FROM user_info WHERE name = #{name} AND email = #{email}", keyProperty = "id", order = Order.After)
        @Insert("INSERT INTO user_info (name, age, email) VALUES (#{name}, #{age}, #{email})")
        int insertWithSelectKeyAfter(UserInfo user);

        @Override
        @SelectKeySql(value = "SELECT toInt32(cityHash64(generateUUIDv4()))", keyProperty = "id", order = Order.Before,
                statementType = StatementType.Prepared, timeout = 30, fetchSize = 1, resultSetType = ResultSetType.DEFAULT)
        @Insert("INSERT INTO user_info (id, name, age, email) VALUES (#{id}, #{name}, #{age}, #{email})")
        int insertWithSelectKeyFullAttrs(UserInfo user);
    }
}
