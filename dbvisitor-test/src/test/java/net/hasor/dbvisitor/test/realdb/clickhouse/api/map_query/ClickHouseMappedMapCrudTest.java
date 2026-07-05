package net.hasor.dbvisitor.test.realdb.clickhouse.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.MappedMapCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;

public class ClickHouseMappedMapCrudTest extends MappedMapCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
