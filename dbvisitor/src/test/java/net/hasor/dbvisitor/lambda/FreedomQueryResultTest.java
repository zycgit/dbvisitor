/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapping.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FreedomQueryResultTest {
    private Connection     connection;
    private JdbcTemplate   jdbc;
    private LambdaTemplate lambda;

    @Before
    public void setUp() throws Exception {
        this.connection = DriverManager.getConnection("jdbc:h2:mem:freedom_" + UUID.randomUUID());
        this.jdbc = new JdbcTemplate(this.connection);
        this.lambda = new LambdaTemplate(this.connection);
        this.jdbc.execute("CREATE TABLE items (id INT, item_name VARCHAR(40), remark VARCHAR(40))");
        this.jdbc.executeUpdate("INSERT INTO items VALUES (?, ?, ?)", new Object[] { 7, "kept", null });
    }

    @After
    public void tearDown() throws Exception {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    @Test
    public void queryForMapShouldReturnAllColumnsWithoutEntityMapping() throws Exception {
        Map<String, Object> row = this.lambda.queryFreedom("items").eq("id", 7).queryForMap();
        assertEquals(3, row.size());
        assertEquals(7, row.get("ID"));
        assertEquals("kept", row.get("ITEM_NAME"));
        assertTrue(row.containsKey("REMARK"));
        assertNull(row.get("REMARK"));
    }

    @Test
    public void queryForMapShouldAgreeWithOtherMapResultMethods() throws Exception {
        Map<String, Object> row = this.lambda.queryFreedom("items").queryForMap();
        assertFalse(row.isEmpty());
        assertEquals(row, this.lambda.queryFreedom("items").queryForObject());
        assertEquals(List.of(row), this.lambda.queryFreedom("items").queryForMapList());
        assertEquals(List.of(row), this.lambda.queryFreedom("items").queryForList());
    }

    @Test
    public void queryForMapShouldPreserveAliasesAndProjectionOrder() throws Exception {
        Map<String, Object> row = this.lambda.queryFreedom("items").applySelect("item_name AS \"displayName\", id AS \"itemId\"").queryForMap();
        assertEquals(List.of("displayName", "itemId"), List.copyOf(row.keySet()));
        assertEquals("kept", row.get("displayName"));
        assertEquals(7, row.get("itemId"));
    }

    @Test
    public void queryForMapShouldPreserveComputedColumns() throws Exception {
        Map<String, Object> row = this.lambda.queryFreedom("items").applySelect("id + 1 AS next_id").queryForMap();
        assertEquals(Map.of("NEXT_ID", 8), row);
    }

    @Test
    public void queryForMapShouldHonorCaseInsensitiveOption() throws Exception {
        LambdaTemplate insensitive = new LambdaTemplate(this.connection, Options.of().caseInsensitive(true));
        Map<String, Object> row = insensitive.queryFreedom("items").select("item_name").queryForMap();
        assertEquals("kept", row.get("ITEM_NAME"));
        assertEquals("kept", row.get("item_name"));

        LambdaTemplate sensitive = new LambdaTemplate(this.connection, Options.of().caseInsensitive(false));
        Map<String, Object> exactRow = sensitive.queryFreedom("items").select("item_name").queryForMap();
        assertEquals("kept", exactRow.get("ITEM_NAME"));
        assertFalse(exactRow.containsKey("item_name"));
    }

    @Test
    public void camelCaseQueryShouldKeepTheSameResultLabelsAsMapList() throws Exception {
        LambdaTemplate camel = new LambdaTemplate(this.connection, Options.of().mapUnderscoreToCamelCase(true));
        Map<String, Object> row = camel.queryFreedom("items").select("itemName").eq("id", 7).queryForMap();
        assertEquals("kept", row.get("ITEM_NAME"));
        assertEquals(List.of(row), camel.queryFreedom("items").select("itemName").eq("id", 7).queryForMapList());
    }
}
