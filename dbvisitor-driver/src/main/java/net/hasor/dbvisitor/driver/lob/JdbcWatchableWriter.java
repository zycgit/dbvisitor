/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver.lob;
import java.io.CharArrayWriter;

/**
 * A java.io.Writer used to write unicode data into Blobs and Clobs
 */
public class JdbcWatchableWriter extends CharArrayWriter {
    private JdbcWriterWatcher watcher;

    /** @see java.io.Writer#close() */
    @Override
    public void close() {
        super.close();

        // Send data to watcher
        if (this.watcher != null) {
            this.watcher.writerClosed(this);
        }
    }

    /** @param watcher {@link JdbcWriterWatcher} */
    public void setWatcher(JdbcWriterWatcher watcher) {
        this.watcher = watcher;
    }
}
