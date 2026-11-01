/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.session.SessionStatementParameterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class MilvusSessionStatementParameterTest extends SessionStatementParameterCase {
    private final MilvusStatementFixture fixture = new MilvusStatementFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    public void setup() throws SQLException {
        this.fixture.open();
    }

    @Override
    @BeforeEach
    public void createStatementSession() throws Exception {
        this.setup();
        this.session = this.fixture.session("/realdb/milvus/material/SessionStatements.xml");
    }

    @AfterEach
    public void cleanupFixture() throws Exception {
        this.fixture.close();
    }
}
