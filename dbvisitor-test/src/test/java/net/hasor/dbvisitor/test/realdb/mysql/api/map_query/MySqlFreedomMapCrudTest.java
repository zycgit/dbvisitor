package net.hasor.dbvisitor.test.realdb.mysql.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.FreedomMapCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlFreedomMapCrudTest extends FreedomMapCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
