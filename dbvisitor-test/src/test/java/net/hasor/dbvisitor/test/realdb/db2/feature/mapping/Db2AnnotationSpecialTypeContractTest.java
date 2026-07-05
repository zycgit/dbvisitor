package net.hasor.dbvisitor.test.realdb.db2.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationSpecialTypeContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2AnnotationSpecialTypeContractTest extends AnnotationSpecialTypeContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
