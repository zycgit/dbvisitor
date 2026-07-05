package net.hasor.dbvisitor.test.realdb.pg.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.FreedomMapIdentifierSecurityContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlFreedomMapIdentifierSecurityContractTest extends FreedomMapIdentifierSecurityContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
