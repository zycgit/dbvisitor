package net.hasor.dbvisitor.adapter.milvus.commands;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import net.hasor.dbvisitor.adapter.milvus.MilvusConnFactory;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import org.junit.Test;
import static org.junit.Assert.*;

/** Keep parameter spelling stable and prevent new string-based key references in production code. */
public class MilvusKeysTest {
    @Test
    public void jdbcKeysContainOnlyRegisteredConnectionParameters() throws Exception {
        // Literal expected values deliberately freeze the JDBC contract independently of the constants.
        Set<String> expected = new HashSet<>(Arrays.asList("adapterName", "interceptor", "customMilvus", "server", "timeZone", "database", "token", "user", "password", "connectTimeout", "keepAliveTime", "keepAliveTimeout", "keepAliveWithoutCalls", "idleTimeout", "rpcDeadline", "maxRetry", "consistencyLevel", "secure", "caPemPath", "serverPemPath", "clientPemPath", "clientKeyPath", "serverName"));
        String[] properties = new MilvusConnFactory().getPropertyNames();
        Set<String> actual = new HashSet<>(Arrays.asList(properties));
        assertEquals(expected, actual);
        assertEquals(expected.size(), properties.length);
        Set<String> declared = new HashSet<>();
        for (Field field : MilvusKeys.class.getFields()) {
            assertEquals("Only connection parameter names belong in MilvusKeys: " + field.getName(), String.class, field.getType());
            declared.add((String) field.get(null));
        }
        assertEquals(expected, declared);
        assertFalse(actual.contains(MilvusCommandKeys.TIMEOUT));
    }

    @Test
    public void parameterReferencesUseConstants() throws Exception {
        Map<String, String> keys = new HashMap<>();
        for (Class<?> owner : Arrays.asList(MilvusKeys.class, MilvusCommandKeys.class)) {
            for (Field field : owner.getFields()) {
                if (field.getType() == String.class)
                    keys.put((String) field.get(null), owner.getSimpleName() + "." + field.getName());
            }
        }
        Set<String> keyMethods = new HashSet<>(Arrays.asList("get", "put", "remove", "containsKey", "getOrDefault", "getProperty", "setProperty", "has", "add", "addProperty", "getAsJsonObject", "getAsJsonArray", "setKey", "singletonMap"));
        Set<String> secondArgumentKeys = new HashSet<>(Arrays.asList("hintAsBoolean", "hintAsLong", "property", "text", "number", "integerBound"));
        Path root = Paths.get("src/main/java/net/hasor/dbvisitor/adapter/milvus");
        if (!Files.isDirectory(root))
            root = Paths.get("dbvisitor-adapter/jdbc-milvus").resolve(root);
        List<Path> sources;
        try (Stream<Path> paths = Files.walk(root)) {
            sources = paths.filter(path -> path.toString().endsWith(".java")).filter(path -> !path.getFileName().toString().equals("MilvusKeys.java")).filter(path -> !path.getFileName().toString().equals("MilvusCommandKeys.java")).collect(Collectors.toList());
        }
        assertFalse(sources.isEmpty());
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull("Run this test with a JDK", compiler);
        try (StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8)) {
            JavacTask task = (JavacTask) compiler.getTask(null, manager, null, Collections.singletonList("-proc:none"), null, manager.getJavaFileObjectsFromPaths(sources));
            for (CompilationUnitTree source : task.parse()) {
                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethodInvocation(MethodInvocationTree call, Void unused) {
                        ExpressionTree select = call.getMethodSelect();
                        String method = select instanceof MemberSelectTree member ? member.getIdentifier().toString() : select.toString();
                        if (keyMethods.contains(method) && !call.getArguments().isEmpty()) {
                            checkKey(call.getArguments().get(0));
                        } else if (secondArgumentKeys.contains(method) && call.getArguments().size() > 1) {
                            checkKey(call.getArguments().get(1));
                        } else if ("asList".equals(method) || "of".equals(method)) {
                            call.getArguments().forEach(this::checkKey);
                        }
                        if (select instanceof MemberSelectTree member) {
                            if (member.getIdentifier().contentEquals("equals") || member.getIdentifier().contentEquals("equalsIgnoreCase")) {
                                checkKey(member.getExpression());
                                call.getArguments().forEach(this::checkKey);
                            }
                        }
                        return super.visitMethodInvocation(call, unused);
                    }

                    private void checkKey(ExpressionTree expression) {
                        if (expression instanceof LiteralTree literal && keys.containsKey(literal.getValue())) {
                            fail(source.getSourceFile().getName() + ": parameter reference " + literal + " must use " + keys.get(literal.getValue()));
                        }
                    }
                }.scan(source, null);
            }
        }
    }
}
