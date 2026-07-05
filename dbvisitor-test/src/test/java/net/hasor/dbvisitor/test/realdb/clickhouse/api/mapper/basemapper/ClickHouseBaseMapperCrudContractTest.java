package net.hasor.dbvisitor.test.realdb.clickhouse.api.mapper.basemapper;

import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseBaseMapperCrudContractTest extends BaseMapperCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
