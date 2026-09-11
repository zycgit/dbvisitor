/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver.lob;
/**
 * Objects that want to be notified of lifecycle events on a JdbcWatchableWriter should implement this interface,
 * and register themselves with setWatcher() on the JdbcWatchableWriter instance.
 */
public interface JdbcWriterWatcher {
    /**
     * Called when the Writer being watched has .close() called
     * @param out JdbcWatchableWriter instance
     */
    void writerClosed(JdbcWatchableWriter out);
}
