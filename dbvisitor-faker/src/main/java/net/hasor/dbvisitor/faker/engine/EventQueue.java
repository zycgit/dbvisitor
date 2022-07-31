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
import net.hasor.dbvisitor.faker.generator.BoundQuery;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 生产者和消费者之间的 数据传输通道
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class EventQueue {
    private final String                          queueID;
    private final int                             capacity;
    private final BlockingQueue<List<BoundQuery>> dataSet;

    public EventQueue(int capacity) {
        this.queueID = UUID.randomUUID().toString().replace("-", "");
        this.capacity = capacity;
        this.dataSet = new LinkedBlockingQueue<>(capacity);
    }

    public String getQueueID() {
        return queueID;
    }

    public List<BoundQuery> tryPoll() {
        return this.dataSet.poll();
    }

    public boolean tryOffer(List<BoundQuery> queries) {
        return this.dataSet.offer(queries);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getQueueSize() {
        return this.dataSet.size();
    }
}
