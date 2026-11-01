/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeBasicTypeFixture;
import net.hasor.dbvisitor.test.contract.feature.type.JsonMapJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class MongoJsonMapJdbcTest extends JsonMapJdbcCase {
    private final NativeBasicTypeFixture fixture = new NativeBasicTypeFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env(), "json_types_explicit_test");
    }

    @Override
    protected String insertCommand(String columns, String... parameters) {
        return this.fixture.insertCommand("json_types_explicit_test", columns, parameters);
    }

    @Override
    protected String selectCommand(String columns) throws SQLException {
        return this.fixture.selectCommand("json_types_explicit_test", columns);
    }

    @AfterEach
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
