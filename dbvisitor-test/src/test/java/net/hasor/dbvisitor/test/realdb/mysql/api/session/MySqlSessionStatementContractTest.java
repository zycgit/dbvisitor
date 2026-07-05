package net.hasor.dbvisitor.test.realdb.mysql.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionStatementContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlSessionStatementContractTest extends SessionStatementContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
