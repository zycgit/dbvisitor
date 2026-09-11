/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
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
