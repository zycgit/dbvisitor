package net.hasor.dbvisitor.test.realdb.mysql.feature.transaction;

import net.hasor.dbvisitor.test.contract.feature.transaction.AbstractTransactionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlTransactionContractTest extends AbstractTransactionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
