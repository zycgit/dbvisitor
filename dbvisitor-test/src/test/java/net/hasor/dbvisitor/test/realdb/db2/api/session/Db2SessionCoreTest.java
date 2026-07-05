package net.hasor.dbvisitor.test.realdb.db2.api.session;

import net.hasor.dbvisitor.test.contract.api.session.SessionCoreContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2SessionCoreTest extends SessionCoreContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
