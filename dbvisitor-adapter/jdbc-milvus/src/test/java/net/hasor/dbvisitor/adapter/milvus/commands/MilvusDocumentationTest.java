package net.hasor.dbvisitor.adapter.milvus.commands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusLexer;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.*;
import io.milvus.grpc.*;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.response.QueryResultsWrapper.RowRecord;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.response.GetLoadStateResp;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.*;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusConnFactory;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.antlr.v4.runtime.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

/** Compile the actual documented Java and validate SQL examples, without a Milvus service. */
public class MilvusDocumentationTest {
    @Rule
    public        TemporaryFolder temporary = new TemporaryFolder();
    private final List<Object>    requests  = new ArrayList<>();
    private       int             insertions;
    private       int             partialUpdates;
    private       int             deletions;

    @Before
    public void install() throws Exception {
        Class.forName(JdbcDriver.class.getName());
        MilvusCommandInterceptor.resetInterceptor();
        CollectionSchema schema = CollectionSchema.newBuilder().setName("books_demo").addFields(FieldSchema.newBuilder().setName("book_id").setDataType(DataType.Int64).setIsPrimaryKey(true)).addFields(FieldSchema.newBuilder().setName("title").setDataType(DataType.VarChar)).addFields(FieldSchema.newBuilder().setName("word_count").setDataType(DataType.Int32)).addFields(FieldSchema.newBuilder().setName("book_intro").setDataType(DataType.FloatVector)).build();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (args.length > 0) {
                requests.add(args[0]);
            }
            switch (method.getName()) {
                case "getServerVersion":
                    return "v2.6.2";
                case "describeCollection":
                    return v2Response(method.getName(), DescribeCollectionResponse.newBuilder().setSchema(schema).build());
                case "createCollection":
                case "createIndex":
                case "loadCollection":
                    return null;
                case "getLoadStateV2":
                    return GetLoadStateResp.builder().state(LoadState.LoadStateLoaded).progress(100L).build();
                case "insert":
                    insertions++;
                    InsertReq insert = (InsertReq) args[0];
                    assertEquals("books_demo", insert.getCollectionName());
                    assertEquals(2, insert.getData().get(0).getAsJsonArray("book_intro").size());
                    return InsertResp.builder().InsertCnt(1).build();
                case "upsert":
                    UpsertReq upsert = (UpsertReq) args[0];
                    if (upsert.isPartialUpdate()) {
                        partialUpdates++;
                        assertEquals(new HashSet<>(Arrays.asList("book_id", "word_count")), upsert.getData().get(0).keySet());
                    }
                    return UpsertResp.builder().upsertCnt(upsert.getData().size()).build();
                case "delete":
                    deletions++;
                    return DeleteResp.builder().deleteCnt(1).build();
                case "search":
                    SearchReq search = (SearchReq) args[0];
                    assertEquals(ConsistencyLevel.STRONG, search.getConsistencyLevel());
                    assertEquals(Arrays.asList("book_id", "title"), search.getOutputFields());
                    assertEquals(Arrays.asList(0.1F, 0.2F), search.getData().get(0).getData());
                    SearchResp.SearchResult hit = SearchResp.SearchResult.builder().primaryKey("book_id").id(1L).score(0F).entity(row().getFieldValues()).build();
                    return SearchResp.builder().searchResults(Collections.singletonList(Collections.singletonList(hit))).build();
                case "query":
                    QueryReq query = (QueryReq) args[0];
                    Map<String, Object> entity = query.getOutputFields().contains("count(*)") ? Collections.singletonMap("count(*)", 1L) : row().getFieldValues();
                    return QueryResp.builder().queryResults(Collections.singletonList(QueryResp.QueryResult.builder().entity(entity).build())).build();
                case "queryIterator":
                    QueryIterator iterator = Mockito.mock(QueryIterator.class);
                    Mockito.when(iterator.next()).thenReturn(Collections.singletonList(row()), Collections.emptyList());
                    return iterator;
                default:
                    throw new AssertionError("Unexpected SDK call from documentation example: " + method.getName());
            }
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private static RowRecord row() {
        RowRecord row = new RowRecord();
        row.put("book_id", 1L);
        row.put("title", "A book");
        row.put("word_count", 1000);
        row.put("book_intro", Arrays.asList(0.1F, 0.2F));
        return row;
    }

    private String interceptedUrl() {
        return "jdbc:dbvisitor:milvus://mock:19530/default?" + MilvusKeys.CUSTOM_MILVUS + "=" + MilvusCustomClient.class.getName() + "&" + MilvusKeys.INTERCEPTOR + "=" + MilvusCommandInterceptor.class.getName();
    }

    private Path repositoryRoot() {
        Path root = Paths.get("").toAbsolutePath();
        while (root != null && !Files.isDirectory(root.resolve("dbvisitor-doc"))) {
            root = root.getParent();
        }
        assertNotNull("Run documentation tests inside the dbvisitor repository", root);
        return root;
    }

    private String document(String language, String file) throws Exception {
        String directory = "cn".equals(language) ? "docs" : "i18n/en/docusaurus-plugin-content-docs/current";
        Path path = repositoryRoot().resolve("dbvisitor-doc").resolve(directory).resolve("drivers/milvus").resolve(file);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static List<String> blocks(String document, String language) {
        Matcher matcher = Pattern.compile("(?m)^`{3}" + language + "\\r?\\n([\\s\\S]*?)^`{3}\\s*$").matcher(document);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    @Test
    public void bothWebsiteProgramsCompileAndRunThroughJdbc() throws Exception {
        for (String language : Arrays.asList("cn", "en")) {
            String source = blocks(document(language, "usecase.mdx"), "java").get(0);
            try (URLClassLoader compiled = compile("MilvusJdbcExample", source)) {
                compiled.loadClass("MilvusJdbcExample").getMethod("main", String[].class).invoke(null, (Object) new String[] { interceptedUrl() });
            }
        }
        assertEquals(2, insertions);
        CreateCollectionReq collection = (CreateCollectionReq) requests.stream().filter(CreateCollectionReq.class::isInstance).findFirst().get();
        assertEquals("books_demo", collection.getCollectionName());
        CreateIndexReq index = (CreateIndexReq) requests.stream().filter(CreateIndexReq.class::isInstance).findFirst().get();
        assertEquals("L2", index.getIndexParams().get(0).getMetricType().name());
    }

    @Test
    public void everyOtherJavaBlockCompiles() throws Exception {
        for (String language : Arrays.asList("cn", "en")) {
            for (String name : Arrays.asList("usecase.mdx", "params.md", "commands.md")) {
                for (String block : blocks(document(language, name), "java")) {
                    if (block.contains("public class MilvusJdbcExample")) {
                        continue;
                    }
                    boolean factory = block.contains("public class MyMilvusFactory");
                    String className = factory ? "MyMilvusFactory" : "DocumentationFragment";
                    String source = factory ? block : fragment(block);
                    try (URLClassLoader ignored = compile(className, source)) {
                        // Compilation checks the actual document, not a separately maintained copy.
                    }
                }
            }
        }
    }

    @Test
    public void documentedWritePagingAndMultiResultFragmentsExecute() throws Exception {
        for (String language : Arrays.asList("cn", "en")) {
            List<String> examples = new ArrayList<>();
            for (String block : blocks(document(language, "usecase.mdx"), "java")) {
                if (block.startsWith("try (PreparedStatement")) {
                    examples.add(block);
                }
            }
            examples.addAll(blocks(document(language, "commands.md"), "java"));
            assertEquals(3, examples.size());
            Properties props = new Properties();
            props.setProperty(MilvusKeys.CONSISTENCY_LEVEL, "Strong");
            try (Connection conn = DriverManager.getConnection(interceptedUrl(), props)) {
                for (String example : examples) {
                    try (URLClassLoader compiled = compile("DocumentationFragment", fragment(example))) {
                        compiled.loadClass("DocumentationFragment").getMethod("run", Connection.class).invoke(null, conn);
                    }
                }
            }
        }
        assertEquals(4, partialUpdates);
        assertEquals(2, deletions);
    }

    @Test
    public void allSqlExampleBlocksParseWithoutRecovery() throws Exception {
        int examples = 0;
        BaseErrorListener strict = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int position, String message, RecognitionException e) {
                throw new AssertionError("SQL example at " + line + ":" + position + ": " + message, e);
            }
        };
        for (String language : Arrays.asList("cn", "en")) {
            List<String> sqlBlocks = new ArrayList<>();
            for (String name : Arrays.asList("usecase.mdx", "params.md", "commands.md")) {
                sqlBlocks.addAll(blocks(document(language, name), "sql"));
            }
            for (String sql : sqlBlocks) {
                MilvusLexer lexer = new MilvusLexer(CharStreams.fromString(sql));
                lexer.removeErrorListeners();
                lexer.addErrorListener(strict);
                MilvusParser parser = new MilvusParser(new CommonTokenStream(lexer));
                parser.removeErrorListeners();
                parser.addErrorListener(strict);
                assertFalse(sql, parser.root().hintCommand().isEmpty());
                examples++;
            }
        }
        assertTrue("Expected all concrete manual examples, not syntax templates", examples >= 30);
    }

    @Test
    public void countPartitionAndHintSemanticsMatchTheManual() throws Exception {
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CONSISTENCY_LEVEL, "Strong");
        try (Connection conn = DriverManager.getConnection(interceptedUrl(), props); PreparedStatement ps = conn.prepareStatement("COUNT FROM books_demo PARTITION p WHERE word_count > ?")) {
            ps.setInt(1, 100);
            try (ResultSet result = ps.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1L, result.getLong("COUNT"));
            }
            QueryReq count = (QueryReq) requests.stream().filter(QueryReq.class::isInstance).findFirst().get();
            assertEquals(Collections.singletonList("p"), count.getPartitionNames());
            assertEquals(ConsistencyLevel.STRONG, count.getConsistencyLevel());
            assertEquals("word_count > {arg1}", count.getFilter());
            assertEquals(Collections.singletonMap("arg1", 100), count.getFilterTemplateValues());
            try (Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("/*+ overwrite_find_as_count=false */ SELECT book_id FROM books_demo")) {
                assertTrue(result.next());
                assertEquals("COUNT", result.getMetaData().getColumnName(1));
            }
        }
    }

    @Test
    public void connectionTablesCoverExactlyTheRegisteredProperties() throws Exception {
        Set<String> expected = new HashSet<>(Arrays.asList(new MilvusConnFactory().getPropertyNames()));
        for (String language : Arrays.asList("cn", "en")) {
            String parameters = document(language, "params.md");
            String heading = "cn".equals(language) ? "### 连接参数" : "### Connection Parameters";
            int start = parameters.indexOf(heading);
            assertTrue("Missing connection properties section: " + language, start >= 0);
            String section = parameters.substring(start, parameters.indexOf("\n### ", start + heading.length()));
            Set<String> documented = new HashSet<>();
            for (String line : section.split("\\R")) {
                if (line.startsWith("| `")) {
                    String firstCell = line.split("\\|")[1];
                    Matcher name = Pattern.compile("`([^`]+)`").matcher(firstCell);
                    while (name.find()) {
                        documented.add(name.group(1));
                    }
                }
            }
            assertEquals(expected, documented);
        }
    }

    @Test
    public void moduleReadmeLinksToBothWebsiteLanguages() throws Exception {
        Path module = repositoryRoot().resolve("dbvisitor-adapter/jdbc-milvus");
        String entry = Files.readString(module.resolve("README.md"), StandardCharsets.UTF_8);
        Matcher link = Pattern.compile("\\]\\(([^)]+)\\)").matcher(entry);
        Set<String> linkedDocuments = new HashSet<>();
        while (link.find()) {
            Path target = module.resolve(link.group(1)).normalize();
            assertTrue("Missing documentation target: " + target, Files.isRegularFile(target));
            linkedDocuments.add(Files.readString(target, StandardCharsets.UTF_8));
        }
        Set<String> expected = new HashSet<>();
        for (String language : Arrays.asList("cn", "en")) {
            expected.add(document(language, "about.md"));
        }
        assertEquals(expected, linkedDocuments);
    }

    private static String fragment(String code) {
        String argument = code.startsWith("Properties props") ? "Connection unused" : "Connection conn";
        return "import java.sql.*; import java.util.*;\npublic class DocumentationFragment {\n" + "public static void run(" + argument + ") throws Exception {\n" + code + "\n}\n}";
    }

    private URLClassLoader compile(String className, String source) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull("Documentation compilation requires a JDK", compiler);
        Path output = temporary.newFolder().toPath();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaFileObject unit = new SimpleJavaFileObject(URI.create("string:///" + className + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        Set<String> classpath = new LinkedHashSet<>(Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator)));
        for (ClassLoader loader = getClass().getClassLoader(); loader != null; loader = loader.getParent()) {
            if (loader instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) loader).getURLs()) {
                    if ("file".equals(url.getProtocol()))
                        classpath.add(Paths.get(url.toURI()).toString());
                }
            }
        }
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            List<String> options = Arrays.asList("--release", "17", "-proc:none", "-classpath", String.join(File.pathSeparator, classpath), "-d", output.toString());
            boolean compiled = compiler.getTask(null, files, diagnostics, options, null, Collections.singletonList(unit)).call();
            assertTrue(diagnostics.getDiagnostics().toString(), compiled);
        }
        return new URLClassLoader(new URL[] { output.toUri().toURL() }, getClass().getClassLoader());
    }
}
