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
import net.hasor.cobble.RandomUtils;
import net.hasor.dbvisitor.faker.OpsType;
import net.hasor.dbvisitor.faker.generator.BoundQuery;
import net.hasor.dbvisitor.faker.generator.FakerGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据发生器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
class ProducerWorker implements ShutdownHook, Runnable {
    private final    String         threadName;
    private final    List<OpsType>  specialOps;
    private final    FakerGenerator producer;
    private final    FakerMonitor   monitor;
    private final    EventQueue     eventQueue;
    //
    private final    AtomicBoolean  running;
    private volatile Thread         workThread;

    ProducerWorker(String threadName, List<OpsType> specialOps, FakerGenerator producer, FakerMonitor monitor, EventQueue eventQueue) {
        this.threadName = threadName;
        this.specialOps = specialOps == null ? new ArrayList<>() : specialOps;
        this.producer = producer;
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
        this.monitor.producerStart(this.threadName);

        while (this.testContinue()) {
            try {
                List<BoundQuery> queries = null;
                if (this.specialOps.isEmpty()) {
                    queries = this.producer.generator();
                } else {
                    OpsType randomOps = this.specialOps.get(RandomUtils.nextInt(0, this.specialOps.size()));
                    queries = this.producer.generator(randomOps);
                }

                while (this.testContinue()) {
                    if (!this.eventQueue.tryOffer(queries)) {
                        Thread.sleep(100); // prevent empty loop
                    } else {
                        break;
                    }
                }
            } catch (Throwable e) {
                this.running.set(false);
                this.monitor.workExit(this.threadName, e);
                return;
            }
        }

        this.monitor.workExit(this.threadName, null);
    }

}