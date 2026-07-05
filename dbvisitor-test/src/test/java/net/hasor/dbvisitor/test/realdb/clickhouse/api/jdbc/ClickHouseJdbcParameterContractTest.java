package net.hasor.dbvisitor.test.realdb.clickhouse.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseJdbcParameterContractTest extends JdbcParameterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
