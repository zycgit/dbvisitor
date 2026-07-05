package net.hasor.dbvisitor.test.realdb.mysql.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationTypeHandlerContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlAnnotationTypeHandlerContractTest extends AnnotationTypeHandlerContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
