package net.hasor.dbvisitor.test.realdb.oracle.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleJdbcBatchTest extends JdbcBatchContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
