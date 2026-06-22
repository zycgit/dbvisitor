package net.hasor.dbvisitor.test.realdb.db2.api.session;

import net.hasor.dbvisitor.test.contract.api.session.AbstractSessionCoreContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2SessionCoreContractTest extends AbstractSessionCoreContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
