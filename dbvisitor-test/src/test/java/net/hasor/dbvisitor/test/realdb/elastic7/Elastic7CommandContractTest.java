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
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import static org.junit.Assert.*;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class Elastic7CommandContractTest extends AdapterContractTest {
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
                    // ignore
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
                        assertTrue("doc not match (name): " + doc, doc.contains("\"name\":\"mali\"") || doc.contains("\"name\": \"mali\""));
                        assertTrue("doc not match (age): " + doc, doc.contains("\"age\":26") || doc.contains("\"age\": 26"));
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
