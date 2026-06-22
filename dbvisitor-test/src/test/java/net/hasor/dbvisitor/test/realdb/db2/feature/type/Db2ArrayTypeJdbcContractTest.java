package net.hasor.dbvisitor.test.realdb.db2.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.AbstractArrayTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2ArrayTypeJdbcContractTest extends AbstractArrayTypeJdbcContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
