package net.hasor.dbvisitor.test.realdb.h2.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationInheritanceContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2AnnotationInheritanceContractTest extends AnnotationInheritanceContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
