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
package net.hasor.dbvisitor.driver.lob;
/**
 * Objects that want to be notified of lifecycle events on a JdbcWatchableOutputStream should implement this interface,
 * and register themselves with setWatcher() on the JdbcWatchableOutputStream instance.
 */
public interface JdbcWatchableStream {

    void setWatcher(JdbcOutputStreamWatcher watcher);

    int size();

    byte[] toByteArray();

    void write(byte[] b, int off, int len);
}