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
package net.hasor.dbvisitor.wrapper;
/**
 * 提供 lambda 方式生成 SQL。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-27
 */
public interface WrapperOperations {
    // ----------------------------------------------------------------------------------
    // Insert
    // ----------------------------------------------------------------------------------

    /** 相当于 insert ... */
    default <T> EntityInsertWrapper<T> insertByEntity(Class<T> entityType) {
        return this.insertBySpace(entityType, "");
    }

    /** 相当于 insert ... */
    <T> EntityInsertWrapper<T> insertBySpace(Class<T> entityType, String space);

    /** 相当于 insert ... */
    default <T> EntityInsertWrapper<T> insertByTable(String table) {
        return this.insertByTable(null, null, table, null);
    }

    /** 相当于 insert ... */
    default <T> EntityInsertWrapper<T> insertByTable(String catalog, String schema, String table) {
        return this.insertByTable(catalog, schema, table, null);
    }

    /** 相当于 insert ... */
    <T> EntityInsertWrapper<T> insertByTable(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    // Update
    // ----------------------------------------------------------------------------------

    /** 相当于 update ... */
    default <T> EntityUpdateWrapper<T> updateByEntity(Class<T> entityType) {
        return this.updateBySpace(entityType, "");
    }

    /** 相当于 update ... */
    <T> EntityUpdateWrapper<T> updateBySpace(Class<T> entityType, String space);

    /** 相当于 update ... */
    default <T> EntityUpdateWrapper<T> updateByTable(String table) {
        return this.updateByTable(null, null, table, null);
    }

    /** 相当于 update ... */
    default <T> EntityUpdateWrapper<T> updateByTable(String catalog, String schema, String table) {
        return this.updateByTable(catalog, schema, table, null);
    }

    /** 相当于 update ... */
    <T> EntityUpdateWrapper<T> updateByTable(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------

    /** 相当于 delete ... */
    default <T> EntityDeleteWrapper<T> deleteByEntity(Class<T> entityType) {
        return this.deleteBySpace(entityType, "");
    }

    /** 相当于 delete ... */
    <T> EntityDeleteWrapper<T> deleteBySpace(Class<T> entityType, String space);

    /** 相当于 delete ... */
    default <T> EntityDeleteWrapper<T> deleteByTable(String table) {
        return this.deleteByTable(null, null, table, null);
    }

    /** 相当于 delete ... */
    default <T> EntityDeleteWrapper<T> deleteByTable(String catalog, String schema, String table) {
        return this.deleteByTable(catalog, schema, table, null);
    }

    /** 相当于 delete ... */
    <T> EntityDeleteWrapper<T> deleteByTable(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------

    /** 相当于 select ... */
    default <T> EntityQueryWrapper<T> queryByEntity(Class<T> entityType) {
        return this.queryBySpace(entityType, "");
    }

    /** 相当于 select ... */
    <T> EntityQueryWrapper<T> queryBySpace(Class<T> entityType, String space);

    /** 相当于 select ... */
    default <T> EntityQueryWrapper<T> queryByTable(String table) {
        return this.queryByTable(null, null, table, null);
    }

    /** 相当于 select ... */
    default <T> EntityQueryWrapper<T> queryByTable(String catalog, String schema, String table) {
        return this.queryByTable(catalog, schema, table, null);
    }

    /** 相当于 select ... */
    <T> EntityQueryWrapper<T> queryByTable(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------

    default MapInsertWrapper freedomInsert(String table) {
        return this.freedomInsert(null, null, table);
    }

    MapInsertWrapper freedomInsert(String catalog, String schema, String table);

    default MapUpdateWrapper freedomUpdate(String table) {
        return this.freedomUpdate(null, null, table);
    }

    MapUpdateWrapper freedomUpdate(String catalog, String schema, String table);

    default MapDeleteWrapper freedomDelete(String table) {
        return this.freedomDelete(null, null, table);
    }

    MapDeleteWrapper freedomDelete(String catalog, String schema, String table);

    default MapQueryWrapper freedomQuery(String table) {
        return this.freedomQuery(null, null, table);
    }

    MapQueryWrapper freedomQuery(String catalog, String schema, String table);
}
