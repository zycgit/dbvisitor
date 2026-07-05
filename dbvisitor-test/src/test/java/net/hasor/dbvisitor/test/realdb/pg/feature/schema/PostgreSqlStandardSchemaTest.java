package net.hasor.dbvisitor.test.realdb.pg.feature.schema;

import net.hasor.dbvisitor.test.contract.feature.schema.StandardSchemaContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlStandardSchemaTest extends StandardSchemaContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
