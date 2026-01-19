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
package net.hasor.dbvisitor.adapter.redis;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.adapter.redis.parser.*;
import net.hasor.dbvisitor.driver.*;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

public class JedisConn extends AdapterConnection {
    private static final Logger     logger    = LoggerFactory.getLogger(JedisConn.class);
    private final        Connection owner;
    private final        JedisCmd   jedisCmd;
    private              int        database;
    private final        boolean    uncheckNumKeys;
    private final        char       separatorChar;
    private volatile     boolean    cancelled = false;

    JedisConn(Connection owner, JedisCmd jedisCmd, String jdbcUrl, Map<String, String> prop, int database) throws SQLException {
        super(jdbcUrl, prop.get(JedisKeys.USERNAME));
        this.owner = owner;
        this.jedisCmd = jedisCmd;
        this.database = database;
        this.uncheckNumKeys = Boolean.parseBoolean(prop.getOrDefault(JedisKeys.UNCHECK_NUM_KEYS, "false"));

        switch (prop.getOrDefault(JedisKeys.SEPARATOR_CHAR, "\n").charAt(0)) {
            case '\n':
                this.separatorChar = '\n';
                break;
            case ';':
                this.separatorChar = ';';
                break;
            default:
                throw new SQLException("SeparatorChar must be '\\n' or ';'");
        }
    }

    protected String getSeparatorCharString() {
        switch (this.separatorChar) {
            case '\n':
                return "\\n";
            case ';':
                return ";";
            default:
                return String.valueOf(this.separatorChar);
        }
    }

    protected Connection getOwner() {
        return this.owner;
    }

    public void initConnection() {
        AdapterInfo info = this.getInfo();
        info.getDriverVersion().setName(JedisKeys.DEFAULT_CLIENT_NAME);
        info.getDbVersion().setName("Redis");
        info.getDbVersion().setVersion("Unknown");
        info.getDbVersion().setMajorVersion(0);
        info.getDbVersion().setMinorVersion(0);

        try {
            String serverInfo = jedisCmd.getServerCommands().info("server");
            Properties properties = new Properties();
            properties.load(new StringReader(serverInfo));
            String redisVersion = String.valueOf(properties.get("redis_version"));
            info.getDbVersion().setVersion(redisVersion);

            if (StringUtils.isNotBlank(redisVersion)) {
                String[] version = StringUtils.split(redisVersion, ".");
                if (version.length >= 1) {
                    info.getDbVersion().setMajorVersion(Integer.parseInt(version[0]));
                }
                if (version.length >= 2) {
                    info.getDbVersion().setMinorVersion(Integer.parseInt(version[1]));
                }
            }
        } catch (Throwable e) {
            logger.warn("initConnection failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getCatalog() {
        return this.getSchema();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        this.setSchema(catalog);
    }

    @Override
    public String getSchema() {
        return String.valueOf(database);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        int newDatabase = Integer.parseInt(schema);
        if (this.database != newDatabase) {
            this.database = newDatabase;
            String status = this.jedisCmd.getDatabaseCommands().select(newDatabase);
            if (!StringUtils.equalsIgnoreCase(status, "ok")) {
                throw new SQLException("select database failed, status: " + status);
            }
        }
    }

    @Override
    public AdapterRequest newRequest(String sql) {
        JedisRequest request = new JedisRequest(sql);
        request.setNumKeysCheck(this.uncheckNumKeys);
        return request;
    }

    @Override
    protected <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface == JedisConn.class) {
            return (T) this;
        } else if (iface == JedisCmd.class) {
            return (T) this.jedisCmd;
        } else if (iface == Jedis.class || iface == JedisCluster.class) {
            Object target = this.jedisCmd.getTarget();
            if (iface == Jedis.class && target instanceof Jedis) {
                return (T) target;
            } else if (iface == JedisCluster.class && target instanceof JedisCluster) {
                return (T) target;
            }
        }

        return super.unwrap(iface);
    }

    public void killDriverConnection(String connID) throws SQLException {
        JedisConn conn = (JedisConn) AdapterConnManager.getConnection(connID);
        if (conn != null) {
            try {
                conn.close();
            } catch (Throwable e) {
                Throwable ee = ExceptionUtils.getRootCause(e);
                if (ee instanceof SQLException) {
                    throw (SQLException) ee;
                } else {
                    throw new SQLException(e);
                }
            }
        }
    }

    protected RedisParser.RootContext parserRequest(AdapterRequest request) throws SQLException {
        if (StringUtils.isBlank(((JedisRequest) request).getCommandBody())) {
            throw new SQLException("query command is empty.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }
        try {
            RedisLexer lexer = new RedisLexer(CharStreams.fromString(((JedisRequest) request).getCommandBody()));
            lexer.setSeparatorChar(this.separatorChar);
            lexer.removeErrorListeners();
            lexer.addErrorListener(ThrowingListener.INSTANCE);

            RedisParser parser = new RedisParser(new BufferedTokenStream(lexer));
            parser.setSeparatorChar(this.separatorChar);
            parser.removeErrorListeners();
            parser.addErrorListener(ThrowingListener.INSTANCE);
            return parser.root();
        } catch (QueryParseException e) {
            String errorMsg = "command '" + ((JedisRequest) request).getCommandBody() + "' parserFailed. (separatorChar = '" + getSeparatorCharString() + "')";
            throw new SQLException(errorMsg, JdbcErrorCode.SQL_STATE_SYNTAX_ERROR);
        }
    }

    @Override
    public synchronized void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("doRequest: " + ((JedisRequest) request).getCommandBody());
        }
        this.cancelled = false;
        RedisParser.RootContext root = parserRequest(request);
        JedisArgVisitor argVisitor = new JedisArgVisitor();
        root.accept(argVisitor);
        int argCount = argVisitor.getArgCount();
        List<RedisParser.CommandContext> commandList = argVisitor.getCommandList();

        if (commandList.isEmpty()) {
            throw new SQLException("query command is empty.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }
        if (argCount > 0) {
            if (argCount != request.getArgMap().size()) {
                throw new SQLException("param size not match.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        int startArgIdx = 0;
        for (RedisParser.CommandContext redisCmd : commandList) {
            if (this.cancelled) {
                throw new SQLException("Operation cancelled.", JdbcErrorCode.SQL_STATE_IS_CANCELLED);
            }
            Future<Object> sync = new BasicFuture<>();
            if (argCount > 0) {
                argVisitor.reset();
                redisCmd.accept(argVisitor);
                JedisDistributeCall.execRedisCmd(sync, this.jedisCmd, redisCmd, request, receive, startArgIdx, this);
                startArgIdx += argVisitor.getArgCount();
            } else {
                JedisDistributeCall.execRedisCmd(sync, this.jedisCmd, redisCmd, request, receive, startArgIdx, this);
            }

            sync.await();

            if (sync.isDone() && sync.getCause() != null) {
                receive.responseFailed(request, sync.getCause());
                break;
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
        this.jedisCmd.close();
    }
}
