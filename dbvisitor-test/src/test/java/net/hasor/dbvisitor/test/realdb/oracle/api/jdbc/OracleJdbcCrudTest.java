package net.hasor.dbvisitor.test.realdb.oracle.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleJdbcCrudTest extends JdbcCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
