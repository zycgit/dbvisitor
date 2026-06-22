package net.hasor.dbvisitor.test.realdb.oracle.feature.report;

import net.hasor.dbvisitor.test.contract.feature.report.AbstractNxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleNxnMetadataContractTest extends AbstractNxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
