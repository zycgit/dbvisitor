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