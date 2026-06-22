package net.hasor.dbvisitor.test.realdb.clickhouse.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.AbstractKeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseKeyGenerationContractTest extends AbstractKeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
