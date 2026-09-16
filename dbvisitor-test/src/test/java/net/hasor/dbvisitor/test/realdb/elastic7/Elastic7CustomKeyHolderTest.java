/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.keygen.CustomKeyHolderCase;
import net.hasor.dbvisitor.test.contract.material.model.keygen.*;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7CustomKeyHolderTest extends CustomKeyHolderCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        fixture.registry().loadEntityAsTable(KeyHolderUser.class, fixture.index());
        fixture.registry().loadEntityAsTable(KeyHolderBothUser.class, fixture.index());
        fixture.registry().loadEntityAsTable(KeyHolderContextUser.class, fixture.index());
        fixture.registry().loadEntityAsTable(KeyHolderConnectionUser.class, fixture.index());
        fixture.registry().loadEntityAsTable(KeyHolderFailingUser.class, fixture.index());
        fixture.registry().loadEntityAsTable(KeyHolderSqlExceptionUser.class, fixture.index());
        lambdaTemplate = fixture.lambdaTemplate();
    }

    @Override
    protected Integer countStoredKey(int id) throws SQLException {
        return Math.toIntExact(lambdaTemplate.query(KeyHolderBothUser.class).eq(KeyHolderBothUser::getId, id).queryForCount());
    }

    @Override
    protected void deleteUserInfoIds(int first, int second) throws SQLException {
        lambdaTemplate.delete(KeyHolderBothUser.class).in(KeyHolderBothUser::getId, java.util.Arrays.asList(first, second)).doDelete();
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
