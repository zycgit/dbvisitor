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
 * @version 2020-10-27
 */
public interface WrapperOperations {
    // ----------------------------------------------------------------------------------
    // Insert
    // ----------------------------------------------------------------------------------

    /** 相当于 insert ... */
    default <T> EntityInsertWrapper<T> insert(Class<T> entityType) {
        return this.insert(entityType, "");
    }

    /** 相当于 insert ... */
    <T> EntityInsertWrapper<T> insert(Class<T> entityType, String space);

    /** 相当于 insert ... */
    default <T> EntityInsertWrapper<T> insert(String table) {
        return this.insert(null, null, table, null);
    }

    /** 相当于 insert ... */
    default <T> EntityInsertWrapper<T> insert(String catalog, String schema, String table) {
        return this.insert(catalog, schema, table, null);
    }

    /** 相当于 insert ... */
    <T> EntityInsertWrapper<T> insert(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    // Update
    // ----------------------------------------------------------------------------------

    /** 相当于 update ... */
    default <T> EntityUpdateWrapper<T> update(Class<T> entityType) {
        return this.update(entityType, "");
    }

    /** 相当于 update ... */
    <T> EntityUpdateWrapper<T> update(Class<T> entityType, String space);

    /** 相当于 update ... */
    default <T> EntityUpdateWrapper<T> update(String table) {
        return this.update(null, null, table, null);
    }

    /** 相当于 update ... */
    default <T> EntityUpdateWrapper<T> update(String catalog, String schema, String table) {
        return this.update(catalog, schema, table, null);
    }

    /** 相当于 update ... */
    <T> EntityUpdateWrapper<T> update(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------

    /** 相当于 delete ... */
    default <T> EntityDeleteWrapper<T> delete(Class<T> entityType) {
        return this.delete(entityType, "");
    }

    /** 相当于 delete ... */
    <T> EntityDeleteWrapper<T> delete(Class<T> entityType, String space);

    /** 相当于 delete ... */
    default <T> EntityDeleteWrapper<T> delete(String table) {
        return this.delete(null, null, table, null);
    }

    /** 相当于 delete ... */
    default <T> EntityDeleteWrapper<T> delete(String catalog, String schema, String table) {
        return this.delete(catalog, schema, table, null);
    }

    /** 相当于 delete ... */
    <T> EntityDeleteWrapper<T> delete(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------

    /** 相当于 select ... */
    default <T> EntityQueryWrapper<T> query(Class<T> entityType) {
        return this.query(entityType, "");
    }

    /** 相当于 select ... */
    <T> EntityQueryWrapper<T> query(Class<T> entityType, String space);

    /** 相当于 select ... */
    default <T> EntityQueryWrapper<T> query(String table) {
        return this.query(null, null, table, null);
    }

    /** 相当于 select ... */
    default <T> EntityQueryWrapper<T> query(String catalog, String schema, String table) {
        return this.query(catalog, schema, table, null);
    }

    /** 相当于 select ... */
    <T> EntityQueryWrapper<T> query(String catalog, String schema, String table, String specifyName);

    // ----------------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------------

    default MapInsertWrapper insertFreedom(String table) {
        return this.insertFreedom(null, null, table);
    }

    MapInsertWrapper insertFreedom(String catalog, String schema, String table);

    default MapUpdateWrapper updateFreedom(String table) {
        return this.updateFreedom(null, null, table);
    }

    MapUpdateWrapper updateFreedom(String catalog, String schema, String table);

    default MapDeleteWrapper deleteFreedom(String table) {
        return this.deleteFreedom(null, null, table);
    }

    MapDeleteWrapper deleteFreedom(String catalog, String schema, String table);

    default MapQueryWrapper queryFreedom(String table) {
        return this.queryFreedom(null, null, table);
    }

    MapQueryWrapper queryFreedom(String catalog, String schema, String table);
}
