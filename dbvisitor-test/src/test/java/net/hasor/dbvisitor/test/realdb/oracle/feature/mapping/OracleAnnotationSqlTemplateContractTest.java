package net.hasor.dbvisitor.test.realdb.oracle.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationSqlTemplateContractTest;
import net.hasor.dbvisitor.test.contract.material.model.annotation.AbstractMd5User;
import net.hasor.dbvisitor.test.contract.material.model.annotation.OracleMd5User;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleAnnotationSqlTemplateContractTest extends AnnotationSqlTemplateContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }

    @Override
    protected Class<? extends AbstractMd5User> md5UserType() {
        return OracleMd5User.class;
    }
}
