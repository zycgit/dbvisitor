/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.extractor;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;

/**
 * 使用 {@link RowCallbackHandler} 类型循环处理每一行记录的适配器。
 * @author 赵永春 (zyc@hasor.net)
 */
public class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor<Void> {
    private final RowCallbackHandler rch;

    public RowCallbackHandlerResultSetExtractor(final RowCallbackHandler rch) {
        this.rch = rch;
    }

    @Override
    public Void extractData(final ResultSet rs) throws SQLException {
        int rowNum = 0;
        while (rs.next()) {
            this.rch.processRow(rs, rowNum++);
        }
        return null;
    }
}
