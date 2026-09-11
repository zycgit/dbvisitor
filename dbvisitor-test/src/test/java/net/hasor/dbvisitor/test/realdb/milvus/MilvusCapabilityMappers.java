/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationTestMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoLongUser;

/** Native query material; the shared tests still assert the original result shapes and attributes. */
final class MilvusCapabilityMappers {
    private MilvusCapabilityMappers() {
    }

    @SimpleMapper
    public interface GeneratedKeys {
        @Insert(value = "INSERT INTO user_info (name, age, create_time) VALUES (#{name}, #{age}, #{createTime})",
                useGeneratedKeys = true, keyProperty = "id")
        int generated(KeyAutoLongUser user);

        @Insert(value = "INSERT INTO user_manual (id, name, age, create_time) VALUES (#{id}, #{name}, #{age}, #{createTime})",
                useGeneratedKeys = false)
        int explicit(KeyAutoLongUser user);

        @Query("SELECT id, name, age, create_time FROM user_info WHERE id = #{id}")
        KeyAutoLongUser load(@Param("id") Long id);

        @Query("SELECT id, name, age, create_time FROM user_manual WHERE id = #{id}")
        KeyAutoLongUser loadExplicit(@Param("id") Long id);
    }

    @SimpleMapper
    public interface Results extends AnnotationTestMapper {
        @Override
        @Query("SELECT * FROM user_info WHERE age = #{age}")
        List<UserInfo> selectByAge(@Param("age") Integer age);

        // A fixed test prefix checks list mapping independently of templated LIKE support.
        @Override
        @Query("SELECT * FROM user_info WHERE name LIKE 'AnnoQuery%'")
        List<UserInfo> selectByNameLike(@Param("pattern") String pattern);
    }

    @SimpleMapper
    public interface Attributes extends AnnotationAttributesMapper {
        // The private collection contains exactly the shared AttrNxn fixture rows.
        @Override
        @Query(value = "SELECT * FROM user_info", fetchSize = 256)
        List<UserInfo> selectWithDefaultFetchSize(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info", fetchSize = 10)
        List<UserInfo> selectWithSmallFetchSize(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info", fetchSize = 1000)
        List<UserInfo> selectWithLargeFetchSize(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info", fetchSize = 1)
        List<UserInfo> selectWithFetchSizeOne(@Param("pattern") String pattern);
    }
}
