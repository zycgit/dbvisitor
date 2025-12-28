package net.hasor.dbvisitor.adapter.elastic;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.SQLException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.driver.AdapterReceive;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

class ElasticCommandsForCat extends ElasticCommands {

    public static Future<?> execCat(Future<Object> sync, ElasticCmd elasticCmd, ElasticOperation operation, Object jsonBody, AdapterReceive receive) {
        try {
            Request esRequest = new Request(operation.getMethod().name(), operation.getEndpoint());
            if (jsonBody != null) {
                ObjectMapper mapper = new ObjectMapper();
                esRequest.setJsonEntity(mapper.writeValueAsString(jsonBody));
            }

            Response response = elasticCmd.getClient().performRequest(esRequest);
            try (InputStream content = response.getEntity().getContent()) {
                StringWriter writer = new StringWriter();
                InputStreamReader reader = new InputStreamReader(content);
                char[] buffer = new char[1024];
                int n;
                while (-1 != (n = reader.read(buffer))) {
                    writer.write(buffer, 0, n);
                }
                receive.responseResult(operation.getRequest(), singleResult(operation.getRequest(), COL_JSON_STRING, writer.toString()));
            }
            return completed(sync);
        } catch (Exception e) {
            return failed(sync, new SQLException(e));
        }
    }
}
