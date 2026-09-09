package net.hasor.dbvisitor.adapter.milvus.commands;

import java.util.*;
import java.util.function.Function;
import io.milvus.shaded.io.grpc.CallOptions;
import io.milvus.shaded.io.grpc.Channel;
import io.milvus.shaded.io.grpc.ClientCall;
import io.milvus.shaded.io.grpc.Metadata;
import io.milvus.shaded.io.grpc.MethodDescriptor;
import io.milvus.grpc.*;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.RpcStubWrapper;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.v2.common.IndexParam.MetricType;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import io.milvus.v2.service.vector.request.data.FloatVec;
import org.junit.Test;
import static org.junit.Assert.*;

/** Run the actual SDK iterators over an in-memory gRPC transport, including subsequent pages. */
public class MilvusIteratorBindingTest {
    private static final String VALUE = "a\" || id > 0 \\ 中文";

    @Test
    public void queryIteratorKeepsTemplateValuesOnEveryWireRequest() {
        List<QueryRequest> requests = new ArrayList<>();
        RpcStubWrapper stub = transport(request -> {
            requests.add((QueryRequest) request);
            return QueryResults.newBuilder().setStatus(Status.newBuilder()).setSessionTs(100)
                    .addFieldsData(FieldData.newBuilder().setFieldName("id").setType(DataType.Int64)
                            .setScalars(ScalarField.newBuilder().setLongData(LongArray.newBuilder().addData(requests.size())))).build();
        });
        QueryIteratorReq request = QueryIteratorReq.builder().collectionName("t").expr("title == {arg1}")
                .filterTemplateValues(Map.of("arg1", VALUE)).outputFields(List.of("id")).batchSize(1).limit(2L).build();
        CreateCollectionReq.FieldSchema primary = CreateCollectionReq.FieldSchema.builder().name("id")
                .dataType(io.milvus.v2.common.DataType.Int64).isPrimaryKey(true).build();
        QueryIterator iterator = new QueryIterator(request, stub, primary);
        try {
            assertEquals(1, iterator.next().size());
            assertEquals(1, iterator.next().size());
        } finally {
            iterator.close();
        }
        assertTrue(requests.size() >= 3); // Initial snapshot probe plus data pages.
        for (QueryRequest query : requests) {
            assertTrue(query.getExpr().contains("title == {arg1}"));
            assertFalse(query.getExpr().contains(VALUE));
            assertEquals(VALUE, query.getExprTemplateValuesOrThrow("arg1").getStringVal());
        }
    }

    @Test
    public void searchIteratorV2KeepsTemplatesAndRangeOptionsAcrossPages() {
        List<SearchRequest> requests = new ArrayList<>();
        RpcStubWrapper stub = transport(request -> {
            if (request instanceof DescribeCollectionRequest) {
                return DescribeCollectionResponse.newBuilder().setStatus(Status.newBuilder()).setCollectionID(1)
                        .setSchema(CollectionSchema.newBuilder().setName("t")
                                .addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true))).build();
            }
            requests.add((SearchRequest) request);
            return SearchResults.newBuilder().setStatus(Status.newBuilder()).setSessionTs(100)
                    .setResults(SearchResultData.newBuilder().setNumQueries(1).setTopK(1).addTopks(1).setPrimaryFieldName("id")
                            .setIds(IDs.newBuilder().setIntId(LongArray.newBuilder().addData(requests.size()))).addScores(0.1F)
                            .setSearchIteratorV2Results(SearchIteratorV2Results.newBuilder().setToken("cursor").setLastBound(0.1F))).build();
        });
        SearchIteratorReqV2 request = SearchIteratorReqV2.builder().collectionName("t").filter("title == {arg1}")
                .filterTemplateValues(Map.of("arg1", VALUE)).vectorFieldName("v").vectors(List.of(new FloatVec(new float[] { 1, 2 })))
                .metricType(MetricType.L2).searchParams(new LinkedHashMap<>(Map.of(MilvusCommandKeys.RADIUS, 3D)))
                .outputFields(List.of("id")).batchSize(1).limit(2L).build();
        SearchIteratorV2 iterator = new SearchIteratorV2(request, stub);
        try {
            assertEquals(1, iterator.next().size());
            assertEquals(1, iterator.next().size());
            assertTrue(iterator.next().isEmpty());
        } finally {
            iterator.close();
        }
        assertEquals(3, requests.size()); // Compatibility probe and two pages, no extra fetch after LIMIT.
        for (SearchRequest query : requests) {
            assertEquals("title == {arg1}", query.getDsl());
            assertEquals(VALUE, query.getExprTemplateValuesOrThrow("arg1").getStringVal());
            assertTrue(query.getSearchParamsList().stream().anyMatch(param -> param.getValue().contains("\"radius\":3.0")));
        }
    }

    private static RpcStubWrapper transport(Function<Object, Object> respond) {
        Channel channel = new Channel() {
            @Override
            public String authority() {
                return "in-memory";
            }

            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> newCall(MethodDescriptor<ReqT, RespT> method, CallOptions options) {
                return new ClientCall<ReqT, RespT>() {
                    private Listener<RespT> listener;
                    private ReqT request;

                    @Override public void start(Listener<RespT> responseListener, Metadata headers) { listener = responseListener; }
                    @Override public void request(int count) { }
                    @Override public void sendMessage(ReqT message) { request = message; }
                    @Override public void cancel(String message, Throwable cause) { }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void halfClose() {
                        listener.onMessage((RespT) respond.apply(request));
                        listener.onClose(io.milvus.shaded.io.grpc.Status.OK, new Metadata());
                    }
                };
            }
        };
        return new RpcStubWrapper(MilvusServiceGrpc.newBlockingStub(channel), 0);
    }
}
