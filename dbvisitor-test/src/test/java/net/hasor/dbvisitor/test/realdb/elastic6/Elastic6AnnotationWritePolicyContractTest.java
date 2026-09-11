/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeAnnotationMappingFixture;
import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationWritePolicyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic6AnnotationWritePolicyContractTest extends AnnotationWritePolicyContractTest {
    private final NativeAnnotationMappingFixture fixture = new NativeAnnotationMappingFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.lambdaTemplate = this.fixture.open(profile().env());
    }

    @Override
    protected void deleteRaw(int id) throws SQLException {
        this.fixture.delete(id);
    }

    @Override
    protected void updateRawEmail(int id, String email) throws SQLException {
        this.fixture.updateEmail(id, email);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
