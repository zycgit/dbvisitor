package net.hasor.dbvisitor.test.realdb.clickhouse.feature.report;

import net.hasor.dbvisitor.test.contract.feature.report.AbstractNxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseNxnMetadataContractTest extends AbstractNxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
