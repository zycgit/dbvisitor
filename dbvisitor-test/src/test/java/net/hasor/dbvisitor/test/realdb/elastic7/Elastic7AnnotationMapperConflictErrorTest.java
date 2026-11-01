/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperConflictErrorCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class Elastic7AnnotationMapperConflictErrorTest extends AnnotationMapperConflictErrorCase {
    private final Elastic7SessionMapperFixture fixture = new Elastic7SessionMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
    }

    @Override
    @BeforeEach
    public void createAnnotationMapper() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        this.mapper = fixture.session().createMapper(Elastic7SessionMapperFixture.NativeBoundary.class);
    }

    @AfterEach
    public void closeBoundaryFixture() throws Exception {
        fixture.close();
    }
}
