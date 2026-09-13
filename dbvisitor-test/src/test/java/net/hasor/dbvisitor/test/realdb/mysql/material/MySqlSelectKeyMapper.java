/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mysql.material;

import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Order;
import net.hasor.dbvisitor.mapper.ResultSetType;
import net.hasor.dbvisitor.mapper.SelectKeySql;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.StatementType;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface MySqlSelectKeyMapper extends AnnotationAttributesMapper {
    @Override
    @SelectKeySql(value = "SELECT -959101", keyProperty = "id", order = Order.Before)
    @Insert("""
        INSERT INTO user_info (id, name, age, email, create_time)
        VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})
        """)
    int insertWithSelectKeyBefore(UserInfo user);

    @Override
    @SelectKeySql(value = "SELECT LAST_INSERT_ID()", keyProperty = "id", order = Order.After)
    @Insert("""
        INSERT INTO user_info (name, age, email, create_time)
        VALUES (#{name}, #{age}, #{email}, #{createTime})
        """)
    int insertWithSelectKeyAfter(UserInfo user);

    @Override
    @SelectKeySql(value = "SELECT -959102", keyProperty = "id", order = Order.Before,
            statementType = StatementType.Prepared, timeout = 30, fetchSize = 1, resultSetType = ResultSetType.DEFAULT)
    @Insert("""
        INSERT INTO user_info (id, name, age, email, create_time)
        VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})
        """)
    int insertWithSelectKeyFullAttrs(UserInfo user);
}
