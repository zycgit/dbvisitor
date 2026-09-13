/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.feature.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClickHouseDistributedTableContractTest extends AbstractNxnContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Test
    public void distributedTableRoutesReadsAndWritesButDoesNotOfferMutations() throws SQLException {
        String local = "ch_nxn_distributed_local";
        String distributed = "ch_nxn_distributed_table";
        dropTableIfExists(distributed);
        dropTableIfExists(local);
        Properties settings = new Properties();
        settings.setProperty("custom_settings", "distributed_foreground_insert=1");
        try (Connection connection = OneApiDataSourceManager.getConnection("clickhouse", settings)) {
            jdbcTemplate.execute("CREATE TABLE " + local + " (id Int32, name String) ENGINE=MergeTree ORDER BY id");
            jdbcTemplate.execute("CREATE TABLE " + distributed + " AS " + local + " ENGINE=Distributed('default', currentDatabase(), '" + local + "', id)");
            LambdaTemplate lambda = new LambdaTemplate(connection);
            lambda.insertFreedom(distributed).applyMap(Map.of("id", 1, "name", "distributed")).executeSumResult();
            assertEquals("distributed", lambda.queryFreedom(distributed).eq("id", 1).queryForObject().get("name"));
            assertEquals(1, lambda.queryFreedom(local).queryForCount());
            SQLException update = assertThrows(SQLException.class, () -> lambda.updateFreedom(distributed).eq("id", 1).updateTo("name", "changed").doUpdate());
            assertTrue(update.getMessage(), update.getMessage().contains("support mutations"));
            SQLException delete = assertThrows(SQLException.class, () -> lambda.deleteFreedom(distributed).eq("id", 1).doDelete());
            assertTrue(delete.getMessage(), delete.getMessage().contains("not supported") || delete.getMessage().contains("support mutations"));
            assertEquals("distributed", lambda.queryFreedom(local).eq("id", 1).queryForObject().get("name"));
        } finally {
            dropTableIfExists(distributed);
            dropTableIfExists(local);
        }
    }
}
