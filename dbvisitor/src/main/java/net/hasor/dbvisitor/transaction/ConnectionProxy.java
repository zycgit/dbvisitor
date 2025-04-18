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
package net.hasor.dbvisitor.transaction;
import java.io.Closeable;
import java.sql.Connection;

/**
 * Connection 连接代理。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-3
 */
public interface ConnectionProxy extends Connection, Closeable {
    /**
     * Return the target Connection of this proxy. <p>This will typically be the native driver Connection or a wrapper from a connection pool.
     * @return the underlying Connection (never <code>null</code>)
     */
    Connection getTargetConnection();
}