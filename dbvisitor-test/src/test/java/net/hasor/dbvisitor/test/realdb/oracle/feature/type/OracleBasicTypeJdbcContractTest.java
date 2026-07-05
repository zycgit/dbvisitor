package net.hasor.dbvisitor.test.realdb.oracle.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.BasicTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleBasicTypeJdbcContractTest extends BasicTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
