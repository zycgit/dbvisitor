package net.hasor.dbvisitor.test.realdb.oracle.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.JsonTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleJsonTypeJdbcContractTest extends JsonTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
