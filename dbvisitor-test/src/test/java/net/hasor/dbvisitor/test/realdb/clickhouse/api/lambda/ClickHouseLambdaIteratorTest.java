/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.api.lambda;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaIteratorCase;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.junit.NxnConcurrent;
import net.hasor.dbvisitor.test.realdb.clickhouse.ClickHouseUserInfoFixture;
import org.junit.Before;
import org.junit.ClassRule;

@NxnConcurrent
public class ClickHouseLambdaIteratorTest extends LambdaIteratorCase {
    @ClassRule
    public static final ClickHouseUserInfoFixture FIXTURE = new ClickHouseUserInfoFixture();

    @Override
    @Before
    public void setup() throws IOException, SQLException {
        this.dataSource = FIXTURE.dataSource();
        super.setup();
    }

    @Override
    protected void cleanTestData() {
        FIXTURE.clearRows();
    }

    @Override
    protected void seedUsers(int startId, String prefix, int count, int age) throws SQLException {
        // This prepares iterator input; the inherited pagination assertions and row counts stay unchanged.
        Object[][] rows = new Object[count][];
        for (int i = 0; i < count; i++) {
            String name = prefix + (i + 1);
            rows[i] = new Object[] { startId + i, name, age, name.toLowerCase() + "@nxn.test", new Date() };
        }
        jdbcTemplate.executeBatch("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", rows);
    }

    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
