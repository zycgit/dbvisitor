package net.hasor.dbvisitor.adapter.mongo.parser;
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

public class MongoParserTest {
    @Test
    public void testBasicCommands() throws IOException {
        runTestFromFile("mongo_tests/basic_commands.txt");
    }

    @Test
    public void testShellCommands() throws IOException {
        runTestFromFile("mongo_tests/shell_commands.txt");
    }

    @Test
    public void testChainCommands() throws IOException {
        runTestFromFile("mongo_tests/chain_commands.txt");
    }

    @Test
    public void testCrudCommands() throws IOException {
        runTestFromFile("mongo_tests/crud_commands.txt");
    }

    @Test
    public void testDbManagementCommands() throws IOException {
        runTestFromFile("mongo_tests/db_management_commands.txt");
    }

    @Test
    public void testCollectionCommands() throws IOException {
        runTestFromFile("mongo_tests/collection_commands.txt");
    }

    @Test
    public void testOperatorTests() throws IOException {
        runTestFromFile("mongo_tests/operator_tests.txt");
    }

    @Test
    public void testBsonTypes() throws IOException {
        runTestFromFile("mongo_tests/bson_types.txt");
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
        MongoLexer lexer = new MongoLexer(CharStreams.fromString(command));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ConsoleErrorListener() {
            @Override
            public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, org.antlr.v4.runtime.RecognitionException e) {
                fail("Lexer error at line " + line + ":" + charPositionInLine + " " + msg);
            }
        });

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MongoParser parser = new MongoParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ConsoleErrorListener() {
            @Override
            public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, org.antlr.v4.runtime.RecognitionException e) {
                fail("Parser error at line " + line + ":" + charPositionInLine + " " + msg);
            }
        });

        ParseTree tree = parser.mongoCommands();
        if (parser.getNumberOfSyntaxErrors() > 0) {
            fail("Syntax errors found");
        }
    }
}
