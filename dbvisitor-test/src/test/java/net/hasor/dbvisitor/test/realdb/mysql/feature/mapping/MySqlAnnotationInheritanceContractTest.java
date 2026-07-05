package net.hasor.dbvisitor.test.realdb.mysql.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationInheritanceContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlAnnotationInheritanceContractTest extends AnnotationInheritanceContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
