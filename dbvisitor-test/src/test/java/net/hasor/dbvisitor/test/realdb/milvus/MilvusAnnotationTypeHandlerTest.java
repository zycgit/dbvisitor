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
import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationTypeHandlerCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Keeps shared type-handler mappings and assertions; only the native collection fixture differs. */
public class MilvusAnnotationTypeHandlerTest extends AnnotationTypeHandlerCase {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void createLambdaTemplate() throws SQLException {
        // Initialize the fixture before the inherited mapper setup, without replacing the shared static DataSource.
        setup();
        super.createLambdaTemplate();
    }

    @Override
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
