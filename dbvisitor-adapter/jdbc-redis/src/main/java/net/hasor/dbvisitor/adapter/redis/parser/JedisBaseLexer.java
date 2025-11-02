package net.hasor.dbvisitor.adapter.redis.parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public abstract class JedisBaseLexer extends Lexer {
    protected char separatorChar = '\n';

    public JedisBaseLexer(CharStream input) {
        super(input);
    }

    public void setSeparatorChar(char separatorChar) {
        this.separatorChar = separatorChar;
    }
}
