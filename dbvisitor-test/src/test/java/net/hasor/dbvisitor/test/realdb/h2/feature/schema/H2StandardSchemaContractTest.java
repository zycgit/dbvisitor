package net.hasor.dbvisitor.test.realdb.h2.feature.schema;

import net.hasor.dbvisitor.test.contract.feature.schema.AbstractStandardSchemaContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2StandardSchemaContractTest extends AbstractStandardSchemaContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
