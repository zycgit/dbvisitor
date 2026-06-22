package net.hasor.dbvisitor.test.realdb.oracle.api.session;

import net.hasor.dbvisitor.test.contract.api.session.AbstractSessionCoreContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleSessionCoreContractTest extends AbstractSessionCoreContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
