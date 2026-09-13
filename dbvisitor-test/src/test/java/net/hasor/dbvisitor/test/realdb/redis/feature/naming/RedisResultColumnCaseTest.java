/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.naming;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcColumnMappingCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.jdbc.RedisJdbcFixture;

/** Result-column matching is independent of the command language used to obtain the rows. */
public class RedisResultColumnCaseTest extends JdbcColumnMappingCase {
    private final RedisJdbcFixture fixture = new RedisJdbcFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected void seedColumnValue() throws SQLException {
        jdbcTemplate.queryForLong("ZADD ? 21 ?", new Object[] { fixture.key("columns"), "NXN-Column" });
    }

    @Override
    protected String columnQuery() {
        return "ZRANGE '" + fixture.key("columns") + "' 0 0 WITHSCORES";
    }

    @Override
    protected String resultColumn() {
        return "ELEMENT";
    }

    @Override
    protected Class<? extends ColumnValue> strictType() {
        return StrictMember.class;
    }

    @Override
    protected Class<? extends ColumnValue> insensitiveType() {
        return InsensitiveMember.class;
    }

    @Table(caseInsensitive = false)
    public static class StrictMember implements ColumnValue {
        @Column("element")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Table(caseInsensitive = true)
    public static class InsensitiveMember extends StrictMember {
    }
}
