package net.hasor.dbvisitor.test.realdb.clickhouse.feature.keygen;

import net.hasor.dbvisitor.test.contract.feature.keygen.InsertDialectStrategyContractTest;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;

public class ClickHouseInsertDialectStrategyContractTest extends InsertDialectStrategyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Override
    protected boolean expectedIgnoreSupport() {
        return false;
    }

    @Override
    protected boolean expectedUpdateSupport() {
        return false;
    }

    @Override
    protected boolean expectedUpdateWithKeyOnlyColumnsSupport() {
        return false;
    }
}
