/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.pg.feature.keygen;

import net.hasor.dbvisitor.test.scenario.ReturningScenario;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlReturningTest extends ReturningScenario {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO doc_returning (name, age) VALUES (?, ?) RETURNING id, create_time";
    }

    @Override
    protected String updateSql() {
        return "UPDATE doc_returning SET age = ? WHERE id = ? RETURNING id, age";
    }

    @Override
    protected String deleteSql() {
        return "DELETE FROM doc_returning WHERE id = ? RETURNING id, name";
    }
}
