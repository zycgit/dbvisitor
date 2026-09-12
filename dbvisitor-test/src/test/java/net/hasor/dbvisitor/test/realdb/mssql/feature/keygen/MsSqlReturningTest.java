/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mssql.feature.keygen;

import net.hasor.dbvisitor.test.scenario.ReturningScenario;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlReturningTest extends ReturningScenario {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO doc_returning (name, age) OUTPUT INSERTED.id, INSERTED.create_time VALUES (?, ?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE doc_returning SET age = ? OUTPUT INSERTED.id, INSERTED.age WHERE id = ?";
    }

    @Override
    protected String deleteSql() {
        return "DELETE FROM doc_returning OUTPUT DELETED.id, DELETED.name WHERE id = ?";
    }
}
