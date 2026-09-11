/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;

import static org.junit.Assert.assertTrue;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class MongoCommandContractTest extends AdapterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_COMMAND_QUERY)
    public void mongoCommand_shouldReturnJsonRowFromStatement() throws Exception {
        try (Connection conn = newAdapterConnection()) {
            try (Statement s = conn.createStatement()) {
                try {
                    s.execute("test.user_info.drop()");
                } catch (Exception ignored) {
                }

                s.execute("test.user_info.insert({name: 'mali', age: 26})");

                try (ResultSet rs = s.executeQuery("test.user_info.find({name: 'mali'})")) {
                    assertTrue("Mongo command query should return one row", rs.next());
                    String json = rs.getString("_JSON");
                    assertTrue(json.contains("\"name\": \"mali\""));
                    assertTrue(json.contains("\"age\": 26"));
                }
            }
        }
    }
}
