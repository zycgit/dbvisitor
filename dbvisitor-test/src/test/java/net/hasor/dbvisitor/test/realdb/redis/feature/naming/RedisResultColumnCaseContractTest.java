/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.naming;

import java.sql.SQLException;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.test.contract.feature.naming.ResultColumnCaseContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.jdbc.RedisJdbcFixture;
import static org.junit.Assert.*;

/** Result-column matching is independent of the command language used to obtain the rows. */
public class RedisResultColumnCaseContractTest extends ResultColumnCaseContractTest {
    private final RedisJdbcFixture fixture = new RedisJdbcFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.fixture.seedScores();
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    @Test
    @Capability(CapabilityId.NAMING_CASE_SENSITIVE_FIELD_MISMATCH)
    public void caseSensitiveMapping_shouldLeaveMismatchedUppercaseColumnsNull() throws SQLException {
        Object[] args = { this.fixture.key("scores") };
        StrictMember strict = this.jdbcTemplate.queryForObject("ZRANGE ? 0 0 WITHSCORES", args, StrictMember.class);
        InsensitiveMember insensitive = this.jdbcTemplate.queryForObject("ZRANGE ? 0 0 WITHSCORES", args, InsensitiveMember.class);
        assertNotNull(strict);
        assertNull(strict.getElement());
        assertEquals("member-1", insensitive.getElement());
    }

    @Override
    @Test
    @Capability(CapabilityId.NAMING_CASE_INSENSITIVE_FREEDOM_MAP)
    public void caseInsensitiveFreedomMap_shouldAllowCaseInsensitiveKeyLookup() throws SQLException {
        this.jdbcTemplate.setResultsCaseInsensitive(true);
        Map<String, Object> row = this.jdbcTemplate.queryForMap("ZRANGE ? 0 0 WITHSCORES",
                new Object[] { this.fixture.key("scores") });
        assertEquals("member-1", row.get("ELEMENT"));
        assertEquals("member-1", row.get("element"));
        assertEquals("member-1", row.get("Element"));
    }

    @Override
    @Test
    @Capability(CapabilityId.NAMING_CASE_SENSITIVE_FREEDOM_MAP)
    public void caseSensitiveFreedomMap_shouldRequireExactResultColumnCase() throws SQLException {
        this.jdbcTemplate.setResultsCaseInsensitive(false);
        Map<String, Object> row = this.jdbcTemplate.queryForMap("ZRANGE ? 0 0 WITHSCORES",
                new Object[] { this.fixture.key("scores") });
        assertEquals("member-1", row.get("ELEMENT"));
        assertNull(row.get("element"));
        assertNull(row.get("Element"));
    }

    @Table(caseInsensitive = false)
    public static class StrictMember {
        @Column("element")
        private String element;

        public String getElement() {
            return this.element;
        }

        public void setElement(String element) {
            this.element = element;
        }
    }

    @Table(caseInsensitive = true)
    public static class InsensitiveMember extends StrictMember {
    }
}
