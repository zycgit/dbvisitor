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
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@RefMapper("/mapper/XmlRefPageMapper.xml")
public interface XmlRefPageMapper {
    List<UserInfo> selectPage(@Param("minAge") int minAge, Page page) throws SQLException;
}
