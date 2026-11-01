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
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaDistinctCase;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.junit.NxnConcurrent;
import net.hasor.dbvisitor.test.realdb.clickhouse.ClickHouseUserInfoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

@NxnConcurrent
public class ClickHouseLambdaDistinctTest extends LambdaDistinctCase {
    @RegisterExtension
    public static final ClickHouseUserInfoFixture FIXTURE = new ClickHouseUserInfoFixture();

    @Override
    @BeforeEach
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
