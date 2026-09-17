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
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaRangeCase;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.junit.NxnConcurrent;
import net.hasor.dbvisitor.test.realdb.clickhouse.ClickHouseUserInfoFixture;
import org.junit.Before;
import org.junit.ClassRule;

@NxnConcurrent
public class ClickHouseLambdaRangeTest extends LambdaRangeCase {
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
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
