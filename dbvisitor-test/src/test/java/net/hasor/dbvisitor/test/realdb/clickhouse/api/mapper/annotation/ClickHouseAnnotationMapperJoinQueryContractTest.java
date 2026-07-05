package net.hasor.dbvisitor.test.realdb.clickhouse.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperJoinQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseAnnotationMapperJoinQueryContractTest extends AnnotationMapperJoinQueryContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
