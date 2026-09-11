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
