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
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@RefMapper("/dbvisitor_coverage/basic_session/basic_mapper_file.xml")
public interface BasicMapperByFile {
    int createUser(UserInfo tbUser);

    int initUser();

    List<UserInfo> listUserList_1(@Param("abc") String name);

    List<UserInfo2> listUserList_2(@Param("abc") String name);

    Map<String, Object> callSelectUser(Map<String, Object> args);

    int insertAutoID_1(AutoIncrID autoId);

    int insertAutoID_2(AutoIncrID autoId);
}
