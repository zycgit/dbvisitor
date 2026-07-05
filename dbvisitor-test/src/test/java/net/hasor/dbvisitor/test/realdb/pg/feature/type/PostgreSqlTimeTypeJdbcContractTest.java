package net.hasor.dbvisitor.test.realdb.pg.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.TimeTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlTimeTypeJdbcContractTest extends TimeTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
