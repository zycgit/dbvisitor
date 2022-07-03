/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.dbvisitor.dal.execute;
import java.util.Map;

/**
 * 调用存储过程的返回值
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class CallableResult {
    private final Map<String, Object> resultOut;
    private final Object              resultSet;

    public CallableResult(Map<String, Object> resultOut, Object resultSet) {
        this.resultOut = resultOut;
        this.resultSet = resultSet;
    }

    public Map<String, Object> getResultOut() {
        return this.resultOut;
    }

    public Object getResultSet() {
        return this.resultSet;
    }
}
