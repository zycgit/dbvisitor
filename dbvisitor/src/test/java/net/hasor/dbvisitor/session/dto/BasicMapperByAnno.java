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
import net.hasor.dbvisitor.mapper.*;

import java.util.List;
import java.util.Map;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@SimpleMapper
public interface BasicMapperByAnno {

    @Segment("user_uuid, user_name, login_name, login_password, email, seq, register_time")
    void user_do_allColumns();

    @Insert({                                                               //
            "insert into user_info (@{macro,user_do_allColumns})",          //
            "values (#{userUuid}, #{name}, #{loginName}, #{loginPassword}, #{email}, #{seq}, #{registerTime})" })
    int createUser(UserInfo tbUser);

    @Insert({                                                               //
            "insert into user_info (@{macro,user_do_allColumns})",          //
            "values",                                                       //
            "('11', '12', '13', '14', '15', 16, '2021-07-20 00:00:00'),",   //
            "('21', '22', '23', '24', '25', 26, '2021-07-20 00:00:00')" })
    int initUser();

    @Query("select @{macro,user_do_allColumns} from user_info where user_name = #{abc}")
    List<UserInfoMap> listUserList_1(@Param("abc") String name);

    @Query("select @{macro,user_do_allColumns} from user_info where user_name = #{abc}")
    List<UserInfo2> listUserList_2(@Param("abc") String name);

    @Call("{call proc_select_user(#{abc, mode=out, jdbcType=decimal})}")
    Map<String, Object> callSelectUser(Map<String, Object> args);

    @SelectKeySql(value = "SELECT LAST_INSERT_ID()", keyProperty = "id", order = Order.After)
    @Insert("insert into auto_id(uid, name) values (#{uid}, #{name});")
    int insertAutoID_1(AutoIncrID autoId);

    @SelectKeySql(value = "SELECT LAST_INSERT_ID() as cc", keyProperty = "id", keyColumn = "cc", order = Order.After)
    @Insert("insert into auto_id(uid, name) values (#{uid}, #{name});")
    int insertAutoID_2(AutoIncrID autoId);
}
