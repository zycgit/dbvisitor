package net.hasor.dbvisitor.test.realdb.h2.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperResultHandlerContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2AnnotationMapperResultHandlerContractTest extends AnnotationMapperResultHandlerContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
