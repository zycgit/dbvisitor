package net.hasor.dbvisitor.test.realdb.mssql.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AbstractAnnotationMapperCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlAnnotationMapperCrudContractTest extends AbstractAnnotationMapperCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
