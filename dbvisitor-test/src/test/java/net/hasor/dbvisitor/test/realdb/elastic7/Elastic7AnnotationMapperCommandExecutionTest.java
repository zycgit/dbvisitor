/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperCommandExecutionCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic7AnnotationMapperCommandExecutionTest extends AnnotationMapperCommandExecutionCase {
    private final Elastic7SessionMapperFixture fixture = new Elastic7SessionMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        this.mapper = fixture.session().createMapper(Elastic7SessionMapperFixture.NativeBoundary.class);
    }

    @Override
    protected String createSimpleTempTableSql(String tableName) {
        return "PUT /" + fixture.temporaryIndex() + " {}";
    }

    @Override
    protected void dropTableIfExists(String tableName) throws SQLException {
        try {
            jdbcTemplate.executeUpdate("DELETE /" + fixture.temporaryIndex());
        } catch (SQLException error) {
            if (!String.valueOf(error.getMessage()).contains("index_not_found_exception") && !String.valueOf(error.getMessage()).contains("no such index")) {
                throw error;
            }
        }
    }

    @After
    public void closeBoundaryFixture() throws Exception {
        try {
            dropTableIfExists("temp_anno_test");
        } finally {
            fixture.close();
        }
    }
}
