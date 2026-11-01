/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeDocumentQueryFixture;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaEntityListInsertCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class MongoLambdaEntityListInsertTest extends LambdaEntityListInsertCase {
    private final NativeDocumentQueryFixture fixture = new NativeDocumentQueryFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env());
        this.lambdaTemplate = this.fixture.lambdaTemplate();
    }

    @AfterEach
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
