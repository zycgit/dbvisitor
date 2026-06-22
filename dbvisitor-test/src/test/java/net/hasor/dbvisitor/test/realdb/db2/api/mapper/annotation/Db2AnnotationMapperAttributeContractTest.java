package net.hasor.dbvisitor.test.realdb.db2.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AbstractAnnotationMapperAttributeContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2AnnotationMapperAttributeContractTest extends AbstractAnnotationMapperAttributeContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
