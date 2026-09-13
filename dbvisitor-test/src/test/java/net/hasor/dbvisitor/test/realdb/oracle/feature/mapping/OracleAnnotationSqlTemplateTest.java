/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.oracle.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationSqlTemplateCase;
import net.hasor.dbvisitor.test.contract.material.model.annotation.AbstractMd5User;
import net.hasor.dbvisitor.test.contract.material.model.annotation.OracleMd5User;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleAnnotationSqlTemplateTest extends AnnotationSqlTemplateCase {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }

    @Override
    protected Class<? extends AbstractMd5User> md5UserType() {
        return OracleMd5User.class;
    }
}
