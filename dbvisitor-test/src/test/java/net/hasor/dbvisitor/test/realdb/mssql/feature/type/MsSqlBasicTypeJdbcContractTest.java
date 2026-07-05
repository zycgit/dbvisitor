package net.hasor.dbvisitor.test.realdb.mssql.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.BasicTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlBasicTypeJdbcContractTest extends BasicTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
