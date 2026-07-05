package net.hasor.dbvisitor.test.realdb.mssql.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.JsonTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlJsonTypeJdbcContractTest extends JsonTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
