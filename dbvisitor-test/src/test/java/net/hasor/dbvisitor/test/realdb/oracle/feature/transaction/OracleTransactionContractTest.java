package net.hasor.dbvisitor.test.realdb.oracle.feature.transaction;

import net.hasor.dbvisitor.test.contract.feature.transaction.TransactionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleTransactionContractTest extends TransactionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
