package net.hasor.dbvisitor.faker;
import net.hasor.cobble.setting.provider.StreamType;
import net.hasor.dbvisitor.faker.engine.FakerEngine;
import net.hasor.dbvisitor.faker.engine.FakerMonitor;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerGenerator;
import net.hasor.dbvisitor.faker.generator.loader.PrecociousDataLoaderFactory;
import org.junit.Test;

public class FakerEngineTest {
    @Test
    public void insertTest() throws Exception {
        FakerConfig fakerConfig = new FakerConfig();
        fakerConfig.setDataLoaderFactory(new PrecociousDataLoaderFactory());
        // 工厂
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql(), fakerConfig);
        fakerFactory.getFakerConfig().addIgnoreError("Duplicate");
        fakerFactory.getFakerConfig().addIgnoreError("restarting");

        // 引擎
        FakerEngine fakerEngine = new FakerEngine(fakerFactory);

        // 生成器
        FakerGenerator generator = new FakerGenerator(fakerFactory);
        generator.loadConfig("ds-mysql-config.yaml", StreamType.Yaml);
        //        FakerTable table = generator.addTable(null, null, "tb_user");
        //        table.setInsertPolitic(SqlPolitic.RandomCol);
        //        ((StringSeedConfig) table.findColumns("userUUID").seedConfig()).addCharacter(CJK_UNIFIED_IDEOGRAPHS);
        //        table.apply();

        fakerEngine.startProducer(generator, 8);
        fakerEngine.startWriter(generator, 120);

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