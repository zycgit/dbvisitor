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
package net.hasor.test.dal;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.test.dto.UserInfo2;

import java.util.List;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@SimpleMapper
public interface Mapper2Dal {
    @Query(value = "<bind name=\"abc\" value=\"sellerId + 'abc'\"/> SELECT * FROM console_job WHERE aac = #{abc}")
    List<UserInfo2> testBind(String abc);

    @Query(value = "select * from t_blog" + //
            "<where>" + //
            "    <choose>" + //
            "        <when test=\"title != null\">and title = #{title}</when>" + //
            "        <when test=\"content != null\">and content = #{content}</when>" + //
            "        <otherwise>and owner = \"owner1\"</otherwise>" + //
            "    </choose>" + //
            "</where>")
    List<UserInfo2> testChoose(String title, String content);

    @Query(value = "SELECT * FROM alert_detail WHERE alert_detail.event_type IN " +//
            "<foreach collection='eventTypes' item='eventType' separator=',' open='(' close=')'>" +//
            "    #{eventType,javaType=net.hasor.test.dto.CharacterSensitiveEnum}" +//
            "</foreach>")
    List<UserInfo2> testForeach(List<String> eventTypes);

    @Query(value = "select * from PROJECT_INFO where 1=1 and status = 2 " + //
            "<if test='ownerID != null and ownerType !=null'>" + //
            "    and owner_id = #{ownerID}" + //
            "    and owner_type = #{ownerType}" + //
            "</if> order by name asc")
    List<UserInfo2> testIf(String ownerID, String ownerType);
}
