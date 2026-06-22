package net.hasor.dbvisitor.test.realdb.mssql.feature.procedure;

import net.hasor.dbvisitor.test.contract.feature.procedure.AbstractProcedureContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlProcedureContractTest extends AbstractProcedureContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
