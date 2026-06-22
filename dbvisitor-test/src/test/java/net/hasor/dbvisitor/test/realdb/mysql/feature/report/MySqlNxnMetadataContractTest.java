package net.hasor.dbvisitor.test.realdb.mysql.feature.report;

import net.hasor.dbvisitor.test.contract.feature.report.AbstractNxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlNxnMetadataContractTest extends AbstractNxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
