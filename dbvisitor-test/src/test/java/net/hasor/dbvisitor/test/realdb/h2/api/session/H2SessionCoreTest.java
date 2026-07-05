package net.hasor.dbvisitor.test.realdb.h2.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionCoreContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2SessionCoreTest extends SessionCoreContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
