/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.dao.declarative;

import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.Update;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

/** Native command templates for the shared CRUD assertions; binding uses the real Mapper proxy. */
@SimpleMapper
public interface NativeCrudMapper extends AnnotationTestMapper {
    @Override
    @Insert("@{macro, nxnCrudInsertBean}")
    int insertUser(UserInfo user);

    @Override
    @Insert("@{macro, nxnCrudInsertParams}")
    int insertUserWithParams(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age, @Param("email") String email);

    @Override
    @Update("@{macro, nxnCrudUpdateAge}")
    int updateUserAge(@Param("id") Integer id, @Param("age") Integer age);

    @Override
    @Update("@{macro, nxnCrudUpdateInfo}")
    int updateUserInfo(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age);

    @Override
    @Delete("@{macro, nxnCrudDelete}")
    int deleteById(@Param("id") Integer id);

    @Override
    @Query("@{macro, nxnCrudSelect}")
    UserInfo selectById(@Param("id") Integer id);
}
