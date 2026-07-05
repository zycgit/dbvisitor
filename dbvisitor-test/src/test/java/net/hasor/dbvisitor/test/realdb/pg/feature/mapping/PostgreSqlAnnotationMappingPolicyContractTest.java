package net.hasor.dbvisitor.test.realdb.pg.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationMappingPolicyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlAnnotationMappingPolicyContractTest extends AnnotationMappingPolicyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
