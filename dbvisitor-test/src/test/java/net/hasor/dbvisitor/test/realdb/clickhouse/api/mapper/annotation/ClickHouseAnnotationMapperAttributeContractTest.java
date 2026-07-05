package net.hasor.dbvisitor.test.realdb.clickhouse.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperAttributeContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseAnnotationMapperAttributeContractTest extends AnnotationMapperAttributeContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
