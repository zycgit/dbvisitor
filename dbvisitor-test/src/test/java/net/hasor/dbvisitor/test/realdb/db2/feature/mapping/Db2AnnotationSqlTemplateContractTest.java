package net.hasor.dbvisitor.test.realdb.db2.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationSqlTemplateContractTest;
import net.hasor.dbvisitor.test.contract.material.model.annotation.AbstractMd5User;
import net.hasor.dbvisitor.test.contract.material.model.annotation.Db2Md5User;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2AnnotationSqlTemplateContractTest extends AnnotationSqlTemplateContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Override
    protected Class<? extends AbstractMd5User> md5UserType() {
        return Db2Md5User.class;
    }
}
