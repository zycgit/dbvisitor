package net.hasor.dbvisitor.test.realdb.oracle.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.EnumTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleEnumTypeJdbcContractTest extends EnumTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
