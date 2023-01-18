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
import net.hasor.dbvisitor.faker.OpsType;
import net.hasor.dbvisitor.faker.generator.BoundQuery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 状态监听
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerMonitor {
    private static final AtomicLong               ZERO              = new AtomicLong(0);
    private final static Logger                   logger            = Logger.getLogger(WriteWorker.class);
    private final        Map<OpsType, AtomicLong> succeedCounter    = new ConcurrentHashMap<>();
    private final        Map<OpsType, AtomicLong> failedCounter     = new ConcurrentHashMap<>();
    private final        AtomicLong               affectRowsCounter = new AtomicLong(0);
    private              long                     startMonitorTime  = 0;
    private final        Map<String, AtomicLong>  writerTotal       = new ConcurrentHashMap<>();
    private final        Map<String, Long>        workerLastThrow   = new ConcurrentHashMap<>();
    //
    private final        List<Thread>             producerThreads   = new CopyOnWriteArrayList<>();
    private final        List<Thread>             writerThreads     = new CopyOnWriteArrayList<>();
    private final        EventQueue               eventQueue;

    FakerMonitor(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    /** 获取成功执行的 insert 数 */
    public long getSucceedInsert() {
        return this.succeedCounter.getOrDefault(OpsType.Insert, ZERO).get();
    }

    /** 获取成功执行的 update 数 */
    public long getSucceedUpdate() {
        return this.succeedCounter.getOrDefault(OpsType.Update, ZERO).get();
    }

    /** 获取成功执行的 delete 数 */
    public long getSucceedDelete() {
        return this.succeedCounter.getOrDefault(OpsType.Delete, ZERO).get();
    }

    /** 获取执行失败的 insert 数 */
    public long getFailedInsert() {
        return this.failedCounter.getOrDefault(OpsType.Insert, ZERO).get();
    }

    /** 获取执行失败的 update 数 */
    public long getFailedUpdate() {
        return this.failedCounter.getOrDefault(OpsType.Update, ZERO).get();
    }

    /** 获取执行失败的 delete 数 */
    public long getFailedDelete() {
        return this.failedCounter.getOrDefault(OpsType.Delete, ZERO).get();
    }

    /** 获取 IUD 成功总数 */
    public long getSucceed() {
        return getSucceedInsert() + getSucceedUpdate() + getSucceedDelete();
    }

    /** 获取 IUD 失败总数 */
    public long getFailed() {
        return getFailedInsert() + getFailedUpdate() + getFailedDelete();
    }

    /** 一个 写入成功事件 */
    void recordMonitor(String writerID, String tranID, BoundQuery event, int affectRows) {
        if (this.startMonitorTime <= 0) {
            this.startMonitorTime = System.currentTimeMillis();
        }
        this.succeedCounter.computeIfAbsent(event.getOpsType(), s -> new AtomicLong()).incrementAndGet();
        this.writerTotal.computeIfAbsent(writerID, s -> new AtomicLong()).incrementAndGet();
        this.affectRowsCounter.getAndAdd(affectRows);
    }

    /** 一个 写入失败事件 */
    void recordFailed(String writerID, String tranID, BoundQuery event, Exception e) {
        this.failedCounter.computeIfAbsent(event.getOpsType(), s -> new AtomicLong()).incrementAndGet();
    }

    public void workThrowable(String writerID, Throwable e) {
        long now = System.currentTimeMillis();
        Long lastTime = this.workerLastThrow.get(writerID);
        if (lastTime != null && (lastTime + 3000) > now) {
            return;
        }

        this.workerLastThrow.put(writerID, now);
        logger.error("work " + writerID + ", throwable:" + e.getMessage(), e);
    }

    /** 启动了一个数据发生器 */
    void producerStart(String workID, Thread workThread) {
        this.producerThreads.add(workThread);
    }

    /** 启动了一个写入器 */
    void writerStart(String writerID, Thread workThread) {
        this.writerThreads.add(workThread);
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

        int queueCapacity = this.eventQueue.getCapacity();
        int queueSize = this.eventQueue.getQueueSize();
        int queueDutyRatio = (int) (((double) queueSize / (double) queueCapacity) * 100);

        int producerRunningCnt = (int) this.producerThreads.stream().map(Thread::getState).filter(state -> state == Thread.State.RUNNABLE).count();
        int writerRunningCnt = (int) this.writerThreads.stream().map(Thread::getState).filter(state -> state == Thread.State.RUNNABLE).count();
        int producerDutyRatio = (int) (((double) producerRunningCnt / (double) this.producerThreads.size()) * 100);
        int writerDutyRatio = (int) (((double) writerRunningCnt / (double) this.writerThreads.size()) * 100);

        return String.format("Succeed[I/U/D] %s/%s/%s, Failed[I/U/D] %s/%s/%s, RPS(s)[per/sum] %s/%s, total/affect %s/%s, load[Q/P/W] %d%%/%d%%/%d%%",//
                getSucceedInsert(), getSucceedUpdate(), getSucceedDelete(),     // Succeed[I/U/D]
                getFailedInsert(), getFailedUpdate(), getFailedDelete(),        // Failed[I/U/D]
                (perWriterAvg / passedTimeSec), (writerTotal / passedTimeSec),  // RPS[perWriter/total]
                writerTotal, affectRowsCounter,                                 // total/affect
                queueDutyRatio, producerDutyRatio, writerDutyRatio              // dutyRatio[Q/P/W] -> queue/producer/writer
        );
    }

    /** 重制状态 */
    public void reset() {
        this.succeedCounter.clear();
        this.failedCounter.clear();
        this.affectRowsCounter.set(0);
        this.startMonitorTime = 0;
        this.writerTotal.clear();
        this.workerLastThrow.clear();
        this.producerThreads.clear();
        this.writerThreads.clear();
    }
}
