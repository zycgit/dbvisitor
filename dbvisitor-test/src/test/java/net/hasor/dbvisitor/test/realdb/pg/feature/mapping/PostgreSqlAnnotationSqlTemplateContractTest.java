package net.hasor.dbvisitor.test.realdb.pg.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationSqlTemplateContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlAnnotationSqlTemplateContractTest extends AnnotationSqlTemplateContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
