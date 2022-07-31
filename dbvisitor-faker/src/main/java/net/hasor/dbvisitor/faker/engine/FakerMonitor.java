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
import net.hasor.cobble.logging.Logger;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.OpsType;
import net.hasor.dbvisitor.faker.generator.BoundQuery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监听写入状态
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerMonitor {
    private static final AtomicLong               ZERO              = new AtomicLong(0);
    // total
    private final static Logger                   logger            = Logger.getLogger(WriteWorker.class);
    private final        Map<OpsType, AtomicLong> succeedCounter    = new ConcurrentHashMap<>();
    private final        Map<OpsType, AtomicLong> failedCounter     = new ConcurrentHashMap<>();
    private final        AtomicLong               affectRowsCounter = new AtomicLong(0);
    private              QoSBucket                qosBucket         = null;
    private              long                     startMonitorTime  = 0;
    // pre worker
    private final        Map<String, AtomicLong>  writerTotal       = new ConcurrentHashMap<>();
    // pre table
    private final        Map<String, AtomicLong>  tableTotal        = new ConcurrentHashMap<>();

    FakerMonitor(FakerConfig fakerConfig) {
        if (fakerConfig.getWriteQps() > 0) {
            this.qosBucket = new QoSBucket(fakerConfig.getWriteQps());
        }
    }

    public long getSucceedInsert() {
        return this.succeedCounter.getOrDefault(OpsType.Insert, ZERO).get();
    }

    public long getSucceedUpdate() {
        return this.succeedCounter.getOrDefault(OpsType.Update, ZERO).get();
    }

    public long getSucceedDelete() {
        return this.succeedCounter.getOrDefault(OpsType.Delete, ZERO).get();
    }

    public long getFailedInsert() {
        return this.failedCounter.getOrDefault(OpsType.Insert, ZERO).get();
    }

    public long getFailedUpdate() {
        return this.failedCounter.getOrDefault(OpsType.Update, ZERO).get();
    }

    public long getFailedDelete() {
        return this.failedCounter.getOrDefault(OpsType.Delete, ZERO).get();
    }

    boolean ifPresentExit() {
        return false;
    }

    void checkQoS() {
        if (this.qosBucket != null) {
            this.qosBucket.check();
        }
    }

    void recordMonitor(String writerID, String tranID, BoundQuery event, int affectRows) {
        if (this.startMonitorTime <= 0) {
            this.startMonitorTime = System.currentTimeMillis();
        }
        this.succeedCounter.computeIfAbsent(event.getOpsType(), s -> new AtomicLong()).incrementAndGet();
        this.writerTotal.computeIfAbsent(writerID, s -> new AtomicLong()).incrementAndGet();
        this.affectRowsCounter.getAndAdd(affectRows);
    }

    void recordFailed(String writerID, String tranID, BoundQuery event, Exception e) {
        this.failedCounter.computeIfAbsent(event.getOpsType(), s -> new AtomicLong()).incrementAndGet();
    }

    void workExit(String writerID, Throwable e) {
        logger.error(e.getMessage(), e);

    }

    void writerStart(String writerID) {
    }

    void producerStart(String workID) {
    }

    @Override
    public String toString() {
        long succeedTotal = 0;
        for (AtomicLong counter : this.succeedCounter.values()) {
            succeedTotal = succeedTotal + counter.get();
        }
        long failedTotal = 0;
        for (AtomicLong counter : this.failedCounter.values()) {
            failedTotal = failedTotal + counter.get();
        }

        long passedTimeSec = Math.max(1, (System.currentTimeMillis() - this.startMonitorTime) / 1000);
        long writerTotal = succeedTotal + failedTotal;
        long perWriterAvg = this.writerTotal.size() == 0 ? 0 : (writerTotal / this.writerTotal.size());

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(String.format("Succeed[I/U/D] %s/%s/%s, Failed[I/U/D] %s/%s/%s, RPS(s)[perWriter/sum] %s/%s, total/affect %s/%s",//
                getSucceedInsert(), getSucceedUpdate(), getSucceedDelete(),     // Succeed[I/U/D]
                getFailedInsert(), getFailedUpdate(), getFailedDelete(),        // Failed[I/U/D]
                (perWriterAvg / passedTimeSec), (writerTotal / passedTimeSec),  // RPS[perWriter/total]
                writerTotal, affectRowsCounter                                  // total/affect
        ));

        //        String qpsString = qosBucket != null ? String.valueOf(qosBucket.getRate()) : "n";
        //        strBuilder.append(String.format("QPS/QoS = %s/%s ,", affectRowsCounter, qpsString));
        return strBuilder.toString();
    }
}
