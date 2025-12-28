package net.hasor.dbvisitor.adapter.elastic.parser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;
import static org.junit.Assert.fail;

public class ElasticParserTest {

    @Test
    public void testBasicCommands() throws IOException {
        runTestFromFile("elastic_tests/basic_commands.txt");
    }

    @Test
    public void testCrudCommands() throws IOException {
        runTestFromFile("elastic_tests/crud_commands.txt");
    }

    @Test
    public void testPlaceholderCommands() throws IOException {
        runTestFromFile("elastic_tests/placeholder_commands.txt");
    }

    @Test
    public void testOperationTypes() throws IOException {
        runTestFromFile("elastic_tests/operation_types.txt");
    }

    @Test
    public void testIndexManagement() throws IOException {
        runTestFromFile("elastic_tests/index_aliases.txt");
        runTestFromFile("elastic_tests/index_management.txt");
        runTestFromFile("elastic_tests/index_mapping.txt");
        runTestFromFile("elastic_tests/index_open_close.txt");
    }

    @Test
    public void testHintCommands() throws IOException {
        runTestFromFile("elastic_tests/hint_commands.txt");
    }

    @Test
    public void testCatCommands() throws IOException {
        runTestFromFile("elastic_tests/cat_commands.txt");
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
                    content.append(scanner.nextLine()).append("\n");
                }
            }
            parseCommand(content.toString());
        }
    }

    private void parseCommand(String command) {
        System.out.println("Testing command: " + command);
        ElasticLexer lexer = new ElasticLexer(CharStreams.fromString(command));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ConsoleErrorListener() {
            @Override
            public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, org.antlr.v4.runtime.RecognitionException e) {
                fail("Lexer error at line " + line + ":" + charPositionInLine + " " + msg);
            }
        });

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ElasticParser parser = new ElasticParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ConsoleErrorListener() {
            @Override
            public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, org.antlr.v4.runtime.RecognitionException e) {
                fail("Parser error at line " + line + ":" + charPositionInLine + " " + msg);
            }
        });

        ParseTree tree = parser.esCommands();
        if (parser.getNumberOfSyntaxErrors() > 0) {
            fail("Syntax errors found");
        }
    }
}
