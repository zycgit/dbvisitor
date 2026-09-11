/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 批量更新时动态参数设置接口。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-9
 */
public interface BatchPreparedStatementSetter {
    /**
     * Set parameter values on the given PreparedStatement.
     * @param ps the PreparedStatement to invoke setter methods on
     * @param i index of the statement we're issuing in the batch, starting from 0
     * @throws SQLException if a SQLException is encountered (i.e. there is no need to catch SQLException)
     */
    void setValues(PreparedStatement ps, int i) throws SQLException;

    /**
     * Return the size of the batch.
     * @return the number of statements in the batch
     */
    int getBatchSize();

    /** 测试批处理是否继续，返回 true 表示处理。false 表示在批处理中放弃这个条目。 */
    default boolean isBatchExhausted(int i) {
        return false;
    }
}
