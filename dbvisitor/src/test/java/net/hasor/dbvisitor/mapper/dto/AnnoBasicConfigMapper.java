/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.dto;
import java.util.List;
import net.hasor.dbvisitor.mapper.*;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
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
