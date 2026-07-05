package net.hasor.dbvisitor.test.realdb.pg.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlJdbcParameterContractTest extends JdbcParameterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
