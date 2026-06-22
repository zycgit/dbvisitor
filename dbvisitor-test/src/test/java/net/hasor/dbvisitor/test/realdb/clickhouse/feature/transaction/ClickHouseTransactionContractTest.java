package net.hasor.dbvisitor.test.realdb.clickhouse.feature.transaction;

import net.hasor.dbvisitor.test.contract.feature.transaction.AbstractTransactionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseTransactionContractTest extends AbstractTransactionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
