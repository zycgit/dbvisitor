/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
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
