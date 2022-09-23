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

public class PostgresTest {
    @Test
    public void workloadTest() throws Exception {
        {
            // 全局配置
            FakerConfig fakerConfig = new FakerConfig();
            fakerConfig.setTransaction(false);
            fakerConfig.setUseRadical(true);
            fakerConfig.setDataLoaderFactory(new PrecociousDataLoaderFactory());
            fakerConfig.addIgnoreError("Duplicate");
            fakerConfig.addIgnoreError("restarting");
            fakerConfig.addIgnoreError("deadlocked");
            fakerConfig.addIgnoreError("was deadlocked on lock");
            fakerConfig.addIgnoreError("The incoming tabular data stream (TDS) remote procedure call (RPC) protocol stream is incorrect");
            //        fakerConfig.setOpsRatio("INSERT#30");

            // 生成器，配置表
            FakerFactory factory = new FakerFactory(DsUtils.dsPg(), fakerConfig);
            FakerGenerator generator = new FakerGenerator(factory);
            FakerTable table = generator.addTable("postgres", "public", "tb_postgre_types");
            table.setInsertPolitic(SqlPolitic.FullCol);
            table.apply();

            // c_interval, columnType 'interval'
            // c_interval_year, columnType 'interval'
            // c_interval_month, columnType 'interval'
            // c_interval_day, columnType 'interval'
            // c_interval_hour, columnType 'interval'
            // c_interval_minute, columnType 'interval'
            // c_interval_second, columnType 'interval'
            // c_interval_year_to_month, columnType 'interval'
            // c_interval_day_to_hour, columnType 'interval'
            // c_interval_day_to_minute, columnType 'interval'
            // c_interval_day_to_second, columnType 'interval'
            // c_interval_hour_to_minute, columnType 'interval'
            // c_interval_hour_to_second, columnType 'interval'
            // c_interval_minute_to_second, columnType 'interval'
            // c_point, columnType 'point'
            // c_line, columnType 'line'
            // c_lseg, columnType 'lseg'
            // c_box, columnType 'box'
            // c_path, columnType 'path'
            // c_polygon, columnType 'polygon'
            // c_circle, columnType 'circle'
            // c_cidr, columnType 'cidr'
            // c_inet, columnType 'inet'
            // c_macaddr, columnType 'macaddr'
            // c_macaddr8, columnType 'macaddr8'
            // c_tsvector, columnType 'tsvector'
            // c_tsquery, columnType 'tsquery'
            // c_xml, columnType 'xml'
            // c_json, columnType 'json'
            // c_jsonb, columnType 'jsonb'
            // c_int4range, columnType 'int4range'
            // c_int8range, columnType 'int8range'
            // c_numrange, columnType 'numrange'
            // c_tsrange, columnType 'tsrange'
            // c_tstzrange, columnType 'tstzrange'
            // c_daterange, columnType 'daterange'
            // c_pg_lsn, columnType 'pg_lsn'
            // c_txid_snapshot, columnType 'txid_snapshot'

            // 生成数据
            FakerEngine fakerEngine = new FakerEngine(factory);
            fakerEngine.startProducer(generator, 1);
            fakerEngine.startWriter(generator, 20);

            // 监控信息
            FakerMonitor monitor = fakerEngine.getMonitor();
            long t = System.currentTimeMillis();
            while (!monitor.ifPresentExit()) {
                if (fakerEngine.getMonitor().getSucceedInsert() > 1000000) {
                    fakerEngine.shutdown();
                }

                if ((t + 1000) < System.currentTimeMillis()) {
                    t = System.currentTimeMillis();
                    System.out.println(monitor);
                }
            }
        }
    }
}
