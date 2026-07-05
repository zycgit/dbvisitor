package net.hasor.dbvisitor.test.realdb.mysql.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.FreedomMapIdentifierSecurityContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlFreedomMapIdentifierSecurityContractTest extends FreedomMapIdentifierSecurityContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
