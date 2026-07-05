package net.hasor.dbvisitor.test.realdb.db2.feature.transaction;

import net.hasor.dbvisitor.test.contract.feature.transaction.TransactionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2TransactionContractTest extends TransactionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
