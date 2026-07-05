package net.hasor.dbvisitor.test.realdb.pg.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlJdbcQueryTest extends JdbcQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
