/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material;

import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Order;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.ResultSetType;
import net.hasor.dbvisitor.mapper.SelectKeySql;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.StatementType;

@SimpleMapper
public interface MongoKeyMapper {
    @Insert(value = "@{macro, mongoSource}.insert({name: #{name}})", useGeneratedKeys = true, keyProperty = "id")
    int insertGenerated(MongoKeyRecord record);

    @Insert(value = "@{macro, mongoSource}.insert({name: #{name}})", useGeneratedKeys = true, keyProperty = "id", keyColumn = "_ID")
    int insertGeneratedColumn(MongoKeyRecord record);

    @Insert(value = "@{macro, mongoSource}.insert({_id: #{id}, name: #{name}})", useGeneratedKeys = false)
    int insertExplicit(MongoKeyRecord record);

    @Query("@{macro, mongoSource}.find({_id: ObjectId(#{id})}, {_id: 0, name: 1})")
    String generatedName(@Param("id") String id);

    @Query("@{macro, mongoSource}.find({_id: #{id}}, {_id: 0, name: 1})")
    String explicitName(@Param("id") String id);

    @SelectKeySql(value = "@{macro, mongoKeySource}.find({name: #{name}}, {_id: 1})", keyProperty = "id", order = Order.Before)
    @Insert("@{macro, mongoSource}.insert({_id: ObjectId(#{id}), name: #{name}})")
    int selectKeyBefore(MongoKeyRecord record);

    @SelectKeySql(value = "@{macro, mongoSource}.find({name: #{name}}, {_id: 1})", keyProperty = "id", order = Order.After)
    @Insert("@{macro, mongoSource}.insert({name: #{name}})")
    int selectKeyAfter(MongoKeyRecord record);

    @SelectKeySql(value = "@{macro, mongoKeySource}.find({name: #{name}}, {_id: 1})", keyProperty = "id", order = Order.Before,
            statementType = StatementType.Prepared, timeout = 30, fetchSize = 1, resultSetType = ResultSetType.DEFAULT)
    @Insert("@{macro, mongoSource}.insert({_id: ObjectId(#{id}), name: #{name}})")
    int selectKeyWithOptions(MongoKeyRecord record);
}
