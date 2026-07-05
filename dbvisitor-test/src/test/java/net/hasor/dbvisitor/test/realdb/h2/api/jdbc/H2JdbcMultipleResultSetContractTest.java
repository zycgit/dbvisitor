package net.hasor.dbvisitor.test.realdb.h2.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMultipleResultSetContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2JdbcMultipleResultSetContractTest extends JdbcMultipleResultSetContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
