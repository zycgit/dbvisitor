package net.hasor.dbvisitor.test.realdb.clickhouse.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.JsonTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseJsonTypeJdbcContractTest extends JsonTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
