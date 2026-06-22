package net.hasor.dbvisitor.test.realdb.mssql.feature.report;

import net.hasor.dbvisitor.test.contract.feature.report.AbstractNxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlNxnMetadataContractTest extends AbstractNxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
