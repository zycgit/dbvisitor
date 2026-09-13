/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationInheritanceCase;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import org.junit.Before;

/** Mapping configuration checks; no database command is required. */
public class Elastic6AnnotationInheritanceTest extends AnnotationInheritanceCase {
    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
    }
}
