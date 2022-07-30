package net.hasor.dbvisitor.faker;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.faker.generator.*;
import org.junit.Test;

import java.util.List;

public class FakerEngineTest {
    @Test
    public void insertTest() throws Exception {
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());
        GeneratorTrans engine = new GeneratorTrans(fakerFactory);

        FakerTable table = engine.addTable(null, null, "tb_user");
        table.setInsertPolitic(SqlPolitic.RandomCol);

        for (int j = 0; j < 10; j++) {
            System.out.println(StringUtils.repeat("-", 20));
            List<BoundQuery> boundQueries = engine.generator();
            for (BoundQuery query : boundQueries) {
                System.out.println(query.getSqlString());
            }
        }
    }

}