package net.hasor.dbvisitor.test.realdb.pg.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlJdbcCrudTest extends JdbcCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
