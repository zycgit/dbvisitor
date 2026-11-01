/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;
import java.sql.SQLException;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.feature.keygen.AssignedKeyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class MilvusAssignedKeyTest extends AssignedKeyCase {
    private final MilvusCapabilityFixture fixture = new MilvusCapabilityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.fixture.userTable("user_info", "id INT64 PRIMARY KEY");
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected void ensureStrictNoneTable() throws SQLException {
        this.fixture.userTable("user_strict_none", "id INT64 PRIMARY KEY");
    }

    @Override
    protected boolean isNotNullMessage(Throwable error) {
        return super.isNotNullMessage(error) || lowerMessage(error).contains("the field: id is not provided");
    }

    @AfterEach
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
