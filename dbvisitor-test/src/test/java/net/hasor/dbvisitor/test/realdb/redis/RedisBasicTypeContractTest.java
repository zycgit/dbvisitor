/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** Native single-value reads cover the same values as the common numeric/boolean/character cases. */
public class RedisBasicTypeContractTest extends AdapterContractTest {
    private final String prefix = "nxn:basic:" + UUID.randomUUID() + ":";
    private final List<String> keys = new ArrayList<>();
    private Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Before
    public void openFixture() throws SQLException {
        this.connection = newAdapterConnection();
        this.jdbcTemplate = new JdbcTemplate(this.connection);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_NUMERIC)
    public void numericValues_shouldRoundTripWithoutPrecisionLoss() throws SQLException {
        assertEquals(Byte.valueOf(Byte.MAX_VALUE), roundTrip("byte", Byte.MAX_VALUE, Byte.class));
        assertEquals(Short.valueOf(Short.MAX_VALUE), roundTrip("short", Short.MAX_VALUE, Short.class));
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), roundTrip("int", Integer.MAX_VALUE, Integer.class));
        assertEquals(Long.valueOf(Long.MAX_VALUE), roundTrip("long", Long.MAX_VALUE, Long.class));
        assertEquals(3.14f, roundTrip("float", 3.14f, Float.class), 0.001f);
        assertEquals(2.718281828d, roundTrip("double", 2.718281828d, Double.class), 0.000001d);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_DECIMAL)
    public void decimalValue_shouldRoundTripWithoutPrecisionLoss() throws SQLException {
        BigDecimal value = roundTrip("decimal", new BigDecimal("12345.67"), BigDecimal.class);
        assertEquals("Decimal readback: " + value, 0, new BigDecimal("12345.67").compareTo(value));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_BIG_INTEGER)
    public void bigIntegerValue_shouldRoundTripWithoutPrecisionLoss() throws SQLException {
        assertEquals(new BigInteger("9223372036854775807"), roundTrip("bigint", new BigInteger("9223372036854775807"), BigInteger.class));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_EMPTY_STRING)
    public void emptyString_shouldRoundTripWithoutBecomingNull() throws SQLException {
        assertEquals("", roundTrip("empty", "", String.class));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_BOOLEAN)
    public void booleanValues_shouldRoundTripTrueAndFalse() throws SQLException {
        assertEquals(Boolean.TRUE, roundTrip("true", true, Boolean.class));
        assertEquals(Boolean.FALSE, roundTrip("false", false, Boolean.class));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_CHARACTER)
    public void characterValues_shouldRoundTripAsciiAndUnicode() throws SQLException {
        assertEquals(Character.valueOf('A'), roundTrip("char", 'A', Character.class));
        assertEquals("Hello World!", roundTrip("text", "Hello World!", String.class));
        assertEquals("你好世界！🌍", roundTrip("unicode", "你好世界！🌍", String.class));
    }

    private <T> T roundTrip(String label, Object value, Class<T> type) throws SQLException {
        String key = this.prefix + label;
        this.keys.add(key);
        assertEquals(1, this.jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, value }));
        return this.jdbcTemplate.queryForObject("GET ?", new Object[] { key }, type);
    }

    @After
    public void closeFixture() throws SQLException {
        try {
            for (String key : this.keys) {
                this.jdbcTemplate.executeUpdate("DEL ?", new Object[] { key });
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }
}
