package net.hasor.dbvisitor.adapter.milvus.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.junit.Test;
import static org.junit.Assert.fail;

public class MilvusParserTest {

    @Test
    public void testDatabaseCommands() throws IOException {
        runTestFromFile("milvus_tests/database_commands.txt");
    }

    @Test
    public void testTableCommands() throws IOException {
        runTestFromFile("milvus_tests/table_commands.txt");
    }

    @Test
    public void testPartitionCommands() throws IOException {
        runTestFromFile("milvus_tests/partition_commands.txt");
    }

    @Test
    public void testIndexCommands() throws IOException {
        runTestFromFile("milvus_tests/index_commands.txt");
    }

    @Test
    public void testAliasCommands() throws IOException {
        runTestFromFile("milvus_tests/alias_commands.txt");
    }

    @Test
    public void testUserCommands() throws IOException {
        runTestFromFile("milvus_tests/user_commands.txt");
    }

    @Test
    public void testProgressCommands() throws IOException {
        runTestFromFile("milvus_tests/progress_commands.txt");
    }

    @Test
    public void testDMLCommands() throws IOException {
        runTestFromFile("milvus_tests/dml_commands.txt");
    }

    @Test
    public void testDQLCommands() throws IOException {
        runTestFromFile("milvus_tests/dql_commands.txt");
    }

    @Test
    public void testHintCommands() throws IOException {
        runTestFromFile("milvus_tests/hint_commands.txt");
    }

    @Test
    public void testParameterizedCommands() throws IOException {
        runTestFromFile("milvus_tests/parameterized_commands.txt");
    }

    private void runTestFromFile(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                fail("Resource not found: " + resourcePath);
                return;
            }

            StringBuilder content = new StringBuilder();
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (!line.isEmpty() && !line.startsWith("--")) {
                        content.append(line).append("\n");
                    }
                }
            }

            // Execute line by line or as a block? 
            // The grammar supports `commands : command (SEMI? command)*`.
            // Let's feed the whole content.
            parseCommand(content.toString());
        }
    }

    private void parseCommand(String command) {
        System.out.println("Testing command block:\n" + command);
        MilvusLexer lexer = new MilvusLexer(CharStreams.fromString(command));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ConsoleErrorListener() {
            @Override
            public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, org.antlr.v4.runtime.RecognitionException e) {
                fail("Lexer error at line " + line + ":" + charPositionInLine + " " + msg);
            }
        });

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MilvusParser parser = new MilvusParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ConsoleErrorListener() {
            @Override
            public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, org.antlr.v4.runtime.RecognitionException e) {
                fail("Parser error at line " + line + ":" + charPositionInLine + " " + msg);
            }
        });

        parser.root();
    }
}
