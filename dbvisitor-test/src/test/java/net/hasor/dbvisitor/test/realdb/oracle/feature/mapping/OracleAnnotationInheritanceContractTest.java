package net.hasor.dbvisitor.test.realdb.oracle.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationInheritanceContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleAnnotationInheritanceContractTest extends AnnotationInheritanceContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
