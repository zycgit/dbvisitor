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
import net.hasor.cobble.concurrent.QoSBucket;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.generator.FakerRepository;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 压力引擎
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerEngine {
    private final DataSource         dataSource;
    private final FakerConfig        fakerConfig;
    private final FakerRepository    repository;
    //
    private final QoSBucket          qosBucket;
    private final AtomicBoolean      exitSignal;
    private final ThreadFactory      threadFactory;
    private final EventQueue         eventQueue;
    private final List<ShutdownHook> workers;
    private final FakerMonitor       monitor;

    public FakerEngine(DataSource dataSource, FakerRepository repository) {
        this.dataSource = dataSource;
        this.fakerConfig = repository.getConfig();
        this.repository = repository;
        this.eventQueue = new EventQueue(this.fakerConfig.getQueueCapacity());

        this.qosBucket = (this.fakerConfig.getWriteQps() > 0) ? new QoSBucket(fakerConfig.getWriteQps()) : null;
        this.exitSignal = new AtomicBoolean(true);
        this.workers = new CopyOnWriteArrayList<>();
        this.monitor = new FakerMonitor(this.eventQueue);

        if (this.fakerConfig.getThreadFactory() != null) {
            this.threadFactory = this.fakerConfig.getThreadFactory();
        } else {
            this.threadFactory = Thread::new;
        }
    }

    /** 各 worker 否退出？ */
    public boolean isExitSignal() {
        return this.exitSignal.get();
    }

    /** 写入限流 */
    void checkQoS() {
        if (this.qosBucket != null) {
            this.qosBucket.check();
        }
    }

    public FakerMonitor getMonitor() {
        return this.monitor;
    }

    /** 启动引擎 */
    public synchronized void start(int pThreadCnt, int wThreadCnt) {
        if (!this.exitSignal.compareAndSet(true, false)) {
            throw new IllegalStateException("the engine started.");
        }

        String repositoryID = this.repository.getGeneratorID();

        for (int i = 0; i < pThreadCnt; i++) {
            String workName = String.format("generator[%s-%s]", repositoryID, i);
            this.workers.add(new ProducerWorker(workName, this, this.monitor, this.eventQueue, this.repository));
        }

        for (int i = 0; i < wThreadCnt; i++) {
            String workName = String.format("writer[%s-%s]", repositoryID, i);
            this.workers.add(new WriteWorker(workName, this, this.monitor, this.eventQueue, this.dataSource, this.fakerConfig));
        }

        for (ShutdownHook worker : this.workers) {
            this.threadFactory.newThread(worker).start();
        }
    }

    /** 停止引擎，并清空监控状态 */
    public void shutdown() {
        try {
            this.shutdown(-1, null);
        } catch (TimeoutException ignored) {
        }
    }

    /** 停止引擎，并清空监控状态 */
    public void shutdown(int waitTime, TimeUnit timeUnit) throws TimeoutException {
        if (!this.exitSignal.compareAndSet(false, true)) {
            throw new IllegalStateException("the engine state is going to shutdown or not start");
        }

        // do stop
        long timeout = (waitTime <= 0) ? (-1) : (System.currentTimeMillis() + timeUnit.toMillis(waitTime));
        for (ShutdownHook hook : this.workers) {
            hook.shutdown();
        }

        while (true) {
            boolean allStop = true;
            for (ShutdownHook hook : this.workers) {
                if (hook.isRunning()) {
                    allStop = false;
                    break;
                }
            }

            if (allStop) {
                break;
            } else if (timeout > 0 && System.currentTimeMillis() > timeout) {
                throw new TimeoutException();
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }

        this.workers.clear();
        this.eventQueue.clear();
        this.monitor.reset();
    }
}
