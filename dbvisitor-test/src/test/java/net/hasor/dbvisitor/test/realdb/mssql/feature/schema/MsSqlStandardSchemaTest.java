package net.hasor.dbvisitor.test.realdb.mssql.feature.schema;

import net.hasor.dbvisitor.test.contract.feature.schema.StandardSchemaContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlStandardSchemaTest extends StandardSchemaContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
