package net.hasor.dbvisitor.test.realdb.clickhouse.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.FreedomMapCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseFreedomMapCrudTest extends FreedomMapCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
