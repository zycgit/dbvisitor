package net.hasor.dbvisitor.test.realdb.db2.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.AbstractJsonTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2JsonTypeJdbcContractTest extends AbstractJsonTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
