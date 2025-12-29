package net.hasor.dbvisitor.adapter.elastic;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.driver.JdbcErrorCode;

class ElasticResultBuffer implements Closeable, Iterable<Map<String, Object>> {
    private final long                      thresholdBytes;
    private final long                      maxFileSizeBytes;
    private final File                      cacheDir;
    private final List<Map<String, Object>> memoryBuffer;
    private       File                      tempFile;
    private       ObjectOutputStream        fileOutput;
    private       long                      currentSize;
    private       boolean                   switchedToDisk;

    ElasticResultBuffer(long thresholdBytes, long maxFileSizeBytes, File cacheDir) {
        this.thresholdBytes = thresholdBytes;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.cacheDir = cacheDir;

        this.memoryBuffer = new ArrayList<>();
        this.currentSize = 0;
        this.switchedToDisk = false;
    }

    public boolean isSpilled() {
        return switchedToDisk;
    }

    public void add(Map<String, Object> doc) throws SQLException, IOException {
        // Simple size estimation (very rough)
        long docSize = 100; // minimal size
        if (doc != null) {
            docSize += doc.toString().length() * 2L;
        }
        currentSize += docSize;

        if (switchedToDisk) {
            if (currentSize > maxFileSizeBytes) {
                throw new SQLException("Result set size exceeded limit of " + (maxFileSizeBytes / 1024 / 1024) + "MB", JdbcErrorCode.SQL_STATE_GENERAL_ERROR);
            }
            fileOutput.writeObject(doc);
        } else {
            memoryBuffer.add(doc);
            if (currentSize > thresholdBytes) {
                switchToDisk();
            }
        }
    }

    private void switchToDisk() throws IOException {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        tempFile = File.createTempFile("elastic_result_", ".dat", cacheDir);
        tempFile.deleteOnExit();
        fileOutput = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));

        for (Map<String, Object> doc : memoryBuffer) {
            fileOutput.writeObject(doc);
        }
        memoryBuffer.clear();
        switchedToDisk = true;
    }

    public void finish() throws IOException {
        if (fileOutput != null) {
            fileOutput.flush();
            fileOutput.close();
        }
    }

    @Override
    public Iterator<Map<String, Object>> iterator() {
        if (!switchedToDisk) {
            return memoryBuffer.iterator();
        }

        return new Iterator<Map<String, Object>>() {
            private ObjectInputStream   input;
            private Map<String, Object> nextDoc;

            {
                try {
                    input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
                    advance();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            private void advance() {
                try {
                    Object obj = input.readObject();
                    if (obj instanceof Map) {
                        nextDoc = (Map<String, Object>) obj;
                    } else {
                        nextDoc = null;
                        input.close();
                    }
                } catch (EOFException e) {
                    nextDoc = null;
                    try {
                        input.close();
                    } catch (IOException ignored) {
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean hasNext() {
                return nextDoc != null;
            }

            @Override
            public Map<String, Object> next() {
                if (nextDoc == null) {
                    throw new java.util.NoSuchElementException();
                }
                Map<String, Object> result = nextDoc;
                advance();
                return result;
            }
        };
    }

    @Override
    public void close() throws IOException {
        if (fileOutput != null) {
            try {
                fileOutput.close();
            } catch (IOException ignored) {
            }
        }
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }
}
