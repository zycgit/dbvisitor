package net.hasor.dbvisitor.faker.realdb;
import com.alibaba.druid.pool.DruidDataSource;
import net.hasor.dbvisitor.faker.DsUtils;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.engine.FakerEngine;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerRepository;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.loader.PrecociousDataLoaderFactory;

import java.sql.SQLException;

public class MySqlTest {
    public static void main(String[] args) throws SQLException {
        // 全局配置
        FakerConfig fakerConfig = new FakerConfig();
        fakerConfig.setMinBatchSizePerOps(1);
        fakerConfig.setMaxBatchSizePerOps(1);
        fakerConfig.setMinOpsCountPerTransaction(1);
        fakerConfig.setMaxOpsCountPerTransaction(1);
        fakerConfig.setTransaction(false);
        //        fakerConfig.setUseRadical(true);
        fakerConfig.setDataLoaderFactory(new PrecociousDataLoaderFactory());
        fakerConfig.addIgnoreError("Duplicate");
        fakerConfig.addIgnoreError("Data truncation: Incorrect datetime value");
        //        fakerConfig.setOpsRatio("INSERT#30");

        // 生成器，配置表
        DruidDataSource dataDs = DsUtils.dsMySql();
        FakerFactory factory = new FakerFactory(dataDs, fakerConfig);
        FakerRepository generator = new FakerRepository(factory);
        FakerTable table1 = generator.addTable("devtester", null, "tb_mysql_types");
        //        table1.setInsertPolitic(SqlPolitic.RandomKeyCol);
        //        table1.setUpdateSetPolitic(SqlPolitic.RandomKeyCol);
        //        table1.setWherePolitic(SqlPolitic.RandomKeyCol);
        //        FakerTable table2 = generator.addTable("devtester", null, "tb_mysql_geometry");
        //        table2.setInsertPolitic(SqlPolitic.FullCol);

        //        table.findColumn("c_geometrycollection").ignoreAct(UseFor.values());
        //        table2.apply();

        // 生成数据
        FakerEngine engine = new FakerEngine(dataDs, generator);
        engine.start(4, 40);

        // 监控信息
        long t = System.currentTimeMillis();
        while (!engine.isExitSignal()) {
            if ((t + 1000) < System.currentTimeMillis()) {
                t = System.currentTimeMillis();
                System.out.println(engine.getMonitor());
            }

            if (engine.getMonitor().getSucceedInsert() > 100) {
                System.out.println(engine.getMonitor());
                engine.shutdown();
            }
        }
    }
}