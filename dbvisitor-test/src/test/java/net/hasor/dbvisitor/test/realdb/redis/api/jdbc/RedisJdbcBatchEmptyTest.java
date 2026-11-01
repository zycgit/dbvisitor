/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchEmptyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class RedisJdbcBatchEmptyTest extends JdbcBatchEmptyCase {

    private final RedisJdbcFixture fixture = new RedisJdbcFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @AfterEach
    public void closeRedisFixture() throws SQLException {
        this.fixture.close();
    }
}
