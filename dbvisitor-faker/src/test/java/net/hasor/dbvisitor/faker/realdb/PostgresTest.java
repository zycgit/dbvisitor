package net.hasor.dbvisitor.faker.realdb;
import com.alibaba.druid.pool.DruidDataSource;
import net.hasor.dbvisitor.faker.DsUtils;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.engine.FakerEngine;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerRepository;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.loader.PrecociousDataLoaderFactory;

public class PostgresTest {
    public static void main(String[] args) throws Exception {
        // LoggerFactory.useStdOutLogger();
        // 全局配置
        FakerConfig fakerConfig = new FakerConfig();
        fakerConfig.setTransaction(false);
        //        fakerConfig.setPolicy("extreme");
        fakerConfig.setDataLoaderFactory(new PrecociousDataLoaderFactory());
        fakerConfig.addIgnoreError("Duplicate");
        fakerConfig.addIgnoreError("restarting");
        fakerConfig.addIgnoreError("deadlocked");
        fakerConfig.addIgnoreError("was deadlocked on lock");
        fakerConfig.addIgnoreError("duplicate key");
        fakerConfig.setOpsRatio("I#30;U#10;D#5");

        // 生成器，配置表
        DruidDataSource dataDs = DsUtils.dsPg();
        FakerFactory factory = new FakerFactory(dataDs, fakerConfig);
        FakerRepository generator = new FakerRepository(factory);
        FakerTable table = generator.addTable("postgres", "public", "tb_postgre_types");
        //        table.setInsertPolitic(SqlPolitic.FullCol);
        table.apply();

        // 生成数据
        FakerEngine engine = new FakerEngine(dataDs, generator);
        engine.start(23, 5);

        // 监控信息
        long t = System.currentTimeMillis();
        while (!engine.isExitSignal()) {
            if ((t + 1000) < System.currentTimeMillis()) {
                t = System.currentTimeMillis();
                System.out.println(engine.getMonitor());
            }

            if (engine.getMonitor().getSucceedInsert() > 10000) {
                System.out.println(engine.getMonitor());
                engine.shutdown();
            }
        }
    }
}