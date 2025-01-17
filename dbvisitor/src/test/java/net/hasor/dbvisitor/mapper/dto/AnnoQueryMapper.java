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
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

import java.util.List;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@SimpleMapper
public interface AnnoQueryMapper {
    @Query(value = "select * from console_job where aac = #{abc}")
    List<UserInfo> testSelectArg(String abc);

    @Query(value = "select * from t_blog where title = #{title} and content = #{content}")
    List<UserInfo> testChoose(String title, String content);

    @Query(value = "select * from alert_detail where alert_detail.event_type in @{in, arg0}")
    List<UserInfo> testForeach(List<String> eventTypes);

    @Query(value = "select * from project_info where status = 2 @{and, owner_id = :ownerID} @{and, owner_type = ownerType} order by name asc")
    List<UserInfo> testIf(String ownerID, String ownerType);
}
