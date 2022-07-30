package net.hasor.dbvisitor.faker;
import net.hasor.dbvisitor.faker.engine.FakerEngine;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.SqlPolitic;
import org.junit.Test;

public class FakerEngineTest {
    @Test
    public void insertTest() throws Exception {
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());
        FakerEngine engine = new FakerEngine(fakerFactory);

        FakerTable table = engine.setupTable(null, null, "tb_user");
        table.setInsertPolitic(SqlPolitic.RandomCol);

        for (int j = 0; j < 10; j++) {
            //            FakerEventSet dataSet = engine.syncGeneratorTable();

            //            dataSet.
            //            List<BoundQuery> boundQueries = table.buildInsert(1);
            //            for (BoundQuery query : boundQueries) {
            //                System.out.println(query.getSqlString());
            //            }
        }
    }

}