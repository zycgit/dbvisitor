package net.hasor.dbvisitor.faker.realdb;
import net.hasor.dbvisitor.faker.DsUtils;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.engine.FakerEngine;
import net.hasor.dbvisitor.faker.engine.FakerMonitor;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerGenerator;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.SqlPolitic;
import net.hasor.dbvisitor.faker.generator.loader.PrecociousDataLoaderFactory;
import org.junit.Test;

public class MySqlTest {

    @Test
    public void workloadTest() throws Exception {
        // 全局配置
        FakerConfig fakerConfig = new FakerConfig();
        fakerConfig.setTransaction(false);
        //        fakerConfig.setUseRadical(true);
        fakerConfig.setDataLoaderFactory(new PrecociousDataLoaderFactory());
        fakerConfig.addIgnoreError("Duplicate");
        fakerConfig.addIgnoreError("Data truncation: Incorrect datetime value");
        fakerConfig.setOpsRatio("INSERT#30");

        // 生成器，配置表
        FakerFactory factory = new FakerFactory(DsUtils.dsMySql(), fakerConfig);
        FakerGenerator generator = new FakerGenerator(factory);
        //        FakerTable table1 = generator.addTable("devtester", null, "tb_mysql_types");
        FakerTable table2 = generator.addTable("devtester", null, "tb_mysql_geometry");
        table2.setInsertPolitic(SqlPolitic.FullCol);

        //        table.findColumn("c_geometrycollection").ignoreAct(UseFor.values());
        table2.apply();

        // 生成数据
        FakerEngine fakerEngine = new FakerEngine(factory);
        fakerEngine.startProducer(generator, 4);
        fakerEngine.startWriter(generator, 40);

        // 监控信息
        FakerMonitor monitor = fakerEngine.getMonitor();
        long t = System.currentTimeMillis();
        while (!monitor.ifPresentExit()) {
            if (fakerEngine.getMonitor().getSucceedInsert() > 10000) {
                fakerEngine.shutdown();
            }

            if ((t + 1000) < System.currentTimeMillis()) {
                t = System.currentTimeMillis();
                System.out.println(monitor);
            }
        }
    }
}