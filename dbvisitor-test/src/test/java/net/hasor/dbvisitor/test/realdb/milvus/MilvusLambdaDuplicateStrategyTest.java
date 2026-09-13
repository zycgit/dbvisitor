/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaDuplicateStrategyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusLambdaDuplicateStrategyTest extends LambdaDuplicateStrategyCase {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_INSERT_STRATEGY_REJECTED)
    public void unsupportedStrategiesShouldFailBeforeWritingAnything() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(1);
        user.setName("must-not-be-inserted");
        assertThrows(SQLException.class, () -> this.lambdaTemplate.insert(UserInfo.class)
                .onDuplicateStrategy(DuplicateKeyStrategy.Ignore).applyEntity(user).executeSumResult());
        user.setId(null);
        assertThrows(SQLException.class, () -> this.lambdaTemplate.insert(UserInfo.class)
                .onDuplicateStrategy(DuplicateKeyStrategy.Update).applyEntity(user).executeSumResult());
        assertEquals(0, this.lambdaTemplate.query(UserInfo.class).queryForCount());
    }
}
