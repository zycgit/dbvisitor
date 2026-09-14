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
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcPreparedValueCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;

public class Elastic7JdbcPreparedValueTest extends JdbcPreparedValueCase {
    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    protected void createFixture() throws SQLException {
        String fields = "{\"properties\": {\"id\": {\"type\": \"long\"},\"name\": {\"type\": \"keyword\"}}}";
        String mappings = "es6".equals(profile().env()) ? "{\"_doc\": " + fields + "}" : fields;
        new JdbcTemplate(connection).execute("PUT /" + table + " {\"mappings\": " + mappings + "}");
    }

    @Override
    protected String insertSql() {
        return "POST /" + table + "/_doc {\"id\": ?,\"name\": ?}";
    }

    @Override
    protected String selectByNameSql() {
        return "POST /" + table + "/_search {\"_source\": [\"id\",\"name\"],\"query\": {\"term\": {\"name\": ?}}}";
    }

    @Override
    protected String selectNullSql() {
        return "POST /" + table + "/_search {\"_source\": [\"id\",\"name\"],\"query\": {\"bool\": {\"must_not\": {\"exists\": {\"field\": \"name\"}}}}}";
    }

    @Override
    protected String selectAllSql() {
        return "POST /" + table + "/_search {\"_source\": [\"id\"]}";
    }

    @Override
    protected String updateSql() {
        return "POST /" + table + "/_update_by_query {\"query\": {\"term\": {\"name\": ?}},"
                + "\"script\": {\"source\": \"ctx._source.name = params.value\",\"params\": {\"value\": ?}}}";
    }

    @Override
    protected void bindUpdateParameters(PreparedStatement statement, String newValue, String filterValue) throws SQLException {
        statement.setString(1, filterValue);
        statement.setString(2, newValue);
    }

    @Override
    protected String deleteSql() {
        return "POST /" + table + "/_delete_by_query {\"query\": {\"term\": {\"name\": ?}}}";
    }

    @Override
    protected String dropSql() {
        return "DELETE /" + table;
    }
}
