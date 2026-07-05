package net.hasor.dbvisitor.test.realdb.mysql.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperParameterBindingContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlAnnotationMapperParameterBindingContractTest extends AnnotationMapperParameterBindingContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
