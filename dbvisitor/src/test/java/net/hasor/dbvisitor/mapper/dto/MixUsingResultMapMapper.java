/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.dto;
import java.util.List;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.mapping.ResultMap;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@RefMapper("/dbvisitor_coverage/basic_mapper/basic_mapper_2.xml")
public interface MixUsingResultMapMapper {
    @Query("select 1")
    @ResultMap("userInfo")
    List<UserInfo> usingMethodResultMap1(String abc);

    @Query("select 1")
    List<UserInfoUsingMap1> usingMethodResultMap2(String abc);

    @Query("select 1")
    List<UserInfoUsingMap2> usingMethodResultMap3(String abc);
}
