/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.io.IOException;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.session.SessionFactoryCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

public class Elastic7SessionFactoryTest extends SessionFactoryCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws IOException, SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
        this.dataSource = Elastic7SessionDataSource.open(profile().env());
    }

    @Override
    protected Configuration newConfiguration(Options options) {
        Configuration configuration = new Configuration(options);
        configuration.getMappingRegistry().loadEntityAsTable(UserInfo.class, fixture.index());
        return configuration;
    }

    @Override
    protected String jdbcInsertCommand() {
        return "POST /" + fixture.index() + "/_doc {\"id\": ?,\"name\": ?,\"age\": ?}";
    }

    @Override
    protected String jdbcCountCommand() {
        return "POST /" + fixture.index() + "/_count";
    }

    @Override
    protected Object[] jdbcCountParameters() {
        return new Object[0];
    }

    @Override
    protected String jdbcNameCommand() {
        return "POST /" + fixture.index() + "/_search {\"_source\": [\"name\"],\"query\": {\"term\": {\"id\": ?}}}";
    }

    @After
    public void closeFactoryFixture() throws Exception {
        try {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        } finally {
            fixture.close();
        }
    }
}
