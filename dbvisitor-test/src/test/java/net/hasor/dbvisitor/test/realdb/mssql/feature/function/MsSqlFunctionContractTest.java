package net.hasor.dbvisitor.test.realdb.mssql.feature.function;

import net.hasor.dbvisitor.test.contract.feature.function.AbstractFunctionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlFunctionContractTest extends AbstractFunctionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
