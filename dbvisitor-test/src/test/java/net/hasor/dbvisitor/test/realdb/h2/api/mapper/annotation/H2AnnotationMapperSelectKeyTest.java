/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.h2.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperSelectKeyCase;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;
import net.hasor.dbvisitor.test.realdb.h2.material.H2AnnotationAttributesMapper;

public class H2AnnotationMapperSelectKeyTest extends AnnotationMapperSelectKeyCase {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }

    @Override
    protected Class<? extends AnnotationAttributesMapper> mapperType() {
        return H2AnnotationAttributesMapper.class;
    }
}
