/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;
import java.sql.*;
import java.util.*;
import com.google.gson.JsonParser;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.RunAnalyzerReq;
import io.milvus.v2.service.vector.response.RunAnalyzerResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusAnalyzerTest {
    private final List<RunAnalyzerReq> requests = new ArrayList<>();

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("runAnalyzer")) {
                requests.add((RunAnalyzerReq) args[0]);
                return RunAnalyzerResp.builder().results(Arrays.asList(RunAnalyzerResp.AnalyzerResult.builder().tokens(Collections.singletonList(RunAnalyzerResp.AnalyzerToken.builder().token("hello").startOffset(0L).endOffset(5L).position(0L).positionLength(1L).hash(4_294_967_295L).build())).build(), RunAnalyzerResp.AnalyzerResult.builder().build())).build();
            }
            return null;
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect() throws SQLException {
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", props);
    }

    @Test
    public void analyzerReturnsEveryInputResultIncludingEmptyTokens() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("ANALYZE ['hello', ''] WITH (with_detail=true, with_hash=true)")) {
            assertEquals(Types.BIGINT, result.getMetaData().getColumnType(1));
            assertTrue(result.next());
            assertEquals(1L, result.getLong("TEXT_INDEX"));
            assertEquals(4_294_967_295L, JsonParser.parseString(result.getString("TOKENS")).getAsJsonArray().get(0).getAsJsonObject().get("hash").getAsLong());
            assertTrue(result.next());
            assertEquals(2L, result.getLong("TEXT_INDEX"));
            assertEquals("[]", result.getString("TOKENS"));
            assertFalse(result.next());
        }
        assertEquals(Arrays.asList("hello", ""), requests.get(0).getTexts());
        assertTrue(requests.get(0).getWithDetail());
        assertTrue(requests.get(0).getWithHash());
    }

    @Test
    public void analyzerOptionsAndFieldContextUseNativeRequestFields() throws Exception {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("ANALYZE ? ON TABLE books(body) WITH (analyzer_params=?, analyzer_names=?)")) {
            statement.setObject(1, Arrays.asList("hello", "world"));
            statement.setObject(2, Collections.singletonMap("tokenizer", "standard"));
            statement.setObject(3, Arrays.asList("en", "zh"));
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertFalse(result.next());
            }
        }
        RunAnalyzerReq request = requests.get(0);
        assertEquals("db1", request.getDatabaseName());
        assertEquals("books", request.getCollectionName());
        assertEquals("body", request.getFieldName());
        assertEquals("standard", request.getAnalyzerParams().get("tokenizer"));
        assertEquals(Arrays.asList("en", "zh"), request.getAnalyzerNames());
        assertFalse(request.getWithDetail());
        assertFalse(request.getWithHash());
    }

    @Test
    public void boundTextIsNotReparsedAsSql() throws Exception {
        String text = "x'; DROP TABLE books; -- 中文";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("ANALYZE ?")) {
            statement.setString(1, text);
            statement.executeQuery().close();
            statement.setObject(1, text);
            statement.executeQuery().close();
            statement.setObject(1, text, Types.VARCHAR);
            statement.executeQuery().close();
            statement.setObject(1, new String[] { text, "" });
            statement.executeQuery().close();
        }
        assertEquals(4, requests.size());
        for (RunAnalyzerReq request : requests.subList(0, 3)) {
            assertEquals(Collections.singletonList(text), request.getTexts());
            assertEquals("", request.getCollectionName());
            assertEquals("", request.getFieldName());
        }
        assertEquals(Arrays.asList(text, ""), requests.get(3).getTexts());
    }

    @Test
    public void invalidTextsAndOptionsFailBeforeSdk() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String sql : Arrays.asList("ANALYZE []", "ANALYZE null", "ANALYZE [1]", "ANALYZE ['x', null]", "ANALYZE 'x' WITH (with_detail=1)", "ANALYZE 'x' WITH (with_hash=null)", "ANALYZE 'x' WITH (analyzer_params='[]')", "ANALYZE 'x' WITH (analyzer_names=1)", "ANALYZE 'x' WITH (unknown=true)")) {
                assertThrows(SQLException.class, () -> statement.executeQuery(sql));
            }
        }
        assertTrue(requests.isEmpty());
    }
}
