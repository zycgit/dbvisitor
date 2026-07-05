package net.hasor.dbvisitor.test.nxn.report.metadata;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Assume;
import org.junit.Before;

import net.hasor.dbvisitor.test.nxn.report.NxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

public class RedisNxnMetadataContractTest extends NxnMetadataContractTest {
    @Override
    @Before
    public void setup() throws IOException, SQLException {
        String currentEnv = OneApiDataSourceManager.getDbDialect();
        Assume.assumeTrue("NXN contract for env '" + profile().env() + "' skipped because current env is '" + currentEnv + "'", profile().env().equals(currentEnv));
    }

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }
}
