package net.hasor.dbvisitor.test.realdb.mssql.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionMapperContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlSessionMapperContractTest extends SessionMapperContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
