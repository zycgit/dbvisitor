package net.hasor.dbvisitor.test.realdb.h2.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.JsonTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2JsonTypeJdbcContractTest extends JsonTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
