/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.faker.engine;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.generator.BoundQuery;
import net.hasor.dbvisitor.faker.generator.SqlArg;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.transaction.DataSourceUtils;
import net.hasor.dbvisitor.transaction.TransactionCallbackWithoutResult;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;
import net.hasor.dbvisitor.types.TypeHandler;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 写入器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
class WriteWorker implements ShutdownHook, Runnable {
    private final    String        threadName;
    private final    DataSource    dataSource;
    private final    FakerConfig   fakerConfig;
    private final    FakerMonitor  monitor;
    private final    EventQueue    eventQueue;
    //
    private final    AtomicBoolean running;
    private volatile Thread        workThread;

    WriteWorker(String threadName, DataSource dataSource, FakerConfig fakerConfig, FakerMonitor monitor, EventQueue eventQueue) {
        this.threadName = threadName;
        this.dataSource = dataSource;
        this.fakerConfig = fakerConfig;
        this.monitor = monitor;
        this.eventQueue = eventQueue;
        this.running = new AtomicBoolean(true);
    }

    public void shutdown() {
        this.running.set(false);
        if (this.workThread != null) {
            this.workThread.interrupt();
        }
    }

    private boolean testContinue() {
        return this.running.get() && !this.monitor.ifPresentExit() && !Thread.interrupted();
    }

    @Override
    public void run() {
        this.workThread = Thread.currentThread();
        this.workThread.setName(this.threadName);
        this.monitor.writerStart(this.threadName, this.workThread);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
        TransactionTemplate transTemplate = new TransactionTemplateManager(DataSourceUtils.getManager(this.dataSource));

        if (this.fakerConfig.getQueryTimeout() > 0) {
            jdbcTemplate.setQueryTimeout(fakerConfig.getQueryTimeout());
        }

        while (this.testContinue()) {
            try {
                List<BoundQuery> queries = this.eventQueue.tryPoll();
                if (queries == null) {
                    Thread.sleep(100); // prevent empty loop
                    continue;
                }

                if (this.fakerConfig.isTransaction()) {
                    Thread.sleep(this.fakerConfig.randomPausePerTransactionMs());
                    String tranID = UUID.randomUUID().toString().replace("-", "");

                    transTemplate.execute((TransactionCallbackWithoutResult) tranStatus -> doBatch(tranID, jdbcTemplate, queries));
                } else {
                    doBatch(null, jdbcTemplate, queries);
                }
            } catch (Throwable e) {
                this.running.set(false);
                this.monitor.workExit(this.threadName, e);
                this.monitor.exitSignal();
                return;
            }
        }

        this.monitor.workExit(this.threadName, null);
    }

    private void doBatch(String tranID, JdbcTemplate jdbcTemplate, List<BoundQuery> batch) throws SQLException {
        for (BoundQuery event : batch) {
            if (!this.testContinue()) {
                return;
            }

            try {
                int affectRows = doEvent(jdbcTemplate, event);
                this.monitor.recordMonitor(this.threadName, tranID, event, affectRows);
            } catch (SQLException e) {
                if (this.fakerConfig.ignoreError(e)) {
                    this.monitor.recordFailed(this.threadName, tranID, event, e);
                } else {
                    throw e;
                }
            }
        }
    }

    private int doEvent(JdbcTemplate jdbcTemplate, BoundQuery event) throws SQLException {
        this.monitor.checkQoS(); // 写入限流

        final String sqlString = event.getSqlString();
        final SqlArg[] sqlArgs = event.getArgs();
        return jdbcTemplate.executeUpdate(sqlString, ps -> {
            for (int i = 1; i <= sqlArgs.length; i++) {
                SqlArg arg = sqlArgs[i - 1];
                if (arg.getObject() == null) {
                    ps.setNull(i, arg.getJdbcType());
                } else {
                    TypeHandler handler = arg.getHandler();
                    handler.setParameter(ps, i, arg.getObject(), arg.getJdbcType());
                }
            }
        });
    }
}