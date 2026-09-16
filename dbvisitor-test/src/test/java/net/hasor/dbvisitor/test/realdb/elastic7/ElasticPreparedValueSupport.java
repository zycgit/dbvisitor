/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.feature.parameter.JdbcPreparedValueCase;

/** Shared command material; ES6 and ES7 bind the contract through their own profiles. */
public abstract class ElasticPreparedValueSupport extends JdbcPreparedValueCase {
    @Override
    protected void createFixture() throws SQLException {
        String fields = "{\"properties\": {\"id\": {\"type\": \"long\"},\"name\": {\"type\": \"keyword\"}}}";
        String mappings = "es6".equals(profile().env()) ? "{\"_doc\": " + fields + "}" : fields;
        new JdbcTemplate(this.connection).execute("PUT /" + this.table + " {\"mappings\": " + mappings + "}");
    }

    @Override
    protected String insertSql() {
        return "POST /" + this.table + "/_doc {\"id\": ?,\"name\": ?}";
    }

    @Override
    protected String selectByNameSql() {
        return "POST /" + this.table + "/_search {\"_source\": [\"id\",\"name\"],\"query\": {\"term\": {\"name\": ?}}}";
    }

    @Override
    protected String selectNullSql() {
        return "POST /" + this.table + "/_search {\"_source\": [\"id\",\"name\"],\"query\": {\"bool\": {\"must_not\": {\"exists\": {\"field\": \"name\"}}}}}";
    }

    @Override
    protected String selectAllSql() {
        return "POST /" + this.table + "/_search {\"_source\": [\"id\"]}";
    }

    @Override
    protected String updateSql() {
        return "POST /" + this.table + "/_update_by_query {\"query\": {\"term\": {\"name\": ?}}," + "\"script\": {\"source\": \"ctx._source.name = params.value\",\"params\": {\"value\": ?}}}";
    }

    @Override
    protected void bindUpdateParameters(PreparedStatement statement, String newValue, String filterValue) throws SQLException {
        statement.setString(1, filterValue);
        statement.setString(2, newValue);
    }

    @Override
    protected String deleteSql() {
        return "POST /" + this.table + "/_delete_by_query {\"query\": {\"term\": {\"name\": ?}}}";
    }

    @Override
    protected String dropSql() {
        return "DELETE /" + this.table;
    }
}
