package net.hasor.dbvisitor.adapter.elastic;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.adapter.elastic.parser.*;
import net.hasor.dbvisitor.driver.*;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

public class ElasticConn extends AdapterConnection {
    private static final Logger       logger    = LoggerFactory.getLogger(ElasticConn.class);
    private final        Connection   owner;
    private final        ElasticCmd   elasticCmd;
    private final        boolean      preRead;
    private final        long         preReadThreshold;
    private final        long         preReadMaxFileSize;
    private final        java.io.File preReadCacheDir;
    private volatile     boolean      cancelled = false;
    private final        ObjectMapper json      = new ObjectMapper();

    public ElasticConn(Connection owner, ElasticCmd elasticCmd, String jdbcUrl, Map<String, String> prop) {
        super(jdbcUrl, prop.get(ElasticKeys.USERNAME));
        this.owner = owner;
        this.elasticCmd = elasticCmd;

        this.preRead = "true".equalsIgnoreCase(prop.getOrDefault(ElasticKeys.PREREAD_ENABLED, "true"));
        this.preReadThreshold = parseSize(prop.get(ElasticKeys.PREREAD_THRESHOLD), 5 * 1024 * 1024); // Default 5MB
        this.preReadMaxFileSize = parseSize(prop.get(ElasticKeys.PREREAD_MAX_FILE_SIZE), 20 * 1024 * 1024); // Default 20MB
        String cacheDirStr = prop.get(ElasticKeys.PREREAD_CACHE_DIR);
        this.preReadCacheDir = StringUtils.isBlank(cacheDirStr) ? new java.io.File(System.getProperty("java.io.tmpdir")) : new java.io.File(cacheDirStr);
    }

    @Override
    public int getDefaultGeneratedKeys() {
        return Statement.RETURN_GENERATED_KEYS;
    }

