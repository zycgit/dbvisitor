package net.hasor.dbvisitor.test.realdb.pg.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.JsonTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlJsonTypeJdbcContractTest extends JsonTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
