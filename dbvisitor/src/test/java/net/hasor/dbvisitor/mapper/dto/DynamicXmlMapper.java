/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.dto;
import java.util.List;
import net.hasor.dbvisitor.mapper.RefMapper;

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
