/*
 * Copyright 2015-2022 the original author or authors.
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
package net.hasor.dbvisitor.lambda;
/**
 * 提供 lambda 方式生成 SQL。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-27
 */
public interface LambdaOperations {
    // ----------------------------------------------------------------------------------
    // Insert
    // ----------------------------------------------------------------------------------

    /** 相当于 insert ... */
    default <T> InsertOperation<T> insertBySpace(Class<T> entityType) {
        return this.insertBySpace(entityType, "");
    }

    /** 相当于 insert ... */
    <T> InsertOperation<T> insertBySpace(Class<T> entityType, String space);

    /** 相当于 insert ... */
    default <T> InsertOperation<T> insertByTable(String table) {
        return this.insertByTable(null, null, table, null);
    }

    /** 相当于 insert ... */
    default <T> InsertOperation<T> insertByTable(String catalog, String schema, String table) {
        return this.insertByTable(catalog, schema, table, null);
    }

    /** 相当于 insert ... */
    <T> InsertOperation<T> insertByTable(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    // Update
    // ----------------------------------------------------------------------------------

    /** 相当于 update ... */
    default <T> EntityUpdateOperation<T> updateBySpace(Class<T> entityType) {
        return this.updateBySpace(entityType, "");
    }

    /** 相当于 update ... */
    <T> EntityUpdateOperation<T> updateBySpace(Class<T> entityType, String space);

    /** 相当于 update ... */
    default <T> EntityUpdateOperation<T> updateByTable(String table) {
        return this.updateByTable(null, null, table, null);
    }

    /** 相当于 update ... */
    default <T> EntityUpdateOperation<T> updateByTable(String catalog, String schema, String table) {
        return this.updateByTable(catalog, schema, table, null);
    }

    /** 相当于 update ... */
    <T> EntityUpdateOperation<T> updateByTable(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------

    /** 相当于 delete ... */
    default <T> EntityDeleteOperation<T> deleteBySpace(Class<T> entityType) {
        return this.deleteBySpace(entityType, "");
    }

    /** 相当于 delete ... */
    <T> EntityDeleteOperation<T> deleteBySpace(Class<T> entityType, String space);

    /** 相当于 delete ... */
    default <T> EntityDeleteOperation<T> deleteByTable(String table) {
        return this.deleteByTable(null, null, table, null);
    }

    /** 相当于 delete ... */
    default <T> EntityDeleteOperation<T> deleteByTable(String catalog, String schema, String table) {
        return this.deleteByTable(catalog, schema, table, null);
    }

    /** 相当于 delete ... */
    <T> EntityDeleteOperation<T> deleteByTable(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------

    /** 相当于 select ... */
    default <T> EntityQueryOperation<T> queryBySpace(Class<T> entityType) {
        return this.queryBySpace(entityType, "");
    }

    /** 相当于 select ... */
    <T> EntityQueryOperation<T> queryBySpace(Class<T> entityType, String space);

    /** 相当于 select ... */
    default <T> EntityQueryOperation<T> queryByTable(String table) {
        return this.queryByTable(null, null, table, null);
    }

    /** 相当于 select ... */
    default <T> EntityQueryOperation<T> queryByTable(String catalog, String schema, String table) {
        return this.queryByTable(catalog, schema, table, null);
    }

    /** 相当于 select ... */
    <T> EntityQueryOperation<T> queryByTable(String catalog, String schema, String table, String specifyName);

}
