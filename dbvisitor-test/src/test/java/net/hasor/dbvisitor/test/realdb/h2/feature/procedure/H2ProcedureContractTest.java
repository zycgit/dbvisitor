package net.hasor.dbvisitor.test.realdb.h2.feature.procedure;

import net.hasor.dbvisitor.test.contract.feature.procedure.ProcedureContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2ProcedureContractTest extends ProcedureContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }
}
