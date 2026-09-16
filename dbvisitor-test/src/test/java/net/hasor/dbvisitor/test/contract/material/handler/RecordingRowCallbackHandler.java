/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import static org.junit.Assert.assertEquals;

/** Captures rows on the invoking thread so callback queries can assert their output. */
public class RecordingRowCallbackHandler implements RowCallbackHandler {
    private static final ThreadLocal<List<Integer>> IDS = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public void processRow(ResultSet result, int rowNum) throws SQLException {
        ResultHandlerProbe.record(result);
        List<Integer> ids = IDS.get();
        assertEquals(ids.size(), rowNum);
        ids.add(readId(result));
    }

    protected int readId(ResultSet result) throws SQLException {
        return result.getInt("id");
    }

    public static List<Integer> ids() {
        return List.copyOf(IDS.get());
    }

    public static void clear() {
        IDS.remove();
    }
}
