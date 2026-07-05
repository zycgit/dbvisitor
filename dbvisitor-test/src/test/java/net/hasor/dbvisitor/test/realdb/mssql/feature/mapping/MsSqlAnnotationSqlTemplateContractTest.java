package net.hasor.dbvisitor.test.realdb.mssql.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationSqlTemplateContractTest;
import net.hasor.dbvisitor.test.contract.material.model.annotation.AbstractMd5User;
import net.hasor.dbvisitor.test.contract.material.model.annotation.MsSqlMd5User;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlAnnotationSqlTemplateContractTest extends AnnotationSqlTemplateContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Override
    protected Class<? extends AbstractMd5User> md5UserType() {
        return MsSqlMd5User.class;
    }
}
