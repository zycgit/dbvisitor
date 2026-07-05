package net.hasor.dbvisitor.test.realdb.mysql.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlJdbcBatchTest extends JdbcBatchContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
