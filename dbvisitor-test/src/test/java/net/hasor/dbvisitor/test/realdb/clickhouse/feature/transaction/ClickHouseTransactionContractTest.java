package net.hasor.dbvisitor.test.realdb.clickhouse.feature.transaction;

import net.hasor.dbvisitor.test.contract.feature.transaction.TransactionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseTransactionContractTest extends TransactionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
