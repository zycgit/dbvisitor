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
@RefMapper("/dbvisitor_coverage/basic_mapper/basic_mapper_1.xml")
public interface ApiBasicCrudMapper {
    List<UserInfo> selectList(String abc);

    UserInfo selectOne(String title, String content);

    long insertBean(UserInfo info);

    long updateBean(int id, String uuid);

    long deleteBean(int id);

    void createTable();
}
