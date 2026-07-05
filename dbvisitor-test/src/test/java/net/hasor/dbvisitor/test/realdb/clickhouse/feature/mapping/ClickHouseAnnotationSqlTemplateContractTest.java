package net.hasor.dbvisitor.test.realdb.clickhouse.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationSqlTemplateContractTest;
import net.hasor.dbvisitor.test.contract.material.model.annotation.AbstractMd5User;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ClickHouseMd5User;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseAnnotationSqlTemplateContractTest extends AnnotationSqlTemplateContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Override
    protected Class<? extends AbstractMd5User> md5UserType() {
        return ClickHouseMd5User.class;
    }
}
