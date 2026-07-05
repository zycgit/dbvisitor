package net.hasor.dbvisitor.test.realdb.clickhouse.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationMappingPolicyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseAnnotationMappingPolicyContractTest extends AnnotationMappingPolicyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
