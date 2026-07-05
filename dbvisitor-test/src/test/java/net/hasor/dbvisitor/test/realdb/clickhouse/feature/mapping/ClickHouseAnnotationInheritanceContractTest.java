package net.hasor.dbvisitor.test.realdb.clickhouse.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationInheritanceContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseAnnotationInheritanceContractTest extends AnnotationInheritanceContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
