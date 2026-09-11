/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

/**
 * @RefMapper 接口，通过命名空间引用 XmlRefMapper.xml
 */
@RefMapper("/mapper/XmlRefMapper.xml")
public interface XmlRefMapperDao {

    int insertUser(@Param("id") int id, @Param("name") String name, @Param("age") int age, @Param("email") String email) throws SQLException;

    UserInfo selectById(@Param("id") int id) throws SQLException;

    List<UserInfo> selectAll() throws SQLException;

    int updateEmail(@Param("id") int id, @Param("email") String email) throws SQLException;

    int deleteById(@Param("id") int id) throws SQLException;

    List<UserInfo> selectByCondition(@Param("name") String name, @Param("minAge") Integer minAge) throws SQLException;

    List<UserInfo> selectByIds(@Param("ids") List<Integer> ids) throws SQLException;

    List<UserInfo> selectByAgeRange(@Param("range") Map<String, Object> range) throws SQLException;

    List<UserInfo> selectByBean(@Param("user") UserInfo user) throws SQLException;

    List<UserInfo> selectWithOrderBy(@Param("orderColumn") String orderColumn) throws SQLException;

    List<Map<String, Object>> selectAgeStats() throws SQLException;

    List<Map<String, Object>> selectAsMaps() throws SQLException;
}
