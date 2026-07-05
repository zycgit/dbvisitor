package net.hasor.dbvisitor.test.realdb.clickhouse.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.TimeTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseTimeTypeJdbcContractTest extends TimeTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
