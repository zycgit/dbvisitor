package net.hasor.dbvisitor.test.realdb.mysql.feature.schema;

import net.hasor.dbvisitor.test.contract.feature.schema.AbstractStandardSchemaContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlStandardSchemaContractTest extends AbstractStandardSchemaContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
