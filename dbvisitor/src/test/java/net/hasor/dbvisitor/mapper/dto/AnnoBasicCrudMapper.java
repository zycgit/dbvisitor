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
public interface AnnoBasicCrudMapper {
    @Query("select * from console_job where aac = #{abc}")
    List<UserInfo> selectList(String abc);

    @Query({ "select * from t_blog",//
            "where title = #{title} and content = #{content}" })
    UserInfo selectOne(String title, String content);

    @SelectKeySql(value = "select last_insert_id()", keyProperty = "userUuid", keyColumn = "uid", order = Order.After)
    @Insert("insert into console_job (uid,name,login) values (#{info.userUuid}, #{info.name}, #{info.loginName})")
    long insertBean(UserInfo info);

    @Update("update console_job set uid = #{uuid} where id = #{id}")
    long updateBean(int id, String uuid);

    @Delete("delete console_job where id = #{id}")
    long deleteBean(int id);

    @Execute("create table console_job (uid int,name varchar(200),login varchar(200))")
    void createTable();
}
