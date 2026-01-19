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
package net.hasor.dbvisitor.adapter.mongo;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.driver.JdbcErrorCode;
import org.bson.Document;

class MongoResultBuffer implements Closeable, Iterable<Document> {
    private final long               thresholdBytes;
    private final long               maxFileSizeBytes;
    private final File               cacheDir;
    private final List<Document>     memoryBuffer;
    private final List<InputStream>  openStreams;
    private       File               tempFile;
    private       ObjectOutputStream fileOutput;
    private       long               currentSize;
    private       boolean            switchedToDisk;

    MongoResultBuffer(long thresholdBytes, long maxFileSizeBytes, File cacheDir) {
        this.thresholdBytes = thresholdBytes;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.cacheDir = cacheDir;

        this.memoryBuffer = new ArrayList<>();
        this.openStreams = new ArrayList<>();
        this.currentSize = 0;
        this.switchedToDisk = false;
    }

    public boolean isSpilled() {
        return switchedToDisk;
    }

    public void add(Document doc) throws SQLException, IOException {
        // Simple size estimation
        long docSize = doc.toJson().getBytes().length;
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
        tempFile = File.createTempFile("mongo_result_", ".dat", cacheDir);
        tempFile.deleteOnExit();
        fileOutput = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));

        for (Document doc : memoryBuffer) {
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
    public Iterator<Document> iterator() {
        if (!switchedToDisk) {
            return memoryBuffer.iterator();
        }

        return new Iterator<Document>() {
            private ObjectInputStream input;
            private Document          nextDoc;

            {
                try {
                    input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
                    openStreams.add(input);
                    advance();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            private void advance() {
                try {
                    Object obj = input.readObject();
                    if (obj instanceof Document) {
                        nextDoc = (Document) obj;
                    } else {
                        nextDoc = null;
                        close();
                    }
                } catch (EOFException e) {
                    nextDoc = null;
                    close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private void close() {
                if (input != null) {
                    IOUtils.closeQuietly(input);
                    input = null;
                }
            }

            @Override
            public boolean hasNext() {
                return nextDoc != null;
            }

            @Override
            public Document next() {
                if (nextDoc == null) {
                    throw new NoSuchElementException();
                }
                Document current = nextDoc;
                advance();
                return current;
            }
        };
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(fileOutput);
        for (InputStream is : openStreams) {
            IOUtils.closeQuietly(is);
        }
        openStreams.clear();
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }
}
