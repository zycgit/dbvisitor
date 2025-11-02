package net.hasor.dbvisitor.adapter.redis.parser;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

public abstract class JedisBaseParser extends Parser {
    protected char separatorChar = '\n';

    public JedisBaseParser(TokenStream input) {
        super(input);
    }

    public void setSeparatorChar(char separatorChar) {
        this.separatorChar = separatorChar;
    }
}
