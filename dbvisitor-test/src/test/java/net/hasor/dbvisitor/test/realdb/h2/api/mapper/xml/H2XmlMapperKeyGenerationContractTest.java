package net.hasor.dbvisitor.test.realdb.h2.api.mapper.xml;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperKeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;

public class H2XmlMapperKeyGenerationContractTest extends XmlMapperKeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return H2Profile.INSTANCE;
    }

    @Override
    protected String mapperResource() {
        return "/realdb/h2/material/XmlKeyGenerationMapper.xml";
    }
}
