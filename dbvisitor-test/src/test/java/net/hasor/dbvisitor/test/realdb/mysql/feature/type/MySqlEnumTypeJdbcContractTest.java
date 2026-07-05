package net.hasor.dbvisitor.test.realdb.mysql.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.EnumTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlEnumTypeJdbcContractTest extends EnumTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
