package com.example.demo;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.engine.FakerEngine;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerRepository;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.loader.PrecociousDataLoaderFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class MySqlTest {

    public static void main(String[] args) throws SQLException, IOException {
        //net.hasor.cobble.logging.LoggerFactory.useStdOutLogger(); // 如果想控制台上看日志就打开这个（日志输出选择器）

        // 准备 Faker 的配置
        FakerConfig fakerConfig = new FakerConfig();
        // 使用 widely 策略生成数据（可选的策略有：widely 随机数范围比较保守【默认】、extreme 随机数范围贴近数据库极端）
        fakerConfig.setPolicy("widely");
        // 不使用事务
        fakerConfig.setTransaction(false);
        // 配置反查器加速 Update/Delete
        fakerConfig.setDataLoaderFactory(new PrecociousDataLoaderFactory());
        // 当执行中遇到报错，错误信息中含有如下字符串则跳过错误
        fakerConfig.addIgnoreError("Duplicate");
        fakerConfig.addIgnoreError("Data truncation: Incorrect datetime value");

        // I#30 表示 30% Insert
        // U#10 表示 10% Update
        // D#10 表示 10% Delete
        fakerConfig.setOpsRatio("I#30;U#10;D#10");
        // fakerConfig.setOpsRatio("I#30"); // 仅有一种操作情况下，生成比率 >0 随意填

        // 创建数据源
        String jdbcUrl = "jdbc:mysql://127.0.0.1:13306/devtester?allowMultiQueries=true";
        DataSource dataDs = DsUtils.dsMySql(jdbcUrl, "devtester", "123456");

        // 创建 Faker
        FakerFactory factory = new FakerFactory(dataDs, fakerConfig);
        FakerRepository generator = new FakerRepository(factory);
        FakerEngine engine = new FakerEngine(dataDs, generator);

        // 向 faker 添加待造数据的表
        FakerTable table = generator.addTable("devtester", null, "tb_mysql_types");

        // 启动造数据工具，使用 4 个数据生产线程，40个数据写入线程
        engine.start(4, 40);

        // 每隔 1秒打印一次监控信息,当执行记录条数超过 10000 条后退出造数据过程
        long succeedExit = 10000;

        long t = System.currentTimeMillis();
        while (!engine.isExitSignal()) {
            if ((t + 1000) < System.currentTimeMillis()) {
                t = System.currentTimeMillis();
                System.out.println(engine.getMonitor());
            }

            if (engine.getMonitor().getSucceed() > succeedExit) {
                System.out.println(engine.getMonitor());
                engine.shutdown();
            }
        }
    }
}