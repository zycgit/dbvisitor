/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;
import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;

public abstract class AdapterCase extends AbstractNxnContractTest {
    @Override
    @BeforeEach
    public void setup() {
        String currentEnv = OneApiDataSourceManager.getDbDialect();
        Assumptions.assumeTrue(profile().env().equals(currentEnv), "NXN adapter contract for env '" + profile().env() + "' skipped because current env is '" + currentEnv + "'");
    }

    protected Connection newAdapterConnection() throws SQLException {
        return OneApiDataSourceManager.getConnection(profile().env());
    }
}
