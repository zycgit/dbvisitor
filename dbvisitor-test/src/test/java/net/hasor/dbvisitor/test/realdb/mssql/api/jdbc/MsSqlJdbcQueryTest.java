package net.hasor.dbvisitor.test.realdb.mssql.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlJdbcQueryTest extends JdbcQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
