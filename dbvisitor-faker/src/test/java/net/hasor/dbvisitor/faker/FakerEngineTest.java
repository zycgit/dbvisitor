package net.hasor.dbvisitor.faker;
import net.hasor.dbvisitor.faker.engine.FakerEngine;
import net.hasor.dbvisitor.faker.engine.FakerMonitor;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerGenerator;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.SqlPolitic;
import net.hasor.dbvisitor.faker.generator.loader.PrecociousDataLoaderFactory;
import net.hasor.dbvisitor.faker.seed.string.StringSeedConfig;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static net.hasor.dbvisitor.faker.seed.string.CharacterSet.CJK_UNIFIED_IDEOGRAPHS;

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
        FakerTable table = generator.addTable(null, null, "tb_user");
        table.setInsertPolitic(SqlPolitic.RandomCol);
        ((StringSeedConfig) table.findColumns("userUUID").seedConfig()).setCharacterSet(new HashSet<>(Collections.singletonList(CJK_UNIFIED_IDEOGRAPHS)));
        table.apply();

        fakerEngine.startProducer(generator, 8);
        fakerEngine.startWriter(generator, 120);

        FakerMonitor monitor = fakerEngine.getMonitor();
        long t = System.currentTimeMillis();
        while (!monitor.ifPresentExit()) {
            if ((t + 1000) < System.currentTimeMillis()) {
                t = System.currentTimeMillis();
                System.out.println(monitor);
            }
        }
    }
}