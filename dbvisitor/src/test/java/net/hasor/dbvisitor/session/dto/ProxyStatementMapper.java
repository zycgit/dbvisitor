/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.session.dto;
import java.util.List;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.page.Page;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@SimpleMapper
public interface ProxyStatementMapper {
    @Query(value = "select * from user_info where user_name  = #{arg0};", statementType = StatementType.Prepared)
    List<UserInfo> selectList1(String arg);

    @Query(value = "select * from user_info where user_name  = ${arg0};", statementType = StatementType.Statement)
    List<UserInfo> selectList2(String arg);

    @SelectKeySql(value = "select last_insert_id()", keyProperty = "id", order = Order.After)
    @Insert("insert into auto_id(uid, name) values (?, ?);")
    int insertBean1(AutoIncrID info);

    @SelectKeySql(value = "select last_insert_id() as idid", keyProperty = "id", keyColumn = "idid", order = Order.After)
    @Insert("insert into auto_id(uid, name) values (?, ?);")
    int insertBean2(AutoIncrID info);

    @Query("select * from user_info")
    List<UserInfo> selectByPage(Page page);
}
