package net.hasor.dbvisitor.test.realdb.pg.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcResultHandlingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlJdbcResultHandlingContractTest extends JdbcResultHandlingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
