package net.hasor.dbvisitor.test.realdb.oracle.api.session;

import net.hasor.dbvisitor.test.contract.api.session.AbstractSessionMapperContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleSessionMapperContractTest extends AbstractSessionMapperContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
