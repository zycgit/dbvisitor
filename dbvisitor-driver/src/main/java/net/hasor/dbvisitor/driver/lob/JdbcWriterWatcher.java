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
