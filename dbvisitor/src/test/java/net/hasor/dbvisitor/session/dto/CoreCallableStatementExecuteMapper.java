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
package net.hasor.dbvisitor.session.dto;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.mapper.Execute;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

import java.util.List;
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@SimpleMapper
public interface CoreCallableStatementExecuteMapper {
    @Execute(value = {//
            "{call proc_select_cross_table_for_stat(#{arg0,jdbcType=varchar},#{arg1,mode=out,jdbcType=varchar})}",//
            "      @{resultSet,name=res1}",   //
            "      @{resultSet,name=res2}" }, //
            bindOut = { "arg1", "res1", "res2" })
    Map<String, Object> executeCall1(String arg);

    @Execute("{call proc_select_cross_table_for_stat(#{arg0,jdbcType=varchar},#{arg1,mode=out,jdbcType=varchar})}")
    Map<String, Object> executeCall2(String arg);

    @Query("select * 1")
    List<UserInfo> selectByPage(Page page);
}