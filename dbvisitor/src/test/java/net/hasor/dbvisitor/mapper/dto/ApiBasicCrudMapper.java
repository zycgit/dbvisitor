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
import net.hasor.dbvisitor.mapper.RefMapper;

import java.util.List;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@RefMapper("/dbvisitor_coverage/basic_mapping/basic_mapper_1.xml")
public interface ApiBasicCrudMapper {
    List<UserInfo> selectList(String abc);

    UserInfo selectOne(String title, String content);

    long insertBean(UserInfo info);

    long updateBean(int id, String uuid);

    long deleteBean(int id);

    void createTable();
}
