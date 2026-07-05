package net.hasor.dbvisitor.test.realdb.mssql.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlJdbcCrudTest extends JdbcCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
