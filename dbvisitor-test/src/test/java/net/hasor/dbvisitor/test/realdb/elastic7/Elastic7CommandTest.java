/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Elastic7CommandTest extends AdapterCase {
    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_COMMAND_RESULT_SET)
    public void elasticCommand_shouldExposeDocumentColumnsInResultSet() throws Exception {
        try (Connection conn = newAdapterConnection()) {
            try (Statement s = conn.createStatement()) {
                // 1. clean
                try {
                    s.execute("DELETE /test_user_info");
                } catch (Exception e) {
                    Elastic7Cleanup.requireMissingIndex(e);
                }

                // 2. insert
                s.execute("POST /test_user_info/_doc/1 { \"name\": \"mali\", \"age\": 26 }");

                // 3. query
                try (ResultSet rs = s.executeQuery("POST /test_user_info/_search { \"query\": { \"match\": { \"name\": \"mali\" } } }")) {
                    if (rs.next()) {
                        String id = rs.getString("_ID");
                        String doc = rs.getString("_DOC");
                        String name = rs.getString("name");
                        int age = rs.getInt("age");

                        assertEquals("id not match", "1", id);
                        JsonNode document = new ObjectMapper().readTree(doc);
                        assertEquals("doc not match (name)", "mali", document.path("name").asText());
                        assertEquals("doc not match (age)", 26, document.path("age").asInt());
                        assertEquals("name not match", "mali", name);
                        assertEquals("age not match", 26, age);
                    } else {
                        fail("no data found");
                    }
                }
            }
        }
    }
}
