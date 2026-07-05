package net.hasor.dbvisitor.test.realdb.mysql.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlJdbcCrudTest extends JdbcCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
