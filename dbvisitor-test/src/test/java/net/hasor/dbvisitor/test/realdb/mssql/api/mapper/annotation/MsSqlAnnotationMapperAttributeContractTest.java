package net.hasor.dbvisitor.test.realdb.mssql.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperAttributeContractTest;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;
import net.hasor.dbvisitor.test.realdb.mssql.material.MsSqlAnnotationAttributesMapper;

public class MsSqlAnnotationMapperAttributeContractTest extends AnnotationMapperAttributeContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Override
    protected Class<? extends AnnotationAttributesMapper> mapperType() {
        return MsSqlAnnotationAttributesMapper.class;
    }
}
