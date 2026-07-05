package net.hasor.dbvisitor.test.realdb.db2.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionStatementContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2SessionStatementContractTest extends SessionStatementContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
