package net.hasor.dbvisitor.test.realdb.db2.api.jdbc;

import net.hasor.dbvisitor.test.contract.api.jdbc.AbstractJdbcJoinQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2JdbcJoinQueryContractTest extends AbstractJdbcJoinQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
