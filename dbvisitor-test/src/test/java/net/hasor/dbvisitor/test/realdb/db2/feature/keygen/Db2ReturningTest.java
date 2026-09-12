/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.db2.feature.keygen;

import net.hasor.dbvisitor.test.scenario.ReturningScenario;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2ReturningTest extends ReturningScenario {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Override
    protected String insertSql() {
        return "SELECT id, create_time FROM FINAL TABLE (INSERT INTO doc_returning (name, age) VALUES (?, ?)) AS changed";
    }

    @Override
    protected String updateSql() {
        return "SELECT id, age FROM FINAL TABLE (UPDATE doc_returning SET age = ? WHERE id = ?) AS changed";
    }

    @Override
    protected String deleteSql() {
        return "SELECT id, name FROM OLD TABLE (DELETE FROM doc_returning WHERE id = ?) AS changed";
    }
}
