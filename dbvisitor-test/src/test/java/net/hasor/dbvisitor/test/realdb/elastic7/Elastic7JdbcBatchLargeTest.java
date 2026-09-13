/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchLargeCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7JdbcBatchLargeTest extends JdbcBatchLargeCase {
    protected final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }

    @Override
    protected String insertCommand() {
        return "POST /" + fixture.index() + "/_doc?refresh=true {\"id\": ?,\"name\": ?}";
    }

    @Override
    protected String namedInsertCommand() {
        return "POST /" + fixture.index() + "/_doc?refresh=true {\"id\": :id,\"name\": :val}";
    }

    @Override
    protected String updateCommand() {
        return "POST /" + fixture.index() + "/_update_by_query?refresh=true {\"script\": {\"source\": \"ctx._source.name=params.name\",\"params\": {\"name\": ?}},\"query\": {\"term\": {\"id\": ?}}}";
    }

    @Override
    protected String deleteCommand() {
        return "POST /" + fixture.index() + "/_delete_by_query?refresh=true {\"query\": {\"term\": {\"id\": ?}}}";
    }

    @Override
    protected String valueCommand() {
        return "POST /" + fixture.index() + "/_search {\"_source\": [\"name\"],\"query\": {\"term\": {\"id\": ?}}}";
    }

    @Override
    protected String countCommand() {
        return "POST /" + fixture.index() + "/_count {\"query\": {\"term\": {\"id\": ?}}}";
    }

    @Override
    protected String countRangeCommand(boolean inclusiveEnd) {
        return "POST /" + fixture.index() + "/_count {\"query\": {\"range\": {\"id\": {\"gte\": ?,\"" + (inclusiveEnd ? "lte" : "lt") + "\": ?}}}}";
    }

    @Override
    protected String literalInsertCommand(int id, String value) {
        return "POST /" + fixture.index() + "/_doc?refresh=true {\"id\": " + id + ",\"name\": \"" + value + "\"}";
    }

    @Override
    protected String literalUpdateCommand(int id, String value) {
        return "POST /" + fixture.index() + "/_update_by_query?refresh=true {\"script\": {\"source\": \"ctx._source.name=params.name\",\"params\": {\"name\": \"" + value + "\"}},\"query\": {\"term\": {\"id\": " + id + "}}}";
    }

    @Override
    protected String invalidCommand() {
        return "POST /" + fixture.index() + "/_doc?refresh=true {\"id\": \"invalid-number\"}";
    }
}
