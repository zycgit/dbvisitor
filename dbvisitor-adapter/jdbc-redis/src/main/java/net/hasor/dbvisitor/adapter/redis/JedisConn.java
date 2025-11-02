package net.hasor.dbvisitor.adapter.redis;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.concurrent.future.BasicFuture;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.JedisArgVisitor;
import net.hasor.dbvisitor.adapter.redis.parser.RedisLexer;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.adapter.redis.parser.ThrowingListener;
import net.hasor.dbvisitor.driver.*;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

public class JedisConn extends AdapterConnection {
    private final Connection owner;
    private final JedisCmd   jedisCmd;
    private       int        database;
    private final boolean    uncheckNumKeys;
    private final char       separatorChar;

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

    @Override
    public String getCatalog() {
        return this.getSchema();
    }

    @Override
    public void setCatalog(String catalog) {
        this.setSchema(catalog);
    }

    @Override
    public String getSchema() {
        return String.valueOf(database);
    }

    @Override
    public void setSchema(String schema) {
        int newDatabase = Integer.parseInt(schema);
        if (this.database != newDatabase) {
            this.database = newDatabase;
            this.jedisCmd.getDatabaseCommands().select(newDatabase);
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

    @Override
    public synchronized void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
        RedisLexer lexer = new RedisLexer(CharStreams.fromString(((JedisRequest) request).getCommandBody()));
        lexer.setSeparatorChar(this.separatorChar);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingListener.INSTANCE);

        RedisParser parser = new RedisParser(new BufferedTokenStream(lexer));
        parser.setSeparatorChar(this.separatorChar);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingListener.INSTANCE);
        RedisParser.RootContext root = parser.root();

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
            Future<Object> sync = new BasicFuture<>();
            if (argCount > 0) {
                argVisitor.reset();
                redisCmd.accept(argVisitor);
                JedisDistributeCall.execRedisCmd(sync, this.jedisCmd, redisCmd, request, receive, startArgIdx, this.owner);
                startArgIdx += argVisitor.getArgCount();
            } else {
                JedisDistributeCall.execRedisCmd(sync, this.jedisCmd, redisCmd, request, receive, startArgIdx, this.owner);
            }
            sync.await();
        }

        receive.responseFinish(request);
    }

    @Override
    public void cancelRequest() {
        throw new UnsupportedOperationException("cancelRequest not support."); // TODO
    }

    @Override
    protected void doClose() throws IOException {
        this.cancelRequest();
        this.jedisCmd.close();
    }
}
