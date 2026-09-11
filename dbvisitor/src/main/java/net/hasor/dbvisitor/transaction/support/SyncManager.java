/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.transaction.support;
import javax.sql.DataSource;
import net.hasor.dbvisitor.transaction.DataSourceUtils;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-30
 */
class SyncManager extends DataSourceUtils {
    public static void setSync(TransactionObject tranConn) {
        unsafeResetHolder(tranConn.getDataSource(), tranConn.getHolder());
    }

    public static void clearSync(DataSource dataSource) {
        unsafeClearHolder(dataSource);
    }
}
