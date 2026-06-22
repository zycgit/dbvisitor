package net.hasor.dbvisitor.test.realdb.clickhouse;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ClickHouseJdbcDslContractTest extends AbstractNxnContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_CLICKHOUSE_JDBC_DSL_JOIN_USE_NULLS)
    public void clickHouseJdbcDsl_shouldUseJoinUseNullsSettingForLeftJoin() throws SQLException {
        jdbcTemplate.execute("DROP TABLE IF EXISTS ch_nxn_user_order");
        jdbcTemplate.execute("DROP TABLE IF EXISTS ch_nxn_user_info");
        jdbcTemplate.execute("CREATE TABLE ch_nxn_user_info (id Int32, name Nullable(String)) ENGINE = MergeTree ORDER BY id");
        jdbcTemplate.execute("CREATE TABLE ch_nxn_user_order (id Int32, user_id Nullable(Int32)) ENGINE = MergeTree ORDER BY id");
        jdbcTemplate.execute("INSERT INTO ch_nxn_user_info (id, name) VALUES (790001, 'CH-Join-User')");

        Map<String, Object> row = jdbcTemplate.queryForMap(//
                "SELECT u.id AS user_id, o.id AS order_id " + //
                        "FROM ch_nxn_user_info u LEFT JOIN ch_nxn_user_order o ON u.id = o.user_id " + //
                        "WHERE u.id = 790001 SETTINGS join_use_nulls = 1");

        assertEquals(790001, ((Number) value(row, "user_id")).intValue());
        assertNull(value(row, "order_id"));
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
