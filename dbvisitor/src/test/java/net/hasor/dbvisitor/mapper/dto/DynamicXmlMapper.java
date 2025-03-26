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
 * @version 2013-12-10
 */
@RefMapper("/dbvisitor_coverage/basic_mapper/basic_xml_dynamic.xml")
public interface DynamicXmlMapper {
    List<UserInfo> bind_01(String sellerId);

    List<UserInfo> choose_01(String title, String content);

    List<UserInfo> foreach_03(List<ResourceType> resTypes);

    List<UserInfo> if_01(String ownerID, String ownerType);

    List<UserInfo> include_01(int eventType);

    int selectKey_01(UserInfo user);

    int set_04(UserInfo user);

    List<UserInfo> where_01(String name, String loginName);
}
