/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.db2.api.jdbc;

import java.sql.SQLException;
import java.util.List;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Db2WindowPaginationTest extends AbstractNxnContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Test
    public void windowPagination_shouldPreserveExplicitOrderAcrossPages() throws SQLException {
        for (int id = 25; id >= 1; id--) {
            jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name) VALUES (?, ?)", new Object[] { id, "Page" + id });
        }

        String ascendingSql = """
                SELECT id, name FROM (
                    SELECT id, name, ROW_NUMBER() OVER(ORDER BY id) AS rn
                    FROM user_info
                ) AS paged
                WHERE rn BETWEEN ? AND ?
                ORDER BY rn
                """;
        List<UserInfo> firstPage = jdbcTemplate.queryForList(ascendingSql, new Object[] { 1, 20 }, UserInfo.class);
        List<UserInfo> secondPage = jdbcTemplate.queryForList(ascendingSql, new Object[] { 21, 40 }, UserInfo.class);
        assertEquals(20, firstPage.size());
        assertEquals(5, secondPage.size());
        for (int i = 0; i < firstPage.size(); i++) {
            assertEquals(Integer.valueOf(i + 1), firstPage.get(i).getId());
            assertEquals("Page" + (i + 1), firstPage.get(i).getName());
        }
        for (int i = 0; i < secondPage.size(); i++) {
            assertEquals(Integer.valueOf(i + 21), secondPage.get(i).getId());
        }

        String descendingSql = """
                SELECT id, name FROM (
                    SELECT id, name, ROW_NUMBER() OVER(ORDER BY id DESC) AS rn
                    FROM user_info
                ) AS paged
                WHERE rn BETWEEN ? AND ?
                ORDER BY rn
                """;
        List<UserInfo> descendingPage = jdbcTemplate.queryForList(descendingSql, new Object[] { 1, 20 }, UserInfo.class);
        assertEquals(20, descendingPage.size());
        for (int i = 0; i < descendingPage.size(); i++) {
            assertEquals(Integer.valueOf(25 - i), descendingPage.get(i).getId());
        }
    }
}
