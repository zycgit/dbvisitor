/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeAnnotationMappingFixture;
import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationExplicitMappingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import org.junit.After;
import org.junit.Before;

public class MongoAnnotationExplicitMappingContractTest extends AnnotationExplicitMappingContractTest {
    private final NativeAnnotationMappingFixture fixture = new NativeAnnotationMappingFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.lambdaTemplate = this.fixture.open(profile().env());
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
