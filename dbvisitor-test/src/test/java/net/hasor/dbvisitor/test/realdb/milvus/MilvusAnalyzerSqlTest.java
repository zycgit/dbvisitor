/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusAnalyzerSqlTest extends AdapterCase {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_ANALYZER)
    public void analyze_shouldBindTextsAndPreserveTokensDetailsAndEmptyResults() throws SQLException {
        try (Connection connection = newAdapterConnection(); PreparedStatement statement = connection.prepareStatement(
                "ANALYZE ? WITH (analyzer_params=?, with_detail=?, with_hash=?)")) {
            statement.setObject(1, Arrays.asList("hello world", "", "milvus"));
            statement.setObject(2, Collections.singletonMap("tokenizer", "standard"));
            statement.setBoolean(3, true);
            statement.setBoolean(4, true);
            try (ResultSet result = statement.executeQuery()) {
                JsonArray first = tokens(result, 1);
                assertEquals(2, first.size());
                assertEquals("hello", first.get(0).getAsJsonObject().get("token").getAsString());
                JsonObject secondToken = first.get(1).getAsJsonObject();
                assertEquals("world", secondToken.get("token").getAsString());
                assertEquals(6L, secondToken.get("startOffset").getAsLong());
                assertEquals(11L, secondToken.get("endOffset").getAsLong());
                long hash = secondToken.get("hash").getAsLong();
                assertTrue(hash > 0 && hash <= 4_294_967_295L);
                assertEquals(0, tokens(result, 2).size());
                JsonArray third = tokens(result, 3);
                assertEquals(1, third.size());
                assertEquals("milvus", third.get(0).getAsJsonObject().get("token").getAsString());
                assertFalse(result.next());
            }
            statement.setString(1, "reuse");
            statement.setString(2, "{\"tokenizer\":\"standard\"}");
            try (ResultSet result = statement.executeQuery()) {
                assertEquals("reuse", tokens(result, 1).get(0).getAsJsonObject().get("token").getAsString());
                assertFalse(result.next());
            }
        }
    }

    private JsonArray tokens(ResultSet result, long index) throws SQLException {
        assertTrue(result.next());
        assertEquals(index, result.getLong("TEXT_INDEX"));
        return JsonParser.parseString(result.getString("TOKENS")).getAsJsonArray();
    }
}
