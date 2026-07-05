package net.hasor.dbvisitor.test.nxn.report.metadata;

import net.hasor.dbvisitor.test.nxn.report.NxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlNxnMetadataContractTest extends NxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
