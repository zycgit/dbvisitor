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
package net.hasor.dbvisitor.driver;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class AdapterResultCursor implements AdapterCursor {
    private final List<JdbcColumn>           columns;
    private final Queue<Map<String, Object>> inputQueue;
    private final int                        batchSize;

    public AdapterResultCursor(List<JdbcColumn> columns, Queue<Map<String, Object>> input, int batchSize) {
        this.columns = columns;
        this.inputQueue = inputQueue;
        this.batchSize = batchSize;
    }

    @Override
    public List<JdbcColumn> columns() {
        return this.columns;
    }

    @Override
    public boolean next() {
        return this.inputStream.hasNextRow();
    }

    @Override
    public int batchSize() {
        return this.batchSize;
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    @Override
    public Object column(int column) throws IOException {
        this.inputStream.discardReadRow();
        if (this.inputStream.nextRow()) {
            for (int i = 0; i < this.inputStream.getDataCount(); i++) {
                ResultSetInputStream.DataHeader header = this.inputStream.nextDataHeader();
                if (i + 1 == column) {
                    return this.inputStream.readColumn(i);
                }
            }
        }
        return null;
    }

    @Override
    public List<String> warnings() {
        return Collections.emptyList();
    }

    @Override
    public void clearWarnings() {

    }
}