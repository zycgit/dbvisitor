/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mysql.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperSelectKeyCase;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;
import net.hasor.dbvisitor.test.realdb.mysql.material.MySqlSelectKeyMapper;

public class MySqlAnnotationMapperSelectKeyTest extends AnnotationMapperSelectKeyCase {
    @Override
    protected Class<? extends AnnotationAttributesMapper> mapperType() {
        return MySqlSelectKeyMapper.class;
    }

    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
