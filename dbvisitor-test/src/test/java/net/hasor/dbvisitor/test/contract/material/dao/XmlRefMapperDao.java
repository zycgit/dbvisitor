/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

/**
 * @RefMapper 接口，通过命名空间引用 XmlRefMapper.xml
 */
@RefMapper("/mapper/XmlRefMapper.xml")
public interface XmlRefMapperDao extends XmlRefMapperOperations {
    List<UserInfo> selectByCondition(@Param("name") String name, @Param("minAge") Integer minAge) throws SQLException;

    List<Map<String, Object>> selectAgeStats() throws SQLException;
}
