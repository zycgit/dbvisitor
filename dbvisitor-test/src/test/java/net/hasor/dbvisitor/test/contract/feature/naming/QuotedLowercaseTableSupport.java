/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.naming;

import java.sql.SQLException;
import org.junit.After;

/** A quoted lowercase table beside the standard uppercase USER_INFO fixture. */
public abstract class QuotedLowercaseTableSupport extends IdentifierQuotingCase {
    private boolean quotedTableCreated;

    @Override
    protected void prepareAutoKeywordColumnFixture() throws SQLException {
        dropTableIfExists("naming_keyword_test");
        this.jdbcTemplate.executeUpdate("CREATE TABLE naming_keyword_test ("
                + primaryKeyColumn("id", "INT") + ", "
                + qualified("order") + " VARCHAR(100), "
                + qualified("select") + " VARCHAR(100), name VARCHAR(100))");
    }

    @Override
    protected void prepareAutoKeywordTableFixture() throws SQLException {
        String table = qualified("order");
        dropTableIfExists(table);
        this.jdbcTemplate.executeUpdate("CREATE TABLE " + table + " ("
                + primaryKeyColumn("id", "INT") + ", name VARCHAR(100), description VARCHAR(200))");
    }

    @Override
    protected void prepareDelimitedFixture() throws SQLException {
        String table = qualified("user_info");
        dropTableIfExists(table);
        this.jdbcTemplate.executeUpdate("CREATE TABLE " + table + " ("
                + primaryKeyColumn(qualified("id"), "INT") + ", "
                + qualified("name") + " VARCHAR(100), "
                + qualified("age") + " INT, "
                + qualified("email") + " VARCHAR(100), "
                + qualified("create_time") + " " + profile().datetimeColumnType() + ")");
        this.quotedTableCreated = true;
    }

    @After
    public void closeQuotedTable() throws SQLException {
        if (this.quotedTableCreated) {
            this.jdbcTemplate.executeUpdate("DROP TABLE " + qualified("user_info"));
        }
    }
}
