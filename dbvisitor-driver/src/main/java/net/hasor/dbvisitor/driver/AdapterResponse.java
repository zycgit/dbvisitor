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
import net.hasor.cobble.concurrent.future.BasicFuture;

import java.util.List;

public class AdapterResponse extends BasicFuture<AdapterResponse> {

    public List<JdbcColumn> getColumnList() {
        return null;
    }

    public boolean isResult() {
        return false;
    }

    public AdapterCursor toCursor() {
        return null;
    }

    public long getUpdateCount() {
        return 0;
    }

    public static AdapterResponse ofError(Exception e) {

    }

    public static AdapterResponse ofCursor(AdapterCursor cursor) {

    }

    public static AdapterResponse ofUpdateCount(long updateCount) {

    }
}
