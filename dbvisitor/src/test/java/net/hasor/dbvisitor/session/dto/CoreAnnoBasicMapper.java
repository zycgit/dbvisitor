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
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

import java.util.List;
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@SimpleMapper
public interface CoreAnnoBasicMapper {
    @Query(value = { "set @userName = convert(? USING utf8);       @{resultUpdate,name=upd}",//
            "select * from user_info where user_name  = @userName; @{resultSet,name=res1,javaType=net.hasor.dbvisitor.session.dto.UserInfo}",   //
            "select * from user_info where user_name != @userName; @{resultSet,name=res2,javaType=net.hasor.dbvisitor.session.dto.UserInfo}" }, //
            bindOut = { "res1", "res2" })
    Map<String, Object> selectList1(String arg);

    @Query("select * from user_info where user_name  = ?;")
    List<UserInfo> selectList2(String arg);

    @Query("select * from user_info where user_uuid = ?")
    Map<String, Object> queryById(UserInfo info);

    @Insert("insert into user_info(user_uuid, user_name, login_name, login_password, email, seq, register_time) values (?, ?, ?, ?, ?, ?, ?);")
    int insertBean(UserInfo info);
}