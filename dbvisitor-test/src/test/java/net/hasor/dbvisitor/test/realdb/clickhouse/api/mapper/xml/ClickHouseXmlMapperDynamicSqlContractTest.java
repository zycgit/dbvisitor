package net.hasor.dbvisitor.test.realdb.clickhouse.api.mapper.xml;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperDynamicSqlContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;

public class ClickHouseXmlMapperDynamicSqlContractTest extends XmlMapperDynamicSqlContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
