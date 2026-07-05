package net.hasor.dbvisitor.test.realdb.mysql.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionCoreContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlSessionCoreTest extends SessionCoreContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
