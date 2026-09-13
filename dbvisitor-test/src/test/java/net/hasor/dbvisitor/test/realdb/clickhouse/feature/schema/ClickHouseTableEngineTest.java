/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.feature.schema;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import net.hasor.dbvisitor.dialect.provider.ClickHouseDialect;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ClickHouseTableEngineTest extends AbstractNxnContractTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> engines() {
        // @formatter:off
        return Arrays.asList(new Object[][] {
            { "MergeTree", "MergeTree ORDER BY id", true },
            { "ReplacingMergeTree", "ReplacingMergeTree(version) ORDER BY id", true },
            { "SummingMergeTree", "SummingMergeTree ORDER BY id", true },
            { "AggregatingMergeTree", "AggregatingMergeTree ORDER BY id", true },
            { "CollapsingMergeTree", "CollapsingMergeTree(sign) ORDER BY id", true },
            { "VersionedCollapsingMergeTree", "VersionedCollapsingMergeTree(sign, version) ORDER BY id", true },
            { "CoalescingMergeTree", "CoalescingMergeTree ORDER BY id", true },
            { "Memory", "Memory", true },
            { "Log", "Log", false },
            { "TinyLog", "TinyLog", false },
            { "StripeLog", "StripeLog", false }
        });
        // @formatter:on
    }

    private final String table;
    private final String engine;
    private final boolean mutations;

    public ClickHouseTableEngineTest(String name, String engine, boolean mutations) {
        this.table = "ch_nxn_engine_" + name.toLowerCase(java.util.Locale.ROOT);
        this.engine = engine;
        this.mutations = mutations;
    }

    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Test
    public void sameBuilderApiHonorsTableEngineCapabilities() throws SQLException {
        dropTableIfExists(this.table);
        try {
            if ("Memory".equals(this.engine)) {
                lambdaTemplate = new LambdaTemplate(dataSource, Options.of().dialect(new ClickHouseDialect(ClickHouseDialect.DeleteMode.MUTATION)));
            }
            jdbcTemplate.execute("CREATE TABLE " + this.table + " (id Int32, name String, sign Int8, version UInt64) ENGINE = " + this.engine);
            lambdaTemplate.insertFreedom(this.table).applyMap(Map.of("id", 1, "name", "before", "sign", 1, "version", 1L)).executeSumResult();
            assertEquals("before", lambdaTemplate.queryFreedom(this.table).eq("id", 1).queryForObject().get("name"));

            if (this.mutations) {
                lambdaTemplate.updateFreedom(this.table).eq("id", 1).updateTo("name", "after").doUpdate();
                assertEquals("after", lambdaTemplate.queryFreedom(this.table).eq("id", 1).queryForObject().get("name"));
                lambdaTemplate.deleteFreedom(this.table).eq("id", 1).doDelete();
                assertEquals(0, lambdaTemplate.queryFreedom(this.table).queryForCount());
            } else {
                SQLException update = assertThrows(SQLException.class, () -> lambdaTemplate.updateFreedom(this.table).eq("id", 1).updateTo("name", "after").doUpdate());
                assertTrue(update.getMessage(), update.getMessage().contains("support mutations"));
                SQLException delete = assertThrows(SQLException.class, () -> lambdaTemplate.deleteFreedom(this.table).eq("id", 1).doDelete());
                assertTrue(delete.getMessage(), delete.getMessage().contains("not supported") || delete.getMessage().contains("support mutations"));
                assertEquals("before", lambdaTemplate.queryFreedom(this.table).eq("id", 1).queryForObject().get("name"));
            }
        } finally {
            dropTableIfExists(this.table);
        }
    }
}
