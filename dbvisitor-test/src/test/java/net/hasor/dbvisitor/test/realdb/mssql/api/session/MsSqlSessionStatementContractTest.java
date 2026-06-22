package net.hasor.dbvisitor.test.realdb.mssql.api.session;

import net.hasor.dbvisitor.test.contract.api.session.AbstractSessionStatementContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlSessionStatementContractTest extends AbstractSessionStatementContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
