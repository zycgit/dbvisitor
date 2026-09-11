/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus.material;

import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.contract.material.dao.XmlRefMapperDao;

@RefMapper("/realdb/milvus/material/XmlRefMapper.xml")
public interface MilvusXmlRefMapperDao extends XmlRefMapperDao {
}
