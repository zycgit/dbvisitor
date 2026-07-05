package net.hasor.dbvisitor.test.realdb.db2.api.map_query;

import net.hasor.dbvisitor.test.contract.api.map_query.FreedomMapCrudContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2FreedomMapCrudTest extends FreedomMapCrudContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
