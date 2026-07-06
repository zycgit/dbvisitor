package net.hasor.dbvisitor.test.realdb.db2.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperAttributeContractTest;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;
import net.hasor.dbvisitor.test.realdb.db2.material.Db2AnnotationAttributesMapper;

public class Db2AnnotationMapperAttributeContractTest extends AnnotationMapperAttributeContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }

    @Override
    protected Class<? extends AnnotationAttributesMapper> mapperType() {
        return Db2AnnotationAttributesMapper.class;
    }
}
