package net.hasor.dbvisitor.test.realdb.mysql.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperResultHandlerContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlAnnotationMapperResultHandlerContractTest extends AnnotationMapperResultHandlerContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
