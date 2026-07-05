package net.hasor.dbvisitor.test.realdb.mysql.feature.schema;

import net.hasor.dbvisitor.test.contract.feature.schema.StandardSchemaContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlStandardSchemaTest extends StandardSchemaContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
