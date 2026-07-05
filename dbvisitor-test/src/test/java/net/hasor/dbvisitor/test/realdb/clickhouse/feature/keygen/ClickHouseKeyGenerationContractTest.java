package net.hasor.dbvisitor.test.realdb.clickhouse.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.KeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseKeyGenerationContractTest extends KeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
