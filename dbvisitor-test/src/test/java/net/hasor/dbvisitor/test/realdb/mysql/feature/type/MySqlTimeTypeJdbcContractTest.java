package net.hasor.dbvisitor.test.realdb.mysql.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.TimeTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlTimeTypeJdbcContractTest extends TimeTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
