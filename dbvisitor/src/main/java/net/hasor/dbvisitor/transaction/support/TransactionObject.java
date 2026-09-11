/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.transaction.support;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.hasor.dbvisitor.transaction.ConnectionHolder;
import net.hasor.dbvisitor.transaction.Isolation;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2014-1-18
 */
public class TransactionObject {
    private final ConnectionHolder holder;
    private final DataSource       dataSource;
    private final Isolation        recoverIsolation; //创建事务对象时的隔离级别，当事物结束之后用以恢复隔离级别
    private       boolean          recoverMark = false;

    public TransactionObject(final ConnectionHolder holder, final Isolation recoverIsolation, final DataSource dataSource) {
        this.holder = holder;
        this.dataSource = dataSource;
        this.recoverIsolation = recoverIsolation;
    }

    public Isolation getRecoverIsolation() {
        return this.recoverIsolation;
    }

    public ConnectionHolder getHolder() {
        return this.holder;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void rollback() throws SQLException {
        if (this.holder.hasTransaction()) {
            this.holder.getConnection().rollback();//在AutoCommit情况下不执行事务操作（MYSQL强制在auto下执行该方法会引发异常）。
        }
    }

    public void commit() throws SQLException {
        if (this.holder.hasTransaction()) {
            this.holder.getConnection().commit();//在AutoCommit情况下不执行事务操作（MYSQL强制在auto下执行该方法会引发异常）。
        }
    }

    public boolean hasTransaction() throws SQLException {
        return this.holder.hasTransaction();
    }

    public void beginTransaction() throws SQLException {
        if (!this.holder.hasTransaction()) {
            this.recoverMark = true;
        }
        this.holder.setTransaction();
    }

    public void stopTransaction() throws SQLException {
        if (!this.recoverMark) {
            return;
        }
        this.recoverMark = false;
        this.holder.cancelTransaction();
    }
}