    private long parseSize(String sizeStr, long defaultValue) {
        if (StringUtils.isBlank(sizeStr)) {
            return defaultValue;
        }
        sizeStr = sizeStr.toUpperCase().trim();
        long multiplier = 1;
        if (StringUtils.endsWithIgnoreCase(sizeStr, "KB")) {
            multiplier = 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (StringUtils.endsWithIgnoreCase(sizeStr, "MB")) {
            multiplier = 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (StringUtils.endsWithIgnoreCase(sizeStr, "GB")) {
            multiplier = 1024 * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (StringUtils.endsWithIgnoreCase(sizeStr, "B")) {
            sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
        } else {
            multiplier = 1024 * 1024;
        }
        try {
            return Long.parseLong(sizeStr.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean isPreRead() {
        return this.preRead;
    }

    public long getPreReadThreshold() {
        return this.preReadThreshold;
    }

    public long getPreReadMaxFileSize() {
        return this.preReadMaxFileSize;
    }

    public java.io.File getPreReadCacheDir() {
        return this.preReadCacheDir;
    }

    protected Connection getOwner() {
        return this.owner;
    }

    public void initConnection() {
        this.getFeatures().addFeature(AdapterFeatureKey.ReturnGeneratedKeys, true);

        AdapterInfo info = this.getInfo();
        info.getDriverVersion().setName(ElasticKeys.DEFAULT_CLIENT_NAME);
        info.getDbVersion().setName("Elastic");
        info.getDbVersion().setVersion("Unknown");
        info.getDbVersion().setMajorVersion(0);
        info.getDbVersion().setMinorVersion(0);

        try {
            Response response = elasticCmd.getClient().performRequest(new Request("GET", "/"));
            try (InputStream content = response.getEntity().getContent()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(content);
                JsonNode versionNode = rootNode.path("version");

                if (!versionNode.isMissingNode()) {
                    String number = versionNode.path("number").asText();
                    info.getDbVersion().setVersion(number);

                    if (StringUtils.isNotBlank(number)) {
                        String[] versionParts = number.split("\\.");
                        if (versionParts.length >= 1) {
                            info.getDbVersion().setMajorVersion(Integer.parseInt(versionParts[0]));
                        }
                        if (versionParts.length >= 2) {
                            info.getDbVersion().setMinorVersion(Integer.parseInt(versionParts[1]));
                        }
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("initConnection failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getCatalog() {
        return "";
    }

    @Override
    public void setCatalog(String catalog) {
    }

    @Override
    public String getSchema() {
        return "";
    }

    @Override
    public void setSchema(String catalog) {

    }

    @Override
    public AdapterRequest newRequest(String sql) {
        ElasticRequest request = new ElasticRequest(sql, this.preRead);
        request.setJson(this.json);
        return request;
    }

    @Override
    protected <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface == ElasticConn.class) {
            return (T) this;
        } else if (iface == ElasticCmd.class) {
            return (T) this.elasticCmd;
        } else if (iface == RestClient.class) {
            return (T) this.elasticCmd.getClient();
        } else {
            return super.unwrap(iface);
        }
    }

    protected ElasticParser.EsCommandsContext parserRequest(AdapterRequest request) throws SQLException {
        if (StringUtils.isBlank(((ElasticRequest) request).getCommandBody())) {
            throw new SQLException("query command is empty.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }
        try {
            ElasticLexer lexer = new ElasticLexer(CharStreams.fromString(((ElasticRequest) request).getCommandBody()));
            lexer.removeErrorListeners();
            lexer.addErrorListener(ThrowingListener.INSTANCE);

            ElasticParser parser = new ElasticParser(new BufferedTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(ThrowingListener.INSTANCE);
            return parser.esCommands();
        } catch (QueryParseException e) {
            String errorMsg = "command '" + ((ElasticRequest) request).getCommandBody() + "' parserFailed.";
            throw new SQLException(errorMsg, JdbcErrorCode.SQL_STATE_SYNTAX_ERROR);
        }
    }

    //
    //
    //

    @Override
    public synchronized void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("doRequest: " + ((ElasticRequest) request).getCommandBody());
        }
        this.cancelled = false;
        ElasticParser.EsCommandsContext root = parserRequest(request);
        ElasticArgVisitor argVisitor = new ElasticArgVisitor();
        root.accept(argVisitor);
        int argCount = argVisitor.getArgCount();
        List<ElasticParser.HintCommandContext> commandList = argVisitor.getCommandList();

        if (commandList.isEmpty()) {
            throw new SQLException("query command is empty.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }
        if (argCount > 0) {
            if (argCount != request.getArgMap().size()) {
                throw new SQLException("param size not match.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        int startArgIdx = 0;
        for (ElasticParser.HintCommandContext esCmd : commandList) {
            if (this.cancelled) {
                throw new SQLException("Operation cancelled.", JdbcErrorCode.SQL_STATE_IS_CANCELLED);
            }

            if (esCmd.esCmd() == null) {
                continue;
            }

            Future<Object> sync = new BasicFuture<>();
            if (argCount > 0) {
                ElasticArgVisitor subVisitor = new ElasticArgVisitor();
                esCmd.accept(subVisitor);
                ElasticDistributeCall.execElasticCmd(sync, this.elasticCmd, esCmd, request, receive, startArgIdx, this);
                startArgIdx += subVisitor.getArgCount();
            } else {
                ElasticDistributeCall.execElasticCmd(sync, this.elasticCmd, esCmd, request, receive, startArgIdx, this);
            }

            sync.await();

            if (sync.isDone() && sync.getCause() != null) {
                receive.responseFailed(request, sync.getCause());
                return;
            }
        }

        receive.responseFinish(request);
    }

    @Override
    public void cancelRequest() {
        this.cancelled = true;
    }

    @Override
    protected void doClose() throws IOException {
        this.cancelRequest();
        this.elasticCmd.close();
    }
}
