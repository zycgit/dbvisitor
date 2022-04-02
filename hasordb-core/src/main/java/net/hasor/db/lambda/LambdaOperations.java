/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.db.lambda;
import net.hasor.db.jdbc.JdbcOperations;
import net.hasor.db.mapping.resolve.MappingOptions;

import java.sql.SQLException;
import java.util.Map;

/**
 * 提供 lambda 方式生成 SQL。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public interface LambdaOperations extends JdbcOperations {

    // ----------------------------------------------------------------------------------
    // Insert
    // ----------------------------------------------------------------------------------

    /** 相当于 insert ... */
    default <T> InsertOperation<T> lambdaInsert(Class<T> exampleType) {
        return lambdaInsert(exampleType, null);
    }

    /** 相当于 insert ... */
    <T> InsertOperation<T> lambdaInsert(Class<T> exampleType, MappingOptions options);

    /** 相当于 insert ... */
    default InsertOperation<Map<String, Object>> lambdaInsert(String table) throws SQLException {
        return lambdaInsert(null, table, null);
    }

    /** 相当于 insert ... */
    default InsertOperation<Map<String, Object>> lambdaInsert(String schema, String table) throws SQLException {
        return lambdaInsert(schema, table, null);
    }

    /** 相当于 insert ... */
    InsertOperation<Map<String, Object>> lambdaInsert(String schema, String table, MappingOptions options) throws SQLException;

    // ----------------------------------------------------------------------------------
    // Update
    // ----------------------------------------------------------------------------------

    /** 相当于 update ... */
    default <T> EntityUpdateOperation<T> lambdaUpdate(Class<T> exampleType) {
        return lambdaUpdate(exampleType, null);
    }

    /** 相当于 update ... */
    <T> EntityUpdateOperation<T> lambdaUpdate(Class<T> exampleType, MappingOptions options);

    /** 相当于 update ... */
    default MapUpdateOperation lambdaUpdate(String table) throws SQLException {
        return lambdaUpdate(null, table, null);
    }

    /** 相当于 update ... */
    MapUpdateOperation lambdaUpdate(String schema, String table, MappingOptions options) throws SQLException;

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------

    /** 相当于 delete */
    default <T> EntityDeleteOperation<T> lambdaDelete(Class<T> exampleType) {
        return lambdaDelete(exampleType, null);
    }

    /** 相当于 delete */
    <T> EntityDeleteOperation<T> lambdaDelete(Class<T> exampleType, MappingOptions options);

    /** 相当于 delete ... */
    default MapDeleteOperation lambdaDelete(String table) throws SQLException {
        return lambdaDelete(null, table, null);
    }

    /** 相当于 delete ... */
    MapDeleteOperation lambdaDelete(String schema, String table, MappingOptions options) throws SQLException;

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------
    //

    /** 相当于 select * form */
    default <T> EntityQueryOperation<T> lambdaQuery(Class<T> exampleType) {
        return lambdaQuery(exampleType, null);
    }

    /** 相当于 select * form */
    <T> EntityQueryOperation<T> lambdaQuery(Class<T> exampleType, MappingOptions options);

    /** 相当于 select ... */
    default MapQueryOperation lambdaQuery(String table) throws SQLException {
        return lambdaQuery(null, table, null);
    }

    /** 相当于 select ... */
    MapQueryOperation lambdaQuery(String schema, String table, MappingOptions options) throws SQLException;

}
