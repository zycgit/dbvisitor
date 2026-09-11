/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.dao.declarative;

import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.Update;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

/** Native templates are configured per session; parameter binding still uses the actual Mapper proxy. */
@SimpleMapper
public interface NativeParameterBindingMapper extends ParameterBindingMapper {
    @Override
    @Insert("@{macro, nxnInsertPosition}")
    int insertByPosition(Integer id, String name, Integer age);

    @Override
    @Update("@{macro, nxnUpdatePosition}")
    int updateByPosition(Integer age, Integer id);

    @Override
    @Insert("@{macro, nxnInsertNamed}")
    int insertWithParam(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age, @Param("email") String email);

    @Override
    @Insert("@{macro, nxnInsertBean}")
    int insertBean(UserInfo user);

    @Override
    @Insert("@{macro, nxnInsertNamed}")
    int insertByMap(Map<String, Object> params);

    @Override
    @Query("@{macro, nxnSelectId}")
    UserInfo selectById(@Param("id") Integer id);

    @Override
    @Query("@{macro, nxnSelectRange}")
    List<UserInfo> selectByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    @Override
    @Insert("@{macro, nxnInsertMixed}")
    int insertMixed(@Param("user") UserInfo user, @Param("email") String email);

    @Override
    @Insert("@{macro, nxnInsertReuse}")
    int insertWithReuse(@Param("id") Integer id, @Param("name") String name);

    @Override
    @Insert("@{macro, nxnInsertBean}")
    int insertWithManyParams(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age, @Param("email") String email, @Param("createTime") Date createTime, @Param("extra1") String extra1, @Param("extra2") String extra2);
}
