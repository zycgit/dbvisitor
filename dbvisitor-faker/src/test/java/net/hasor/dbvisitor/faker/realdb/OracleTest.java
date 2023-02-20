package net.hasor.dbvisitor.faker.realdb;

import com.alibaba.druid.pool.DruidDataSource;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.faker.DsUtils;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.engine.FakerEngine;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerRepository;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.SqlPolitic;
import net.hasor.dbvisitor.faker.generator.loader.PrecociousDataLoaderFactory;

public class OracleTest {
    public static void main(String[] args) throws Exception {
        // 全局配置
//        LoggerFactory.useStdOutLogger();
        FakerConfig fakerConfig = new FakerConfig();
        fakerConfig.setTransaction(false);
                fakerConfig.setPolicy("extreme");
        fakerConfig.setDataLoaderFactory(new PrecociousDataLoaderFactory());
        fakerConfig.addIgnoreError("ORA-00001");
        fakerConfig.setOpsRatio("I#30;U#10;D#10");
        //        fakerConfig.addIgnoreError("restarting");
        //        fakerConfig.addIgnoreError("deadlocked");
        //        fakerConfig.addIgnoreError("was deadlocked on lock");
        //        fakerConfig.setOpsRatio("D#30");

        // 生成器，配置表
        DruidDataSource dataDs = DsUtils.dsOracle();
        FakerFactory factory = new FakerFactory(dataDs, fakerConfig);
        FakerRepository generator = new FakerRepository(factory);
        // FakerTable table = generator.addTable("console", "dbo", "tb_sqlserver_types");
        // FakerTable table = generator.addTable("console", "dbo", "stock");
        FakerTable table = generator.addTable(null, "SCOTT", "TB_ORACLE_TYPES");
        table.setInsertPolitic(SqlPolitic.FullCol);
        table.apply();

        // 生成数据
        FakerEngine engine = new FakerEngine(dataDs, generator);
        engine.start(1, 1);

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
