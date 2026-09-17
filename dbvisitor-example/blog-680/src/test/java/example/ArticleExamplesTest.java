/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package example;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArticleExamplesTest {
    @Test
    public void jsonFieldRoundTrip() throws Exception {
        String output = outputOf(() -> JsonField.main(new String[0]));
        assertTrue(output, output.contains("theme=dark"));
        assertTrue(output, output.contains("channels=[email, app]"));
        assertTrue(output, output.contains("language=zh-CN"));
        assertTrue(output, output.contains("updated=light"));
    }

    @Test
    public void threeStylesShareOneMapper() throws Exception {
        String output = outputOf(() -> ApiStyles.main(new String[0]));
        assertTrue(output, output.contains("builder=2"));
        assertTrue(output, output.contains("annotation=1000"));
        assertTrue(output, output.contains("xml=PAID; total=3000"));
    }

    @Test
    public void jdbcFiltersAndRanks() throws Exception {
        integration();
        String output = outputOf(() -> MilvusJdbc.main(new String[0]));
        assertTrue(output, output.contains("1 | Vector introduction | 0.0"));
        assertTrue(output, output.contains("2 | Mapper guide | 2.0"));
        assertFalse(output, output.contains("3 |"));
    }

    @Test
    public void daoPreservesUnmodifiedVector() throws Exception {
        integration();
        String output = outputOf(() -> MilvusDao.main(new String[0]));
        assertTrue(output, output.contains("builder=Vector introduction"));
        assertTrue(output, output.contains("annotation=Vector introduction"));
        assertTrue(output, output.contains("updated=Revised guide; vector=[1.0, 0.0]"));
    }

    @Test
    public void hybridCombinesBm25AndDenseWithinCategory() throws Exception {
        integration();
        String output = outputOf(() -> MilvusHybrid.main(new String[0]));
        assertTrue(output, output.contains("hybrid=1 | milvus vector search"));
        assertTrue(output, output.contains("hybrid=2 | mapper database guide"));
        assertTrue(output, output.indexOf("hybrid=1") < output.indexOf("hybrid=2"));
        assertFalse(output, output.contains("hybrid=3"));
    }

    @Test
    public void iteratorWritesAcrossPageBoundaries() throws Exception {
        integration();
        String output = outputOf(() -> MilvusIngest.main(new String[0]));
        assertTrue(output, output.contains("written=5"));
        assertTrue(output, output.contains("count=5"));
    }

    @Test
    public void cacheMapsJsonAndExpires() throws Exception {
        integration();
        String output = outputOf(() -> RedisCache.main(new String[0]));
        assertTrue(output, output.contains("missing=null"));
        assertTrue(output, output.contains("cached=USB-C Hub; priceCents=12900"));
        assertTrue(output, output.contains("afterEvict=null"));
        String ttlLine = output.lines().filter(line -> line.startsWith("ttl=")).findFirst().orElseThrow();
        long ttl = Long.parseLong(ttlLine.substring(4));
        assertTrue(ttl > 0 && ttl <= 300);
    }

    private static void integration() {
        Assume.assumeTrue("Set BLOG_INTEGRATION=true to run against Milvus and Redis", "true".equals(System.getenv("BLOG_INTEGRATION")));
    }

    private static String outputOf(Example example) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            example.run();
        } finally {
            System.setOut(original);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private interface Example {
        void run() throws Exception;
    }
}
