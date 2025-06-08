package net.hasor.dbvisitor.driver.lob;

/**
 * Objects that want to be notified of lifecycle events on a JdbcWatchableOutputStream should implement this interface,
 * and register themselves with setWatcher() on the JdbcWatchableOutputStream instance.
 */
public interface JdbcOutputStreamWatcher {
    /**
     * Called when the OutputStream being watched has .close() called
     * @param out {@link JdbcWatchableStream}
     */
    void streamClosed(JdbcWatchableStream out);
}