/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import io.milvus.grpc.ErrorCode;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.Status;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.assertEquals;

public class MilvusExpressionEscapingTest extends AbstractJdbcTest {
    private final List<QueryIteratorReq> queryParams  = new ArrayList<>();
    private final List<DeleteReq>        deleteParams = new ArrayList<>();

    private Connection getConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        prop.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://xxxxxx:19530", prop);
    }

    private void installInterceptor() {
        MilvusCommandInterceptor.resetInterceptor();
        this.queryParams.clear();
        this.deleteParams.clear();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("describeCollection".equals(method.getName())) {
                return v2Response(method.getName(), io.milvus.grpc.DescribeCollectionResponse.newBuilder()
                        .setSchema(io.milvus.grpc.CollectionSchema.newBuilder().addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_id").setDataType(io.milvus.grpc.DataType.Int64).setIsPrimaryKey(true)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("word_count").setDataType(io.milvus.grpc.DataType.Int32)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("title").setDataType(io.milvus.grpc.DataType.VarChar)).addFields(io.milvus.grpc.FieldSchema.newBuilder().setName("book_intro").setDataType(io.milvus.grpc.DataType.FloatVector))).build());
            }
            if ("queryIterator".equals(method.getName())) {
                this.queryParams.add((QueryIteratorReq) args[0]);
                return v2Response(method.getName(), QueryResults.newBuilder()//
                        .setStatus(Status.newBuilder().setErrorCode(ErrorCode.Success))//
                        .build());
            }
            if ("delete".equals(method.getName())) {
                this.deleteParams.add((DeleteReq) args[0]);
                return v2Response(method.getName(), MutationResult.newBuilder().setDeleteCnt(1).build());
            }
            if ("close".equals(method.getName())) {
                return null;
            }
            return null;
        });
    }

    @After
    public void cleanupInterceptor() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    @Test
    public void testComparisonParameterEscapesExpressionControlCharacters() throws Exception {
        this.installInterceptor();
        String value = "x\" || book_id > 0 || title == \"y\\z\n";

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "SELECT * FROM book_vectors WHERE title = ?")) {
            ps.setString(1, value);
            ps.executeQuery();
        }

        assertEquals(1, this.queryParams.size());
        assertEquals("title == {arg1}", this.queryParams.get(0).getExpr());
        assertEquals(value, this.queryParams.get(0).getFilterTemplateValues().get("arg1"));
    }

    @Test
    public void testLikeAndInParametersUseTheSameEscapingRules() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement like = conn.prepareStatement(//
                "SELECT * FROM book_vectors WHERE title LIKE ?")) {
            like.setString(1, "a\"%\\tail");
            like.executeQuery();
        }
        try (Connection conn = this.getConnection(); PreparedStatement in = conn.prepareStatement(//
                "SELECT * FROM book_vectors WHERE title IN ?")) {
            in.setObject(1, Arrays.asList("safe", "x\"] || book_id > 0 || title in [\"y"));
            in.executeQuery();
        }

        assertEquals(2, this.queryParams.size());
        assertEquals("title like {arg1}", this.queryParams.get(0).getExpr());
        assertEquals("a\"%\\tail", this.queryParams.get(0).getFilterTemplateValues().get("arg1"));
        assertEquals("title in {arg1}", this.queryParams.get(1).getExpr());
        assertEquals(Arrays.asList("safe", "x\"] || book_id > 0 || title in [\"y"), this.queryParams.get(1).getFilterTemplateValues().get("arg1"));
    }

    @Test
    public void testDeleteParameterCannotChangeFilterExpression() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "DELETE FROM book_vectors WHERE title = ?")) {
            ps.setString(1, "x\" or book_id > 0 or title == \"y");
            ps.executeUpdate();
        }

        assertEquals(1, this.deleteParams.size());
        assertEquals("title == {arg1}", this.deleteParams.get(0).getFilter());
        assertEquals("x\" or book_id > 0 or title == \"y", this.deleteParams.get(0).getFilterTemplateValues().get("arg1"));
    }
}
