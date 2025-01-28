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
package net.hasor.dbvisitor.mapper.dto;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.mapping.ResultMap;

import java.util.List;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
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
