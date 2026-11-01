/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;
import java.sql.SQLException;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperCompositeKeyCase;
import net.hasor.dbvisitor.test.contract.material.model.UserRole;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.nxn.junit.NxnConcurrent;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@NxnConcurrent
public class Elastic7BaseMapperCompositeKeyTest extends BaseMapperCompositeKeyCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env(), UserRole.class, """
                {"properties": {"user_id": {"type": "integer"},"role_id": {"type": "integer"},
                "role_name": {"type": "keyword"},"create_time": {"type": "date"}}}
                """);
        this.lambdaTemplate = this.fixture.lambdaTemplate();
    }

    @Override
    @BeforeEach
    public void createBaseMapper() throws SQLException {
        setup();
        super.createBaseMapper();
    }

    @Override
    protected Session newSession() throws SQLException {
        return this.fixture.session();
    }

    @AfterEach
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
