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
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@SimpleMapper
public interface CoreStatementExecuteMapper {
    @Execute(statementType = StatementType.Statement, value = { //
            "set @userName = convert(${arg0} USING utf8);          @{resultUpdate,name=upd}",//
            "select * from user_info where user_name  = @userName; @{resultSet,name=res1,javaType=net.hasor.dbvisitor.session.dto.UserInfo}",   //
            "select * from user_info where user_name != @userName; @{resultSet,name=res2,javaType=net.hasor.dbvisitor.session.dto.UserInfo}" }, //
            bindOut = { "res1", "res2" })
    Map<String, Object> selectList1(String arg);

    @Query(statementType = StatementType.Statement, value =//
            "select * from user_info where user_name = ${arg0};")
    List<UserInfo> selectList2(String arg);

    @Query(statementType = StatementType.Statement, value =//
            "select * from user_info where user_uuid = ${arg0}")
    Map<String, Object> queryById(UserInfo info);

    @Query(statementType = StatementType.Statement, value =//
            "select count(*) from user_info;")
    int selectCount();

    @Insert(statementType = StatementType.Statement, value =//
            "insert into user_info(user_uuid, user_name, login_name, login_password, email, seq, register_time) values (${arg0}, ${arg1}, ${arg2}, ${arg3}, ${arg4}, ${arg5}, ${arg6});")
    int insertBean(UserInfo info);

    @Update(statementType = StatementType.Statement, value =//
            "update user_info set login_name = ${arg0};")
    int updateBean(String arg);

    @Delete(statementType = StatementType.Statement, value =//
            "delete from user_info where login_name = ${arg0};")
    void deleteBean(String arg);

    @Query(statementType = StatementType.Statement, value =//
            "select * from user_info")
    List<UserInfo> selectByPage(Page page);
}