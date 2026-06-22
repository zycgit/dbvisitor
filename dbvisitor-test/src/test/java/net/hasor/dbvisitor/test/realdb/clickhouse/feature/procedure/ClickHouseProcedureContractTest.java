package net.hasor.dbvisitor.test.realdb.clickhouse.feature.procedure;

import net.hasor.dbvisitor.test.contract.feature.procedure.AbstractProcedureContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseProcedureContractTest extends AbstractProcedureContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
