package net.hasor.dbvisitor.test.realdb.mysql.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMultipleResultSetContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlJdbcMultipleResultSetContractTest extends JdbcMultipleResultSetContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
