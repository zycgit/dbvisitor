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
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.session.SessionCoreCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.UserOrder;
import net.hasor.dbvisitor.test.contract.material.model.UserRole;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.test.nxn.env.*;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7SessionCoreTest extends SessionCoreCase {
    private final ElasticMatrixFixture users = new ElasticMatrixFixture();
    private final ElasticMatrixFixture orders = new ElasticMatrixFixture();
    private final ElasticMatrixFixture roles = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() { return Elastic7Profile.INSTANCE; }

    @Override
    @Before
    public void setup() throws IOException, SQLException {
        this.jdbcTemplate = users.open(profile().env());
        orders.open(profile().env());
        roles.open(profile().env());
        this.dataSource = Elastic7SessionDataSource.open(profile().env());
    }

    @Override
    protected Configuration newConfiguration(Options options) {
        Configuration configuration = new Configuration(options);
        configuration.getMappingRegistry().loadEntityAsTable(UserInfo.class, users.index());
        configuration.getMappingRegistry().loadEntityAsTable(UserOrder.class, orders.index());
        return configuration;
    }

    @Override
    protected Configuration compositeKeyConfiguration() {
        Configuration configuration = newConfiguration();
        configuration.getMappingRegistry().loadEntityAsTable(UserRole.class, roles.index());
        return configuration;
    }

    @Override
    protected String sessionInsertCommand() {
        return "POST /" + users.index() + "/_doc {\"id\": ?,\"name\": ?,\"age\": ?}";
    }

    @Override
    protected String sessionCountCommand() {
        return "POST /" + users.index() + "/_count {\"query\": {\"term\": {\"id\": ?}}}";
    }

    @After
    public void closeCoreFixtures() throws Exception {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
        try {
            roles.close();
        } finally {
            try {
                orders.close();
            } finally {
                users.close();
            }
        }
    }
}
