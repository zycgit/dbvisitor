package net.hasor.dbvisitor.test.nxn.report.metadata;

import net.hasor.dbvisitor.test.nxn.report.NxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseNxnMetadataContractTest extends NxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
