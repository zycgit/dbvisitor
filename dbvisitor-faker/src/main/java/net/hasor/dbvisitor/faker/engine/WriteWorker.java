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
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.generator.BoundQuery;
import net.hasor.dbvisitor.faker.generator.SqlArg;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.transaction.DataSourceUtils;
import net.hasor.dbvisitor.transaction.TransactionCallbackWithoutResult;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.TransactionTemplateManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 写入器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
class WriteWorker implements ShutdownHook, Runnable {
    private final static Logger        logger = Logger.getLogger(WriteWorker.class);
    private final        String        threadName;
    private final        FakerEngine   engine;
    private final        DataSource    dataSource;
    private final        FakerConfig   fakerConfig;
    private final        FakerMonitor  monitor;
    private final        EventQueue    eventQueue;
    //
    private final        AtomicBoolean running;
    private volatile     Thread        workThread;
    private              List<String>  sqlTemp;

    public WriteWorker(String threadName, FakerEngine engine, FakerMonitor monitor, EventQueue eventQueue, DataSource dataSource, FakerConfig fakerConfig) {
        this.threadName = threadName;
        this.engine = engine;
        this.dataSource = dataSource;
        this.fakerConfig = fakerConfig;
        this.monitor = monitor;
        this.eventQueue = eventQueue;
        this.running = new AtomicBoolean(true);
    }

    @Override
    public void shutdown() {
        if (this.running.compareAndSet(true, false)) {
            if (this.workThread != null) {
                this.workThread.interrupt();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    private boolean testContinue() {
        return this.running.get() && !this.engine.isExitSignal() && !Thread.interrupted();
    }

    private static String newTranID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public void run() {
        this.sqlTemp = this.fakerConfig.isPrintSql() ? new ArrayList<>() : null;
        this.workThread = Thread.currentThread();
        this.workThread.setName(this.threadName);
        this.monitor.writerStart(this.threadName, this.workThread);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
        TransactionTemplate transTemplate = new TransactionTemplateManager(DataSourceUtils.getManager(this.dataSource));

        if (this.fakerConfig.getQueryTimeout() > 0) {
            jdbcTemplate.setQueryTimeout(this.fakerConfig.getQueryTimeout());
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
                    String tranID = newTranID();

                    transTemplate.execute((TransactionCallbackWithoutResult) tranStatus -> doBatch(tranID, jdbcTemplate, queries));
                } else {
                    doBatch(null, jdbcTemplate, queries);
                }
            } catch (InterruptedException e) {
                this.running.set(false);
                return;
            } catch (Throwable e) {
                this.monitor.workThrowable(this.threadName, e);
            }
        }
    }

    private void doBatch(final String tranID, JdbcTemplate jdbcTemplate, List<BoundQuery> batch) throws SQLException {
        for (BoundQuery event : batch) {
            if (!this.testContinue()) {
                return;
            }

            String useTranID = tranID;
            if (useTranID == null) {
                useTranID = newTranID();
            }

            try {
                int affectRows = doEvent(jdbcTemplate, event);
                this.monitor.recordMonitor(this.threadName, useTranID, event, affectRows);
            } catch (SQLException e) {
                if (this.fakerConfig.ignoreError(e)) {
                    this.monitor.recordFailed(this.threadName, useTranID, event, e);
                } else {
                    logger.error(e.getMessage() + " event is " + event, e);
                    throw e;
                }
            }
        }
    }

    private int doEvent(JdbcTemplate jdbcTemplate, BoundQuery event) throws SQLException {
        this.engine.checkQoS(); // 写入限流

        final String sqlString = event.getSqlString();
        final SqlArg[] sqlArgs = event.getArgs();

        if (this.sqlTemp != null && !this.sqlTemp.contains(sqlString)) {
            this.sqlTemp.add(sqlString);
            logger.info(sqlString);
        }

        return jdbcTemplate.executeUpdate(sqlString, ps -> {
            for (int i = 1; i <= sqlArgs.length; i++) {
                if (sqlArgs[i - 1] == null) {
                    ps.setObject(i, null);
                } else {
                    sqlArgs[i - 1].setParameter(ps, i);
                }
            }
        });
    }
}