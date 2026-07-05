package net.hasor.dbvisitor.test.realdb.mssql.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionStatementContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlSessionStatementContractTest extends SessionStatementContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
