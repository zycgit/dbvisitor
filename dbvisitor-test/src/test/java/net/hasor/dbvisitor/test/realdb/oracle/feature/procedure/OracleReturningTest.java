/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.oracle.feature.procedure;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;

public class OracleReturningTest extends AbstractNxnContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }

    @Test
    public void insertReturning() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call("""
            BEGIN
                INSERT INTO user_info (id, name, age)
                VALUES (#{id}, UPPER(#{name}), #{age})
                RETURNING name, age INTO #{savedName,mode=out,jdbcType=varchar},
                                         #{savedAge,mode=out,jdbcType=integer};
            END;
            """, Map.of("id", 918101, "name", "alice", "age", 20));
        assertEquals("ALICE", result.get("savedName"));
        assertEquals(20, result.get("savedAge"));
        assertEquals("ALICE", jdbcTemplate.queryForObject("SELECT name FROM user_info WHERE id = 918101", String.class));
    }

    @Test
    public void updateReturning() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age) VALUES (918101, 'ALICE', 20)");
        Map<String, Object> result = jdbcTemplate.call("""
            BEGIN
                UPDATE user_info SET age = age + 1 WHERE id = #{id}
                RETURNING age INTO #{savedAge,mode=out,jdbcType=integer};
                #{rows,mode=out,jdbcType=integer} := SQL%ROWCOUNT;
            END;
            """, Map.of("id", 918101));
        assertEquals(1, result.get("rows"));
        assertEquals(21, result.get("savedAge"));
        assertEquals(Integer.valueOf(21), jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = 918101", Integer.class));
    }

    @Test
    public void deleteReturning() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age) VALUES (918101, 'ALICE', 20)");
        Map<String, Object> result = jdbcTemplate.call("""
            BEGIN
                DELETE FROM user_info WHERE id = #{id}
                RETURNING name INTO #{deletedName,mode=out,jdbcType=varchar};
                #{rows,mode=out,jdbcType=integer} := SQL%ROWCOUNT;
            END;
            """, Map.of("id", 918101));
        assertEquals(1, result.get("rows"));
        assertEquals("ALICE", result.get("deletedName"));
        assertEquals(Integer.valueOf(0), jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = 918101", Integer.class));
    }

    @Test
    public void updateReturningNoMatch() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call("""
            BEGIN
                UPDATE user_info SET age = age + 1 WHERE id = #{id}
                RETURNING age INTO #{savedAge,mode=out,jdbcType=integer};
                #{rows,mode=out,jdbcType=integer} := SQL%ROWCOUNT;
            END;
            """, Map.of("id", 918101));
        assertEquals(0, result.get("rows"));
    }
}
