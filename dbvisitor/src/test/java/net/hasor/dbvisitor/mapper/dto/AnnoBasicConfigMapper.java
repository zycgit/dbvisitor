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
import net.hasor.dbvisitor.mapper.*;

import java.util.List;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@SimpleMapper
public interface AnnoBasicConfigMapper {
    @Query(value = "select * from console_job where aac = #{abc}",//
            statementType = StatementType.Callable,     //
            timeout = 123,                              //
            fetchSize = 512,                            //
            resultSetType = ResultSetType.FORWARD_ONLY, //
            bindOut = { "out1", "out2" })
    List<UserInfo> configQuery1(String abc);

    @Query(value = "select * from console_job where aac = #{abc}",  //
            statementType = StatementType.Callable,                 //
            timeout = 123,                                          //
            fetchSize = 512,                                        //
            resultSetType = ResultSetType.FORWARD_ONLY,             //
            resultSetExtractor = UserNameResultSetExtractor.class,  //
            bindOut = { "out1", "out2" })
    List<UserInfo> configQuery2(String abc);

    @Query(value = "select * from console_job where aac = #{abc}",//
            statementType = StatementType.Callable,         //
            timeout = 123,                                  //
            fetchSize = 512,                                //
            resultSetType = ResultSetType.FORWARD_ONLY,     //
            resultRowCallback = UserNameRowCallback.class,  //
            bindOut = { "out1", "out2" })
    List<UserInfo> configQuery3(String abc);

    @Query(value = "select * from console_job where aac = #{abc}",//
            statementType = StatementType.Callable,     //
            timeout = 123,                              //
            fetchSize = 512,                            //
            resultSetType = ResultSetType.FORWARD_ONLY, //
            resultRowMapper = UserNameRowMapper.class,  //
            bindOut = { "out1", "out2" })
    List<UserInfo> configQuery4(String abc);

    @Insert(value = "insert into console_job (uid,name,login) values (#{info.userUuid}, #{info.name}, #{info.loginName})",//
            statementType = StatementType.Callable, //
            timeout = 123,                          //
            useGeneratedKeys = true,                //
            keyProperty = "numId",                  //
            keyColumn = "num_id")
    long configInsert(UserInfo info);

    @SelectKeySql(value = "select last_insert_id()",//
            statementType = StatementType.Callable, //
            timeout = 123,                          //
            fetchSize = 512,                        //
            resultSetType = ResultSetType.FORWARD_ONLY, //
            keyProperty = "userUuid",               //
            keyColumn = "uid",                      //
            order = Order.After)
    @Insert(value = "insert into console_job (uid,name,login) values (#{info.userUuid}, #{info.name}, #{info.loginName})",//
            statementType = StatementType.Callable, //
            timeout = 123,                          //
            useGeneratedKeys = true,                //
            keyProperty = "numId",                  //
            keyColumn = "num_id")
    long configInsertSelectKey(UserInfo info);

    @Update(value = "update console_job set uid = #{uuid} where id = #{id}",//
            statementType = StatementType.Callable, //
            timeout = 123)
    long configUpdate(int id, String uuid);

    @Delete(value = "delete console_job where id = #{id}",//
            statementType = StatementType.Callable, //
            timeout = 123)
    long configDelete(int id);

    @Execute(value = "create table console_job (uid int,name varchar(200),login varchar(200))",//
            statementType = StatementType.Callable, //
            timeout = 123)
    void configExecute();
}
