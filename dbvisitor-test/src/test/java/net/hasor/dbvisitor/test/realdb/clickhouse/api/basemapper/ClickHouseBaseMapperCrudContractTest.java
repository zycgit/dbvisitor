package net.hasor.dbvisitor.test.realdb.clickhouse.api.basemapper;

import net.hasor.dbvisitor.test.contract.api.basemapper.AbstractBaseMapperCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseBaseMapperCrudContractTest extends AbstractBaseMapperCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
