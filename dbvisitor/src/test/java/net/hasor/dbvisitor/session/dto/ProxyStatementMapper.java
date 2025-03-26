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
import net.hasor.dbvisitor.mapper.*;

import java.util.List;

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