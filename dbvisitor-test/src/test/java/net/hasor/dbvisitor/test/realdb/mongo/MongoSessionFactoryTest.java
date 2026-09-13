/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.session.SessionFactoryCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoMapperFixture;
import org.junit.After;
import org.junit.Before;


public class MongoSessionFactoryTest extends SessionFactoryCase {
    private final MongoMapperFixture fixture = new MongoMapperFixture();
    private HikariDataSource sessionSource;

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        Properties properties = OneApiDataSourceManager.loadAdapterProperties("mongo");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("jdbc.url"));
        config.setDriverClassName(properties.getProperty("jdbc.driver"));
        config.setUsername(properties.getProperty("jdbc.username"));
        config.setPassword(properties.getProperty("jdbc.password"));
        config.setConnectionInitSql("use test");
        config.setConnectionTestQuery(this.fixture.source() + ".count({})");
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        this.sessionSource = new HikariDataSource(config);
    }

    @Override
    protected DataSource sessionDataSource() {
        return this.sessionSource;
    }

    @Override
    protected Configuration newConfiguration(Options options) {
        Configuration configuration = super.newConfiguration(options);
        configuration.getMappingRegistry().loadEntityAsTable(UserInfo.class, this.fixture.table());
        return configuration;
    }

    @Override
    protected String jdbcInsertCommand() {
        return this.fixture.source() + ".insert({id: ?, name: ?, age: ?})";
    }

    @Override
    protected String jdbcCountCommand() {
        return this.fixture.source() + ".count({id: ?})";
    }

    @Override
    protected String jdbcNameCommand() {
        return this.fixture.source() + ".find({id: ?}, {_id: 0, name: 1})";
    }

    @After
    public void closeFixture() throws Exception {
        try {
            if (this.sessionSource != null) {
                this.sessionSource.close();
            }
        } finally {
            this.fixture.close();
        }
    }
}
