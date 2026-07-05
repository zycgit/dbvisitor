package net.hasor.dbvisitor.test.realdb.clickhouse.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.BasicTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseBasicTypeJdbcContractTest extends BasicTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
