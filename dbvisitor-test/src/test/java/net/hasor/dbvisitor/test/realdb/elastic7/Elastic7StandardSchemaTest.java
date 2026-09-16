/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.metadata.StandardSchemaCase;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic7StandardSchemaTest extends StandardSchemaCase {
    private final Map<String, List<String>> physicalSchema = new LinkedHashMap<>();
    private final List<String>              created        = new ArrayList<>();
    private       Connection                fixtureConnection;

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
        fixtureConnection = OneApiDataSourceManager.getConnection(profile().env());
        jdbcTemplate = new JdbcTemplate(fixtureConnection);
        String prefix = "nxn_schema_" + UUID.randomUUID().toString().replace("-", "") + "_";
        for (Map.Entry<String, List<String>> table : super.standardSchema().entrySet()) {
            String index = prefix + table.getKey();
            Map<String, Object> properties = new LinkedHashMap<>();
            for (String column : table.getValue()) {
                properties.put(column, Map.of("type", fieldType(column)));
            }
            Map<String, Object> mapping = Map.of("properties", properties);
            Object mappings = "es6".equals(profile().env()) ? Map.of("_doc", mapping) : mapping;
            try {
                String body = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("mappings", mappings));
                jdbcTemplate.execute("PUT /" + index + " " + body);
            } catch (java.io.IOException error) {
                throw new SQLException(error);
            }
            created.add(index);
            physicalSchema.put(index, table.getValue());
        }
    }

    private String fieldType(String column) {
        if ("id".equals(column) || column.endsWith("_id") || "age".equals(column) || column.contains("int_") || column.contains("ordinal")) {
            return "integer";
        }
        if (column.contains("float") || column.contains("double") || "embedding".equals(column) || "amount".equals(column)) {
            return "double";
        }
        if (column.contains("bool")) {
            return "boolean";
        }
        if ("create_time".equals(column)) {
            return "date";
        }
        return "keyword";
    }

    @Override
    protected Map<String, List<String>> standardSchema() {
        return physicalSchema;
    }

    @Override
    protected Connection schemaConnection() throws SQLException {
        return OneApiDataSourceManager.getConnection(profile().env());
    }

    @After
    public void closeFixture() throws SQLException {
        if (fixtureConnection == null) {
            return;
        }
        SQLException failure = null;
        for (String index : created) {
            try {
                jdbcTemplate.execute("DELETE /" + index);
            } catch (SQLException error) {
                if (failure == null) {
                    failure = error;
                } else {
                    failure.addSuppressed(error);
                }
            }
        }
        try {
            fixtureConnection.close();
        } catch (SQLException error) {
            if (failure == null) {
                failure = error;
            } else {
                failure.addSuppressed(error);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
