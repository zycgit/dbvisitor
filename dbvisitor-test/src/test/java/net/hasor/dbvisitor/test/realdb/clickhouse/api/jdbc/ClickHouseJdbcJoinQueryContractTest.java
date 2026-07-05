package net.hasor.dbvisitor.test.realdb.clickhouse.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcJoinQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseJdbcJoinQueryContractTest extends JdbcJoinQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
