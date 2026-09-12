/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mysql.feature.type;

import java.sql.Connection;
import java.sql.DriverManager;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MySqlTinyIntMappingTest {
    @Test
    public void defaultMappingReadsBooleanButExplicitIntegerPreservesValue() throws Exception {
        verifyMapping(false, Boolean.TRUE);
    }

    @Test
    public void disabledBooleanMappingReadsNumericStatus() throws Exception {
        verifyMapping(true, Integer.valueOf(2));
    }

    private void verifyMapping(boolean disableBooleanMapping, Object expected) throws Exception {
        String url = OneApiDataSourceManager.getProperty("jdbc.url");
        url = url.replaceAll("([?&])tinyInt1isBit=[^&]*", "$1").replace("?&", "?").replace("&&", "&");
        if (disableBooleanMapping) {
            url += (url.contains("?") ? "&" : "?") + "tinyInt1isBit=false";
        }
        try (Connection connection = DriverManager.getConnection(url,
                OneApiDataSourceManager.getProperty("jdbc.username"), OneApiDataSourceManager.getProperty("jdbc.password"))) {
            JdbcTemplate jdbc = new JdbcTemplate(connection);
            jdbc.executeUpdate("CREATE TEMPORARY TABLE doc_tinyint_status (status TINYINT(1))");
            jdbc.executeUpdate("INSERT INTO doc_tinyint_status (status) VALUES (2)");
            String sql = "SELECT status FROM doc_tinyint_status";
            assertEquals(expected, jdbc.queryForObject(sql, (rs, rowNum) -> rs.getObject(1)));
            assertEquals(Integer.valueOf(2), jdbc.queryForObject(sql, Integer.class));
            assertEquals(Byte.valueOf((byte) 2), jdbc.queryForObject(sql, Byte.class));
        }
    }
}
