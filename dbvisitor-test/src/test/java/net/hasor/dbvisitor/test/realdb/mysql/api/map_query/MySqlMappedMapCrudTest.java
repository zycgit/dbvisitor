package net.hasor.dbvisitor.test.realdb.mysql.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.MappedMapCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlMappedMapCrudTest extends MappedMapCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
