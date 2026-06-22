package net.hasor.dbvisitor.test.realdb.mssql.api.session;

import net.hasor.dbvisitor.test.contract.api.session.AbstractSessionCoreContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlSessionCoreContractTest extends AbstractSessionCoreContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
