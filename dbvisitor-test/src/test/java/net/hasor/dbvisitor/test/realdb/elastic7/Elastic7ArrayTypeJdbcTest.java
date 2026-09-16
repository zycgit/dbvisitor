/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import java.util.Arrays;
import net.hasor.dbvisitor.test.contract.feature.type.ArrayTypeJdbcCase;
import net.hasor.dbvisitor.test.contract.material.model.types.ArrayTypesAnnotationModel;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticTypeMappings;
import org.junit.After;
import org.junit.Before;

public class Elastic7ArrayTypeJdbcTest extends ArrayTypeJdbcCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        ElasticTypeMappings.addArrays(jdbcTemplate, profile().env(), fixture.index());
        fixture.registry().loadEntityAsTable(ArrayTypesAnnotationModel.class, fixture.index());
        lambdaTemplate = fixture.lambdaTemplate();
    }

    @Override
    protected String insertCommand(String table, String columns) throws SQLException {
        String[] values = new String[columns.split(",").length];
        Arrays.fill(values, "?");
        return insertCommand(table, columns, values);
    }

    @Override
    protected String insertCommand(String table, String columns, String... values) {
        String[] names = columns.split(",");
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            if (i != 0) {
                body.append(',');
            }
            body.append('"').append(names[i].trim()).append("\": ").append(values[i]);
        }
        return "POST /" + fixture.index() + "/_doc {" + body + "}";
    }

    @Override
    protected String selectCommand(String table, String column) throws SQLException {
        return queryColumn(column, "?");
    }

    @Override
    protected String selectNamedCommand(String table, String column) throws SQLException {
        return queryColumn(column, ":id");
    }

    private String queryColumn(String column, String id) throws SQLException {
        jdbcTemplate.execute("POST /" + fixture.index() + "/_refresh");
        return "POST /" + fixture.index() + "/_search {\"_source\": [\"" + column + "\"],\"query\": {\"term\": {\"id\": " + id + "}}}";
    }

    @Override
    protected String updateArrayCommand() {
        return "POST /" + fixture.index() + "/_update_by_query " + """
                {"query": {"term": {"id": :id}},"script": {"source": "ctx._source.int_array=params.values","params": {"values": :array}}}
                """;
    }

    @Override
    protected String countRangeCommand() {
        return "POST /" + fixture.index() + "/_count " + """
                {"query": {"range": {"id": {"gte": ?,"lte": ?}}}}
                """;
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
