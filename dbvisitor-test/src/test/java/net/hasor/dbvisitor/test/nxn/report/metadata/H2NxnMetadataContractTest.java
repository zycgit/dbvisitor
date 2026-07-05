package net.hasor.dbvisitor.test.nxn.report.metadata;

import net.hasor.dbvisitor.test.nxn.report.NxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2NxnMetadataContractTest extends NxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
