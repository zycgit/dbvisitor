package net.hasor.dbvisitor.test.realdb.clickhouse.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseJdbcQueryTest extends JdbcQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
