package net.hasor.dbvisitor.adapter.elastic.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.Before;
import org.powermock.api.mockito.PowerMockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.hasor.dbvisitor.adapter.elastic.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.elastic.ElasticCommandInterceptor;

/** Runs the real JDBC and command pipeline; only the SDK's HTTP boundary is mocked. */
public abstract class AbstractElasticCommandTest extends AbstractJdbcTest {
    protected final ObjectMapper  json      = new ObjectMapper();
    protected final List<Request> requests  = new ArrayList<>();
    private final Deque<Object>   responses = new ArrayDeque<>();

    @Before
    public void installCommandInterceptor() {
        ElasticCommandInterceptor.resetInterceptor();
        ElasticCommandInterceptor.addInterceptor(RestClient.class, (proxy, method, args) -> {
            Request request = (Request) args[0];
            if ("GET".equals(request.getMethod()) && "/".equals(request.getEndpoint())) {
                return response(200, "{\"version\":{\"number\":\"7.17.10\"}}");
            }
            requests.add(request);
            assertFalse("Unexpected SDK request: " + request, responses.isEmpty());
            Object next = responses.removeFirst();
            if (next instanceof IOException) {
                throw (IOException) next;
            }
            return next;
        });
    }

    @After
    public void verifyResponsesConsumedAndReset() {
        try {
            assertTrue("Expected SDK requests were not executed", responses.isEmpty());
        } finally {
            ElasticCommandInterceptor.resetInterceptor();
        }
    }

    protected void respondWith(String body) {
        respondWith(200, body);
    }

    protected void respondWith(int status, String body) {
        responses.addLast(response(status, body));
    }

    protected void failWith(IOException error) {
        responses.addLast(error);
    }

    protected Response response(int status, String body) {
        Response response = PowerMockito.mock(Response.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, status, "Test response"));
        when(response.getRequestLine()).thenReturn(new BasicRequestLine("GET", "/books", HttpVersion.HTTP_1_1));
        when(response.getHost()).thenReturn(new HttpHost("localhost", 9200));
        when(response.getEntity()).thenReturn(new StringEntity(body, ContentType.APPLICATION_JSON));
        return response;
    }

    protected JsonNode requestBody(int index) throws IOException {
        return json.readTree(EntityUtils.toString(requests.get(index).getEntity(), StandardCharsets.UTF_8));
    }

    protected void assertRequest(int index, String method, String endpoint, String body) throws IOException {
        Request request = requests.get(index);
        assertEquals(method, request.getMethod());
        assertEquals(endpoint, request.getEndpoint());
        if (body == null) {
            assertNull(request.getEntity());
        } else {
            assertEquals(json.readTree(body), requestBody(index));
        }
    }
}
