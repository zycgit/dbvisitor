/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.keygen.UuidKeyCase;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid32User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid36User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuidStringUser;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic6UuidKeyTest extends UuidKeyCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();
    private       boolean              stringIndexCreated;

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        fixture.registry().loadEntityAsTable(KeyUuid32User.class, fixture.index());
        fixture.registry().loadEntityAsTable(KeyUuid36User.class, fixture.index());
        fixture.registry().loadEntityAsTable(KeyUuidStringUser.class, fixture.index() + "_strings");
        lambdaTemplate = fixture.lambdaTemplate();
    }

    @Override
    protected void ensureUuidStringTable() throws SQLException {
        jdbcTemplate.execute("PUT /" + fixture.index() + "_strings");
        stringIndexCreated = true;
    }

    @After
    public void closeFixture() throws SQLException {
        try {
            if (stringIndexCreated) {
                jdbcTemplate.execute("DELETE /" + fixture.index() + "_strings");
            }
        } finally {
            fixture.close();
        }
    }
}
