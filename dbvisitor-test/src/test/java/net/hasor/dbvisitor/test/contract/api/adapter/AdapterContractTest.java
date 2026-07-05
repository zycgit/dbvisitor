package net.hasor.dbvisitor.test.contract.api.adapter;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assume;
import org.junit.Before;

import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class AdapterContractTest extends AbstractNxnContractTest {
    @Override
    @Before
    public void setup() {
        String currentEnv = OneApiDataSourceManager.getDbDialect();
        Assume.assumeTrue("NXN adapter contract for env '" + profile().env() + "' skipped because current env is '" + currentEnv + "'", profile().env().equals(currentEnv));
    }

    protected Connection newAdapterConnection() throws SQLException {
        return OneApiDataSourceManager.getConnection(profile().env());
    }
}
