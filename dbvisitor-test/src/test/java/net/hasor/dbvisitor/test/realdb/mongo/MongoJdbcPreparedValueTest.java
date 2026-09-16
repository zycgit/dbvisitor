/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import net.hasor.dbvisitor.test.contract.feature.parameter.JdbcPreparedValueCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;

public class MongoJdbcPreparedValueTest extends JdbcPreparedValueCase {
    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    protected void createFixture() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("use test");
            statement.execute("db.createCollection('" + table + "')");
        }
    }

    @Override
    protected String insertSql() {
        return "db." + table + ".insert({id: ?, name: ?})";
    }

    @Override
    protected String selectByNameSql() {
        return "db." + table + ".find({name: ?}, {_id: 0, id: 1, name: 1})";
    }

    @Override
    protected String selectNullSql() {
        return "db." + table + ".find({name: null}, {_id: 0, id: 1, name: 1})";
    }

    @Override
    protected String selectAllSql() {
        return "db." + table + ".find({}, {_id: 0, id: 1})";
    }

    @Override
    protected String updateSql() {
        return "db." + table + ".update({name: ?}, {$set: {name: ?}})";
    }

    @Override
    protected void bindUpdateParameters(PreparedStatement statement, String newValue, String filterValue) throws SQLException {
        statement.setString(1, filterValue);
        statement.setString(2, newValue);
    }

    @Override
    protected String deleteSql() {
        return "db." + table + ".remove({name: ?})";
    }

    @Override
    protected String dropSql() {
        return "db." + table + ".drop()";
    }
}
