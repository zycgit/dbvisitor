package net.hasor.dbvisitor.test.realdb.oracle.feature.schema;

import net.hasor.dbvisitor.test.contract.feature.schema.AbstractStandardSchemaContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleStandardSchemaContractTest extends AbstractStandardSchemaContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
