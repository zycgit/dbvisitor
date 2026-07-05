package net.hasor.dbvisitor.test.realdb.pg.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.BasicTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlBasicTypeJdbcContractTest extends BasicTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
