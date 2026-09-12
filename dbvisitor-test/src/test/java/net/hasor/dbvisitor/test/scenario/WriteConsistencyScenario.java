/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.scenario;

import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class WriteConsistencyScenario extends AbstractNxnContractTest {
    private String table;
    @Test
    public void duplicateWrite_withoutTransactionKeepsFirstRow() throws Exception {
        prepareTable("doc_write_auto");
        try {
            insertPair();
            fail("Duplicate primary key must fail");
        } catch (java.sql.SQLException expected) {
            assertEquals(Integer.valueOf(1), jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class));
            assertEquals("Alice", jdbcTemplate.queryForObject("SELECT name FROM " + table, String.class));
        } finally {
            dropTableIfExists(table);
        }
    }

    @Test
    public void duplicateWrite_transactionRollsBackFirstRow() throws Throwable {
        prepareTable("doc_write_tx");
        TransactionTemplate tx = new TransactionTemplateManager(TransactionHelper.txManager(dataSource));
        try {
            try {
                tx.execute(status -> {
                    insertPair();
                    return null;
                });
                fail("Duplicate primary key must fail");
            } catch (java.sql.SQLException expected) {
                assertEquals(Integer.valueOf(0), jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class));
            }
        } finally {
            dropTableIfExists(table);
        }
    }

    private void prepareTable(String table) throws Exception {
        this.table = table;
        dropTableIfExists(table);
        jdbcTemplate.executeUpdate("CREATE TABLE " + table + " (id INTEGER NOT NULL PRIMARY KEY, name VARCHAR(100))"
                + (profile().env().equals("mysql") ? " ENGINE=InnoDB" : ""));
    }

    private void insertPair() throws java.sql.SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO " + table + " (id, name) VALUES (?, ?)", new Object[] { 1001, "Alice" });
        jdbcTemplate.executeUpdate("INSERT INTO " + table + " (id, name) VALUES (?, ?)", new Object[] { 1001, "Bob" });
    }
}
