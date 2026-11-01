/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package example;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArticleExamplesTest {
    @Test
    public void jsonFieldRoundTrip() throws Exception {
        String output = outputOf(() -> JsonField.main(new String[0]));
        assertTrue(output.contains("theme=dark"), output);
        assertTrue(output.contains("channels=[email, app]"), output);
        assertTrue(output.contains("language=zh-CN"), output);
        assertTrue(output.contains("updated=light"), output);
    }

    @Test
    public void threeStylesShareOneMapper() throws Exception {
        String output = outputOf(() -> ApiStyles.main(new String[0]));
        assertTrue(output.contains("builder=2"), output);
        assertTrue(output.contains("annotation=1000"), output);
        assertTrue(output.contains("xml=PAID; total=3000"), output);
    }

    @Test
    public void jdbcFiltersAndRanks() throws Exception {
        integration();
        String output = outputOf(() -> MilvusJdbc.main(new String[0]));
        assertTrue(output.contains("1 | Vector introduction | 0.0"), output);
        assertTrue(output.contains("2 | Mapper guide | 2.0"), output);
        assertFalse(output.contains("3 |"), output);
    }

    @Test
    public void daoPreservesUnmodifiedVector() throws Exception {
        integration();
        String output = outputOf(() -> MilvusDao.main(new String[0]));
        assertTrue(output.contains("builder=Vector introduction"), output);
        assertTrue(output.contains("annotation=Vector introduction"), output);
        assertTrue(output.contains("updated=Revised guide; vector=[1.0, 0.0]"), output);
    }

    @Test
    public void hybridCombinesBm25AndDenseWithinCategory() throws Exception {
        integration();
        String output = outputOf(() -> MilvusHybrid.main(new String[0]));
        assertTrue(output.contains("hybrid=1 | milvus vector search"), output);
        assertTrue(output.contains("hybrid=2 | mapper database guide"), output);
        assertTrue(output.indexOf("hybrid=1") < output.indexOf("hybrid=2"), output);
        assertFalse(output.contains("hybrid=3"), output);
    }

    @Test
    public void iteratorWritesAcrossPageBoundaries() throws Exception {
        integration();
        String output = outputOf(() -> MilvusIngest.main(new String[0]));
        assertTrue(output.contains("written=5"), output);
        assertTrue(output.contains("count=5"), output);
    }

    @Test
    public void cacheMapsJsonAndExpires() throws Exception {
        integration();
        String output = outputOf(() -> RedisCache.main(new String[0]));
        assertTrue(output.contains("missing=null"), output);
        assertTrue(output.contains("cached=USB-C Hub; priceCents=12900"), output);
        assertTrue(output.contains("afterEvict=null"), output);
        String ttlLine = output.lines().filter(line -> line.startsWith("ttl=")).findFirst().orElseThrow();
        long ttl = Long.parseLong(ttlLine.substring(4));
        assertTrue(ttl > 0 && ttl <= 300);
    }

    private static void integration() {
        Assumptions.assumeTrue("true".equals(System.getenv("BLOG_INTEGRATION")), "Set BLOG_INTEGRATION=true to run against Milvus and Redis");
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
