/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.keygen.AssignedKeyCase;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyNoneStrictUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyNoneUser;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7AssignedKeyTest extends AssignedKeyCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        fixture.registry().loadEntityAsTable(KeyNoneStrictUser.class, fixture.index());
        fixture.registry().loadEntityAsTable(KeyNoneUser.class, fixture.index());
        lambdaTemplate = fixture.lambdaTemplate();
    }

    @Override
    protected void ensureStrictNoneTable() {
        // The fixture's index already provides the physical document store.
    }

    @Override
    protected Integer countStrictKey(int id) throws SQLException {
        return Math.toIntExact(lambdaTemplate.query(KeyNoneStrictUser.class).eq(KeyNoneStrictUser::getId, id).queryForCount());
    }

    @Override
    protected void deleteDuplicateKey(int id) throws SQLException {
        lambdaTemplate.delete(KeyNoneUser.class).eq(KeyNoneUser::getId, id).doDelete();
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
