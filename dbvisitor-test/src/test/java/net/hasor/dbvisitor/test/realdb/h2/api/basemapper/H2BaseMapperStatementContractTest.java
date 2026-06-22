package net.hasor.dbvisitor.test.realdb.h2.api.basemapper;

import net.hasor.dbvisitor.test.contract.api.basemapper.AbstractBaseMapperStatementContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2BaseMapperStatementContractTest extends AbstractBaseMapperStatementContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
