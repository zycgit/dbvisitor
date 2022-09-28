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

public class SqlServerTest {
    @Test
    public void workloadTest() throws Exception {
        // 全局配置
        FakerConfig fakerConfig = new FakerConfig();
        fakerConfig.setTransaction(false);
        //        fakerConfig.setUseRadical(true);
        fakerConfig.setDataLoaderFactory(new PrecociousDataLoaderFactory());
        fakerConfig.addIgnoreError("Duplicate");
        fakerConfig.addIgnoreError("restarting");
        fakerConfig.addIgnoreError("deadlocked");
        fakerConfig.addIgnoreError("was deadlocked on lock");
        fakerConfig.addIgnoreError("违反了 PRIMARY KEY");
        fakerConfig.addIgnoreError("违反了 UNIQUE KEY");
        fakerConfig.addIgnoreError("The incoming tabular data stream (TDS) remote procedure call (RPC) protocol stream is incorrect");
        //        fakerConfig.setOpsRatio("INSERT#30");

        // 生成器，配置表
        FakerFactory factory = new FakerFactory(DsUtils.dsSqlServer(), fakerConfig);
        FakerGenerator generator = new FakerGenerator(factory);
        FakerTable table = generator.addTable("tester", "dbo", "tb_sqlserver_types");
        table.setInsertPolitic(SqlPolitic.FullCol);
        table.apply();

        // 生成数据
        FakerEngine fakerEngine = new FakerEngine(factory);
        fakerEngine.startProducer(generator, 1);
        fakerEngine.startWriter(generator, 20);

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