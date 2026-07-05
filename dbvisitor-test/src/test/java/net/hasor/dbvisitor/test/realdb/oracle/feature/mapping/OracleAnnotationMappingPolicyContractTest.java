package net.hasor.dbvisitor.test.realdb.oracle.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationMappingPolicyContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleAnnotationMappingPolicyContractTest extends AnnotationMappingPolicyContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
