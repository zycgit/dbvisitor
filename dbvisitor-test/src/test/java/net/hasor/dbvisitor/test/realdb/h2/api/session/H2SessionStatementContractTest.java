package net.hasor.dbvisitor.test.realdb.h2.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionStatementContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2SessionStatementContractTest extends SessionStatementContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
