package net.hasor.dbvisitor.test.realdb.h2.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationSpecialTypeContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2AnnotationSpecialTypeContractTest extends AnnotationSpecialTypeContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
