package net.hasor.dbvisitor.test.realdb.pg.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionCoreContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlSessionCoreTest extends SessionCoreContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
