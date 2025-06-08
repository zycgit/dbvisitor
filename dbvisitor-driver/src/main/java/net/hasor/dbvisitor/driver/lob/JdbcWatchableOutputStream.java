package net.hasor.dbvisitor.driver.lob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Objects that want to be notified of lifecycle events on a WatchableOutputStream should implement this interface,
 * and register themselves with setWatcher() on the WatchableOutputStream instance.
 */
public class JdbcWatchableOutputStream extends ByteArrayOutputStream implements JdbcWatchableStream {
    private JdbcOutputStreamWatcher watcher;

    /** @see java.io.OutputStream#close() */
    @Override
    public void close() throws IOException {
        super.close();

        if (this.watcher != null) {
            this.watcher.streamClosed(this);
        }
    }

    public void setWatcher(JdbcOutputStreamWatcher watcher) {
        this.watcher = watcher;
    }
}