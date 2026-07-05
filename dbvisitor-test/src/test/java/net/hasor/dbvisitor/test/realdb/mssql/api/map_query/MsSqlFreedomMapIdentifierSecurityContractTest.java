package net.hasor.dbvisitor.test.realdb.mssql.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.FreedomMapIdentifierSecurityContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlFreedomMapIdentifierSecurityContractTest extends FreedomMapIdentifierSecurityContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
