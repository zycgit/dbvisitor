package net.hasor.dbvisitor.adapter.milvus.commands;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLRecoverableException;
import java.sql.SQLTransientException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import io.milvus.shaded.io.grpc.Status;
import io.milvus.shaded.io.grpc.StatusException;
import io.milvus.shaded.io.grpc.StatusRuntimeException;
import io.milvus.v2.exception.MilvusClientException;
import net.hasor.dbvisitor.adapter.milvus.MilvusRequest;
import net.hasor.dbvisitor.driver.AdapterRequest;
import static net.hasor.dbvisitor.adapter.milvus.MilvusRequest.checkActive;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands.sleepQuietly;

/** Bounded retries for the write calls explicitly routed through this policy; not an exactly-once guarantee. */
public final class MilvusRetry {
    private MilvusRetry() {
    }

    private static final long INITIAL_DELAY_MS  = 100;
    private static final long MAX_DELAY_MS      = 1_000;
    private static final long CANCEL_POLL_MS    = 25;
    // Milvus 2.3+ server code; SDK RpcUtils also recognizes it alongside the legacy RateLimit enum.
    private static final int  SERVER_RATE_LIMIT = 8;

    @FunctionalInterface
    public interface Call<T> {
        T execute() throws SQLException;
    }

    public static <T> T execute(AdapterRequest request, String operation, Call<T> call) throws SQLException {
        int maxRetry = request instanceof MilvusRequest ? ((MilvusRequest) request).getMaxRetry() : 0;
        int retries = 0;
        long delay = INITIAL_DELAY_MS;
        while (true) {
            checkActive(request);
            SQLException failure;
            try {
                T result = call.execute();
                checkActive(request);
                if (result == null) {
                    throw new SQLException(operation + " returned null.");
                }
                return result;
            } catch (SQLException | RuntimeException e) {
                failure = sqlException(e);
            }
            checkActive(request);
            if (retries >= maxRetry || !isRetryable(failure)) {
                throw new SQLException(operation + " failed after " + ((long) retries + 1) + " attempt(s): " + failure.getMessage(), failure.getSQLState(), failure.getErrorCode(), failure);
            }
            awaitRetry(request, delay);
            delay = Math.min(MAX_DELAY_MS, delay * 2);
            retries++;
        }
    }

    public static SQLException sqlException(Exception failure) {
        return failure instanceof SQLException ? (SQLException) failure : new SQLException(failure.getMessage(), failure);
    }

    private static boolean isRetryable(Throwable failure) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        boolean transientFailure = false;
        for (Throwable cause = failure; cause != null && visited.add(cause); cause = cause.getCause()) {
            // Prefer a specific SDK/transport status over a generic JDBC wrapper.
            if (cause instanceof StatusRuntimeException) {
                return retryableStatus(((StatusRuntimeException) cause).getStatus().getCode());
            }
            if (cause instanceof StatusException) {
                return retryableStatus(((StatusException) cause).getStatus().getCode());
            }
            if (cause instanceof MilvusClientException sdk) {
                switch (sdk.getErrorCode()) {
                    case INVALID_PARAMS:
                    case COLLECTION_NOT_FOUND:
                    case CLIENT_ERROR:
                        return false;
                    case TIMEOUT:
                        transientFailure = true;
                        break;
                    case SERVER_ERROR:
                        return sdk.getServerErrCode() == SERVER_RATE_LIMIT || retryableServerCode(sdk.getLegacyServerCode());
                    default:
                        break;
                }
            }
            if (cause instanceof SQLNonTransientException || cause instanceof IllegalArgumentException) {
                return false;
            }
            if (cause instanceof SQLException) {
                String state = ((SQLException) cause).getSQLState();
                if (state != null && ("08004".equals(state) || state.startsWith("22") || state.startsWith("23") || state.startsWith("28") || state.startsWith("42"))) {
                    return false;
                }
                transientFailure |= cause instanceof SQLTransientException || cause instanceof SQLRecoverableException;
                transientFailure |= state != null && state.startsWith("08") && !"08004".equals(state);
            }
        }
        // Unknown/programming errors are not assumed to become valid by repeating a write.
        return transientFailure;
    }

    private static boolean retryableStatus(Status.Code code) {
        switch (code) {
            case UNAVAILABLE:
            case RESOURCE_EXHAUSTED:
            case ABORTED:
            case DEADLINE_EXCEEDED:
                return true;
            default:
                return false;
        }
    }

    private static boolean retryableServerCode(int code) {
        io.milvus.grpc.ErrorCode legacy = io.milvus.grpc.ErrorCode.forNumber(code);
        return legacy == io.milvus.grpc.ErrorCode.RateLimit || legacy == io.milvus.grpc.ErrorCode.NotReadyServe || legacy == io.milvus.grpc.ErrorCode.NotReadyCoordActivating || legacy == io.milvus.grpc.ErrorCode.NotShardLeader || legacy == io.milvus.grpc.ErrorCode.NoReplicaAvailable || legacy == io.milvus.grpc.ErrorCode.DataCoordNA;
    }

    private static void awaitRetry(AdapterRequest request, long delayMillis) throws SQLException {
        long started = System.nanoTime();
        long delayNanos = TimeUnit.MILLISECONDS.toNanos(delayMillis);
        while (true) {
            checkActive(request);
            long remaining = delayNanos - (System.nanoTime() - started);
            if (remaining <= 0) {
                return;
            }
            sleepQuietly(Math.min(CANCEL_POLL_MS, Math.max(1, TimeUnit.NANOSECONDS.toMillis(remaining))));
        }
    }
}
