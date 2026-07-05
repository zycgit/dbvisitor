package net.hasor.dbvisitor.test.realdb.pg.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.ArrayTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlArrayTypeJdbcContractTest extends ArrayTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
