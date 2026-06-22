package net.hasor.dbvisitor.test.realdb.pg.feature.schema;

import net.hasor.dbvisitor.test.contract.feature.schema.AbstractStandardSchemaContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlStandardSchemaContractTest extends AbstractStandardSchemaContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
