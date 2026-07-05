package net.hasor.dbvisitor.test.realdb.h2.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.FreedomMapIdentifierSecurityContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2FreedomMapIdentifierSecurityContractTest extends FreedomMapIdentifierSecurityContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
