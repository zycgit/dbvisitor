package net.hasor.dbvisitor.test.realdb.oracle.feature.schema;

import net.hasor.dbvisitor.test.contract.feature.schema.StandardSchemaContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleStandardSchemaTest extends StandardSchemaContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
