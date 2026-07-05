package net.hasor.dbvisitor.test.realdb.pg.feature.transaction;

import net.hasor.dbvisitor.test.contract.feature.transaction.TransactionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlTransactionContractTest extends TransactionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
