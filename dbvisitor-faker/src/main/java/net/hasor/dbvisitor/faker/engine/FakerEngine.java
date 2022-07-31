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
import net.hasor.dbvisitor.faker.OpsType;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.FakerGenerator;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

/**
 * 压力引擎
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerEngine {
    private final DataSource              dataSource;
    private final FakerConfig             fakerConfig;
    private       FakerMonitor            monitor;
    //
    private final ThreadFactory           threadFactory;
    private final Map<String, EventQueue> queueMap;
    private final List<ShutdownHook>      workers;

    public FakerEngine(FakerFactory fakerFactory) {
        this(Objects.requireNonNull(fakerFactory.getJdbcTemplate().getDataSource(), "fakerFactory must be created by DataSource."), fakerFactory.getFakerConfig());
    }

    public FakerEngine(DataSource dataSource) {
        this(dataSource, new FakerConfig());
    }

    public FakerEngine(DataSource dataSource, FakerConfig fakerConfig) {
        this.dataSource = dataSource;
        this.fakerConfig = fakerConfig;
        this.monitor = new FakerMonitor(fakerConfig);
        this.queueMap = new ConcurrentHashMap<>();
        this.workers = new CopyOnWriteArrayList<>();

        if (fakerConfig.getThreadFactory() != null) {
            this.threadFactory = fakerConfig.getThreadFactory();
        } else {
            this.threadFactory = Thread::new;
        }
    }

    public FakerMonitor getMonitor() {
        return this.monitor;
    }

    /** 启动数据集发生器 */
    public synchronized void startProducer(FakerGenerator generator, int threadCount) {
        this.startProducer(generator, threadCount, null);
    }

    /** 启动数据集发生器 */
    public synchronized void startProducer(FakerGenerator generator, int threadCount, List<OpsType> specialOps) {
        String producerID = generator.getGeneratorID();
        if (this.queueMap.containsKey(producerID)) {
            throw new IllegalStateException("generator '" + producerID + "' already exists.");
        }

        EventQueue eventQueue = new EventQueue(this.fakerConfig.getQueueCapacity());
        this.queueMap.put(producerID, eventQueue);
        this.monitor.monitorQueue(eventQueue);

        for (int i = 0; i < threadCount; i++) {
            String workName = String.format("generator[%s-%s]", producerID, i);
            ShutdownHook worker = new ProducerWorker(workName, specialOps, generator, this.monitor, eventQueue);
            this.workers.add(worker);
            this.threadFactory.newThread(worker).start();
        }
    }

    /** 启动数据写入器 */
    public synchronized void startWriter(FakerGenerator producer, int threadCount) {
        String producerID = producer.getGeneratorID();
        if (!this.queueMap.containsKey(producerID)) {
            throw new IllegalStateException("generator '" + producerID + "' is not exists.");
        }

        EventQueue eventQueue = this.queueMap.get(producerID);
        for (int i = 0; i < threadCount; i++) {
            String workName = String.format("writer[%s-%s]", producerID, i);
            ShutdownHook worker = new WriteWorker(workName, this.dataSource, this.fakerConfig, this.monitor, eventQueue);
            this.workers.add(worker);
            this.threadFactory.newThread(worker).start();
        }
    }

    /** 停止引擎，并清空监控状态 */
    public void shutdown() {
        this.monitor.exitSignal();
        for (ShutdownHook hook : this.workers) {
            hook.shutdown();
        }
        this.workers.clear();
        this.queueMap.clear();
        this.monitor = new FakerMonitor(this.fakerConfig);
    }
}
