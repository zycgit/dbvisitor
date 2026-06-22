package net.hasor.dbvisitor.test.realdb.h2.feature.transaction;

import net.hasor.dbvisitor.test.contract.feature.transaction.AbstractTransactionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2TransactionContractTest extends AbstractTransactionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
