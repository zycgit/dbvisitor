/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.*;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.Test;
import static org.junit.Assert.*;

/** Native ES 7 dense_vector boundary; this is not another shared N×N capability. */
public class Elastic7VectorNullBoundaryTest {
    @Test
    public void denseVector_shouldAllowMissingFieldButRejectExplicitNull() throws Exception {
        OneApiDataSourceManager.assumeCurrentDataSource("es7");
        String index = "nxn_vector_null_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = OneApiDataSourceManager.getConnection("es7"); Statement statement = connection.createStatement()) {
            statement.execute("PUT /" + index + """
                     {"mappings":{"properties":{"id":{"type":"integer"},
                     "embedding":{"type":"dense_vector","dims":2}}}}
                    """);
            try {
                assertEquals(1, statement.executeUpdate("POST /" + index + "/_doc/missing {\"id\":1}"));
                assertEquals(1, statement.executeUpdate("POST /" + index + "/_doc/populated {\"id\":2,\"embedding\":[0.25,0.5]}"));
                assertMissingAndPopulatedRows(statement, index);

                SQLException insertError = assertThrows(SQLException.class, () -> statement.executeUpdate("POST /" + index + "/_doc/rejected {\"id\":3,\"embedding\":null}"));
                assertServerRejectedNull(insertError);

                // Keep the explicit null parameter: silently removing the field would change the user's command.
                String update = "POST /" + index + """
                        /_update_by_query {"query":{"term":{"id":2}},
                        "script":{"source":"ctx._source.putAll(params.data)","lang":"painless",
                        "params":{"data":{"embedding":?}}}}
                        """;
                try (PreparedStatement prepared = connection.prepareStatement(update)) {
                    prepared.setNull(1, Types.OTHER);
                    assertServerRejectedNull(assertThrows(SQLException.class, prepared::executeUpdate));
                }

                // Neither the rejected insert nor update may erase or replace either existing document.
                assertMissingAndPopulatedRows(statement, index);
            } finally {
                statement.execute("DELETE /" + index);
            }
        }
    }

    private void assertMissingAndPopulatedRows(Statement statement, String index) throws Exception {
        try (ResultSet rows = statement.executeQuery("POST /" + index + "/_search {\"query\":{\"match_all\":{}},\"sort\":[{\"id\":\"asc\"}]}")) {
            assertTrue(rows.next());
            assertEquals(1, rows.getInt("id"));
            assertNull(rows.getObject("embedding"));
            assertTrue(rows.wasNull());
            assertFalse(new ObjectMapper().readTree(rows.getString("_DOC")).has("embedding"));

            assertTrue(rows.next());
            assertEquals(2, rows.getInt("id"));
            assertEquals(List.of(0.25d, 0.5d), rows.getObject("embedding"));
            assertFalse(rows.next());
        }
    }

    private void assertServerRejectedNull(SQLException error) {
        assertEquals("E400", error.getSQLState());
        assertNotNull(error.getCause());
        String response = error.getCause().getMessage();
        assertTrue(response, response.contains("mapper_parsing_exception"));
        assertTrue(response, response.contains("[VALUE_NUMBER]"));
        assertTrue(response, response.contains("[END_OBJECT]"));
    }
}
