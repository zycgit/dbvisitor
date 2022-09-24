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

            //            {
            //                for (String col : table.getColumns()) {
            //                    if (col.startsWith("a_")) {//
            //                        table.findColumn(col).ignoreReset();
            //                    } else {
            //                        table.findColumn(col).ignoreAct(UseFor.values());
            //                    }
            //                }
            //                table.apply();
            //            }

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
//
//严重: unsupported columnName c_point, columnType 'point'
//严重: unsupported columnName c_line, columnType 'line'
//严重: unsupported columnName c_lseg, columnType 'lseg'
//严重: unsupported columnName c_box, columnType 'box'
//严重: unsupported columnName c_path, columnType 'path'
//严重: unsupported columnName c_polygon, columnType 'polygon'
//严重: unsupported columnName c_circle, columnType 'circle'
//
//严重: unsupported columnName c_cidr, columnType 'cidr'
//严重: unsupported columnName c_inet, columnType 'inet'
//严重: unsupported columnName c_macaddr, columnType 'macaddr'
//严重: unsupported columnName c_macaddr8, columnType 'macaddr8'
//严重: unsupported columnName c_tsvector, columnType 'tsvector'
//严重: unsupported columnName c_tsquery, columnType 'tsquery'
//严重: unsupported columnName c_xml, columnType 'xml'
//严重: unsupported columnName c_json, columnType 'json'
//严重: unsupported columnName c_jsonb, columnType 'jsonb'
//严重: unsupported columnName c_int4range, columnType 'int4range'
//严重: unsupported columnName c_int8range, columnType 'int8range'
//严重: unsupported columnName c_numrange, columnType 'numrange'
//严重: unsupported columnName c_tsrange, columnType 'tsrange'
//严重: unsupported columnName c_tstzrange, columnType 'tstzrange'
//严重: unsupported columnName c_daterange, columnType 'daterange'
//
//严重: unsupported columnName c_pg_lsn, columnType 'pg_lsn'
//严重: unsupported columnName c_txid_snapshot, columnType 'txid_snapshot'
//
//严重: unsupported columnName a_point, columnType 'point'
//严重: unsupported columnName a_line, columnType 'line'
//严重: unsupported columnName a_lseg, columnType 'lseg'
//严重: unsupported columnName a_box, columnType 'box'
//严重: unsupported columnName a_path, columnType 'path'
//严重: unsupported columnName a_polygon, columnType 'polygon'
//严重: unsupported columnName a_circle, columnType 'circle'
//严重: unsupported columnName a_cidr, columnType 'cidr'
//严重: unsupported columnName a_inet, columnType 'inet'
//严重: unsupported columnName a_macaddr, columnType 'macaddr'
//严重: unsupported columnName a_macaddr8, columnType 'macaddr8'
//严重: unsupported columnName a_tsvector, columnType 'tsvector'
//严重: unsupported columnName a_tsquery, columnType 'tsquery'
//严重: unsupported columnName a_xml, columnType 'xml'
//严重: unsupported columnName a_json, columnType 'json'
//严重: unsupported columnName a_jsonb, columnType 'jsonb'
//严重: unsupported columnName a_int4range, columnType 'int4range'
//严重: unsupported columnName a_int8range, columnType 'int8range'
//严重: unsupported columnName a_numrange, columnType 'numrange'
//严重: unsupported columnName a_tsrange, columnType 'tsrange'
//严重: unsupported columnName a_tstzrange, columnType 'tstzrange'
//严重: unsupported columnName a_daterange, columnType 'daterange'
//严重: unsupported columnName a_pg_lsn, columnType 'pg_lsn'
//严重: unsupported columnName a_txid_snapshot, columnType 'txid_snapshot'