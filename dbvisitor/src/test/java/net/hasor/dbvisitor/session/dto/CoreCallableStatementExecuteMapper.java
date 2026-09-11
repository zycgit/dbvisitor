/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.session.dto;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Execute;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.StatementType;
import net.hasor.dbvisitor.page.Page;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@SimpleMapper
public interface CoreCallableStatementExecuteMapper {
    @Execute(statementType = StatementType.Callable, //
            value = "call proc_select_cross_table_for_stat(#{arg0,jdbcType=varchar},#{arg1,mode=inout,jdbcType=varchar},#{res1,mode=cursor},#{res2,mode=cursor})",//
            bindOut = { "arg1", "res1", "res2" })
    Map<String, Object> executeCall1(String arg0, String arg1);

    @Execute(statementType = StatementType.Callable, //
            value = "call proc_select_cross_table_for_stat(#{arg0,jdbcType=varchar},#{arg1,mode=inout,jdbcType=varchar},#{res1,mode=cursor},#{res2,mode=cursor})")
    Map<String, Object> executeCall2(String arg0, String arg1);

    @Query(statementType = StatementType.Callable, value = //
            "select * 1")
    List<UserInfo> selectByPage(Page page);
}
