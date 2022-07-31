package net.hasor.dbvisitor.faker;
import net.hasor.dbvisitor.faker.engine.FakerEngine;
import net.hasor.dbvisitor.faker.engine.FakerMonitor;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerGenerator;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.SqlPolitic;
import org.junit.Test;

public class FakerEngineTest {
    @Test
    public void insertTest() throws Exception {
        // 工厂
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());
        fakerFactory.getFakerConfig().addIgnoreError("Duplicate");

        // 引擎
        FakerEngine fakerEngine = new FakerEngine(fakerFactory);

        // 生成器
        FakerGenerator generator = new FakerGenerator(fakerFactory);
        FakerTable table = generator.addTable(null, null, "tb_user");
        table.setInsertPolitic(SqlPolitic.RandomCol);
        //        table.findColumns("registerTime").ignoreAct(UseFor.Insert);
        // table.apply();

        fakerEngine.startProducer(generator, 8);
        fakerEngine.startWriter(generator, 10);

        FakerMonitor monitor = fakerEngine.getMonitor();
        long t = System.currentTimeMillis();
        while (true) {
            if ((t + 1000) < System.currentTimeMillis()) {
                t = System.currentTimeMillis();
                System.out.println(monitor);
            }
        }
    }
}