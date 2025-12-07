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
import java.util.List;
import net.hasor.dbvisitor.mapper.RefMapper;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@RefMapper("/dbvisitor_coverage/basic_mapper/basic_mapper_two.xml")
public interface ApiBasicConfigTwoMapper {
    List<UserInfo> configQuery1(String abc);

    List<UserInfo> configQuery2(String abc);

    List<UserInfo> configQuery3(String abc);

    List<UserInfo> configQuery4(String abc);

    List<UserInfo> configQuery5(String abc);

    long configInsert(UserInfo info);

    long configInsertSelectKey(UserInfo info);

    long configUpdate(int id, String uuid);

    long configDelete(int id);

    void configExecute();
}
