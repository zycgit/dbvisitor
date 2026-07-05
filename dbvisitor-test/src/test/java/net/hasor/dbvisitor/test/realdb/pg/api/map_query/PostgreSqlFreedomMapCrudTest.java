package net.hasor.dbvisitor.test.realdb.pg.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.FreedomMapCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlFreedomMapCrudTest extends FreedomMapCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
