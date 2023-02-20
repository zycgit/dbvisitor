// Generated from TypeProcessorDSLLexer.g4 by ANTLR 4.9.3
package net.hasor.dbvisitor.faker.dsl.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TypeProcessorDSLLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		WS=1, COMMENT1=2, COMMENT2=3, COMMENT3=4, EOL=5, DEFINE=6, FOLLOW=7, THROW=8, 
		TRUE=9, FALSE=10, NULL=11, SET=12, APPEND=13, SEM=14, ALL=15, COLON=16, 
		COMMA=17, LBT=18, RBT=19, LSBT=20, RSBT=21, ENV=22, OCBR=23, CCBR=24, 
		STRING=25, HEX_NUM=26, OCT_NUM=27, BIT_NUM=28, SIZE=29, INTEGER_NUM=30, 
		DECIMAL_NUM=31, IDENTIFIER=32, TYPE=33;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"WS", "COMMENT1", "COMMENT2", "COMMENT3", "EOL", "DEFINE", "FOLLOW", 
			"THROW", "TRUE", "FALSE", "NULL", "SET", "APPEND", "SEM", "ALL", "COLON", 
			"COMMA", "LBT", "RBT", "LSBT", "RSBT", "ENV", "OCBR", "CCBR", "STRING", 
			"TRANS", "UNICODE", "HEX", "HEX_NUM", "OCT_NUM", "BIT_NUM", "SIZE", "INTEGER_NUM", 
			"DECIMAL_NUM", "B", "KB", "MB", "IDENTIFIER", "TYPE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, "'define'", "'follow'", "'throw'", 
			"'true'", "'false'", "'null'", "'='", "'+='", "';'", "'*'", "':'", "','", 
			"'('", "')'", "'['", "']'", "'${'", "'{'", "'}'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "WS", "COMMENT1", "COMMENT2", "COMMENT3", "EOL", "DEFINE", "FOLLOW", 
			"THROW", "TRUE", "FALSE", "NULL", "SET", "APPEND", "SEM", "ALL", "COLON", 
			"COMMA", "LBT", "RBT", "LSBT", "RSBT", "ENV", "OCBR", "CCBR", "STRING", 
			"HEX_NUM", "OCT_NUM", "BIT_NUM", "SIZE", "INTEGER_NUM", "DECIMAL_NUM", 
			"IDENTIFIER", "TYPE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public TypeProcessorDSLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "TypeProcessorDSLLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2#\u0154\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\3\2\6\2S\n\2\r\2\16"+
		"\2T\3\2\3\2\3\3\3\3\3\3\3\3\7\3]\n\3\f\3\16\3`\13\3\3\3\5\3c\n\3\3\3\3"+
		"\3\3\4\3\4\7\4i\n\4\f\4\16\4l\13\4\3\4\5\4o\n\4\3\4\3\4\3\5\3\5\3\5\3"+
		"\5\7\5w\n\5\f\5\16\5z\13\5\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3"+
		"\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\16"+
		"\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24"+
		"\3\25\3\25\3\26\3\26\3\27\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\32"+
		"\3\32\3\32\7\32\u00c6\n\32\f\32\16\32\u00c9\13\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\7\32\u00d1\n\32\f\32\16\32\u00d4\13\32\3\32\3\32\3\32\3\32"+
		"\3\32\3\32\7\32\u00dc\n\32\f\32\16\32\u00df\13\32\3\32\5\32\u00e2\n\32"+
		"\3\33\3\33\3\33\5\33\u00e7\n\33\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35"+
		"\3\36\3\36\3\36\6\36\u00f4\n\36\r\36\16\36\u00f5\3\37\3\37\3\37\6\37\u00fb"+
		"\n\37\r\37\16\37\u00fc\3 \3 \3 \6 \u0102\n \r \16 \u0103\3!\3!\7!\u0108"+
		"\n!\f!\16!\u010b\13!\3!\3!\3!\5!\u0110\n!\3\"\5\"\u0113\n\"\3\"\6\"\u0116"+
		"\n\"\r\"\16\"\u0117\3#\5#\u011b\n#\3#\7#\u011e\n#\f#\16#\u0121\13#\3#"+
		"\3#\6#\u0125\n#\r#\16#\u0126\3#\6#\u012a\n#\r#\16#\u012b\5#\u012e\n#\3"+
		"#\3#\5#\u0132\n#\3#\3#\7#\u0136\n#\f#\16#\u0139\13#\5#\u013b\n#\3$\3$"+
		"\3%\3%\3&\3&\3\'\3\'\7\'\u0145\n\'\f\'\16\'\u0148\13\'\3(\3(\3(\6(\u014d"+
		"\n(\r(\16(\u014e\5(\u0151\n(\3(\3(\3x\2)\3\3\5\4\7\5\t\6\13\7\r\b\17\t"+
		"\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27"+
		"-\30/\31\61\32\63\33\65\2\67\29\2;\34=\35?\36A\37C E!G\2I\2K\2M\"O#\3"+
		"\2\24\5\2\13\f\16\17\"\"\4\2\f\f\17\17\4\2\f\f\16\17\5\2\f\f\17\17$$\5"+
		"\2\f\f\17\17))\13\2$$))\61\61^^ddhhppttvv\5\2\62;CHch\4\2ZZzz\4\2QQqq"+
		"\3\2\629\4\2DDdd\3\2\62\63\3\2\63;\3\2\62;\4\2GGgg\4\2--//\5\2C\\aac|"+
		"\6\2\62;C\\aac|\2\u0172\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2"+
		"\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25"+
		"\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2"+
		"\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2"+
		"\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2;\3\2\2\2\2=\3\2\2"+
		"\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\3"+
		"R\3\2\2\2\5X\3\2\2\2\7f\3\2\2\2\tr\3\2\2\2\13~\3\2\2\2\r\u0080\3\2\2\2"+
		"\17\u0087\3\2\2\2\21\u008e\3\2\2\2\23\u0094\3\2\2\2\25\u0099\3\2\2\2\27"+
		"\u009f\3\2\2\2\31\u00a4\3\2\2\2\33\u00a6\3\2\2\2\35\u00a9\3\2\2\2\37\u00ab"+
		"\3\2\2\2!\u00ad\3\2\2\2#\u00af\3\2\2\2%\u00b1\3\2\2\2\'\u00b3\3\2\2\2"+
		")\u00b5\3\2\2\2+\u00b7\3\2\2\2-\u00b9\3\2\2\2/\u00bc\3\2\2\2\61\u00be"+
		"\3\2\2\2\63\u00e1\3\2\2\2\65\u00e3\3\2\2\2\67\u00e8\3\2\2\29\u00ee\3\2"+
		"\2\2;\u00f0\3\2\2\2=\u00f7\3\2\2\2?\u00fe\3\2\2\2A\u0105\3\2\2\2C\u0112"+
		"\3\2\2\2E\u011a\3\2\2\2G\u013c\3\2\2\2I\u013e\3\2\2\2K\u0140\3\2\2\2M"+
		"\u0142\3\2\2\2O\u0150\3\2\2\2QS\t\2\2\2RQ\3\2\2\2ST\3\2\2\2TR\3\2\2\2"+
		"TU\3\2\2\2UV\3\2\2\2VW\b\2\2\2W\4\3\2\2\2XY\7\61\2\2YZ\7\61\2\2Z^\3\2"+
		"\2\2[]\n\3\2\2\\[\3\2\2\2]`\3\2\2\2^\\\3\2\2\2^_\3\2\2\2_b\3\2\2\2`^\3"+
		"\2\2\2ac\5\13\6\2ba\3\2\2\2bc\3\2\2\2cd\3\2\2\2de\b\3\2\2e\6\3\2\2\2f"+
		"j\7%\2\2gi\n\3\2\2hg\3\2\2\2il\3\2\2\2jh\3\2\2\2jk\3\2\2\2kn\3\2\2\2l"+
		"j\3\2\2\2mo\5\13\6\2nm\3\2\2\2no\3\2\2\2op\3\2\2\2pq\b\4\2\2q\b\3\2\2"+
		"\2rs\7\61\2\2st\7,\2\2tx\3\2\2\2uw\13\2\2\2vu\3\2\2\2wz\3\2\2\2xy\3\2"+
		"\2\2xv\3\2\2\2y{\3\2\2\2zx\3\2\2\2{|\7,\2\2|}\7\61\2\2}\n\3\2\2\2~\177"+
		"\t\4\2\2\177\f\3\2\2\2\u0080\u0081\7f\2\2\u0081\u0082\7g\2\2\u0082\u0083"+
		"\7h\2\2\u0083\u0084\7k\2\2\u0084\u0085\7p\2\2\u0085\u0086\7g\2\2\u0086"+
		"\16\3\2\2\2\u0087\u0088\7h\2\2\u0088\u0089\7q\2\2\u0089\u008a\7n\2\2\u008a"+
		"\u008b\7n\2\2\u008b\u008c\7q\2\2\u008c\u008d\7y\2\2\u008d\20\3\2\2\2\u008e"+
		"\u008f\7v\2\2\u008f\u0090\7j\2\2\u0090\u0091\7t\2\2\u0091\u0092\7q\2\2"+
		"\u0092\u0093\7y\2\2\u0093\22\3\2\2\2\u0094\u0095\7v\2\2\u0095\u0096\7"+
		"t\2\2\u0096\u0097\7w\2\2\u0097\u0098\7g\2\2\u0098\24\3\2\2\2\u0099\u009a"+
		"\7h\2\2\u009a\u009b\7c\2\2\u009b\u009c\7n\2\2\u009c\u009d\7u\2\2\u009d"+
		"\u009e\7g\2\2\u009e\26\3\2\2\2\u009f\u00a0\7p\2\2\u00a0\u00a1\7w\2\2\u00a1"+
		"\u00a2\7n\2\2\u00a2\u00a3\7n\2\2\u00a3\30\3\2\2\2\u00a4\u00a5\7?\2\2\u00a5"+
		"\32\3\2\2\2\u00a6\u00a7\7-\2\2\u00a7\u00a8\7?\2\2\u00a8\34\3\2\2\2\u00a9"+
		"\u00aa\7=\2\2\u00aa\36\3\2\2\2\u00ab\u00ac\7,\2\2\u00ac \3\2\2\2\u00ad"+
		"\u00ae\7<\2\2\u00ae\"\3\2\2\2\u00af\u00b0\7.\2\2\u00b0$\3\2\2\2\u00b1"+
		"\u00b2\7*\2\2\u00b2&\3\2\2\2\u00b3\u00b4\7+\2\2\u00b4(\3\2\2\2\u00b5\u00b6"+
		"\7]\2\2\u00b6*\3\2\2\2\u00b7\u00b8\7_\2\2\u00b8,\3\2\2\2\u00b9\u00ba\7"+
		"&\2\2\u00ba\u00bb\7}\2\2\u00bb.\3\2\2\2\u00bc\u00bd\7}\2\2\u00bd\60\3"+
		"\2\2\2\u00be\u00bf\7\177\2\2\u00bf\62\3\2\2\2\u00c0\u00c7\7$\2\2\u00c1"+
		"\u00c6\n\5\2\2\u00c2\u00c3\7$\2\2\u00c3\u00c6\7$\2\2\u00c4\u00c6\5\65"+
		"\33\2\u00c5\u00c1\3\2\2\2\u00c5\u00c2\3\2\2\2\u00c5\u00c4\3\2\2\2\u00c6"+
		"\u00c9\3\2\2\2\u00c7\u00c5\3\2\2\2\u00c7\u00c8\3\2\2\2\u00c8\u00ca\3\2"+
		"\2\2\u00c9\u00c7\3\2\2\2\u00ca\u00e2\7$\2\2\u00cb\u00d2\7)\2\2\u00cc\u00d1"+
		"\n\6\2\2\u00cd\u00ce\7)\2\2\u00ce\u00d1\7)\2\2\u00cf\u00d1\5\65\33\2\u00d0"+
		"\u00cc\3\2\2\2\u00d0\u00cd\3\2\2\2\u00d0\u00cf\3\2\2\2\u00d1\u00d4\3\2"+
		"\2\2\u00d2\u00d0\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3\u00d5\3\2\2\2\u00d4"+
		"\u00d2\3\2\2\2\u00d5\u00e2\7)\2\2\u00d6\u00dd\7b\2\2\u00d7\u00dc\n\6\2"+
		"\2\u00d8\u00d9\7b\2\2\u00d9\u00dc\7b\2\2\u00da\u00dc\5\65\33\2\u00db\u00d7"+
		"\3\2\2\2\u00db\u00d8\3\2\2\2\u00db\u00da\3\2\2\2\u00dc\u00df\3\2\2\2\u00dd"+
		"\u00db\3\2\2\2\u00dd\u00de\3\2\2\2\u00de\u00e0\3\2\2\2\u00df\u00dd\3\2"+
		"\2\2\u00e0\u00e2\7b\2\2\u00e1\u00c0\3\2\2\2\u00e1\u00cb\3\2\2\2\u00e1"+
		"\u00d6\3\2\2\2\u00e2\64\3\2\2\2\u00e3\u00e6\7^\2\2\u00e4\u00e7\t\7\2\2"+
		"\u00e5\u00e7\5\67\34\2\u00e6\u00e4\3\2\2\2\u00e6\u00e5\3\2\2\2\u00e7\66"+
		"\3\2\2\2\u00e8\u00e9\7w\2\2\u00e9\u00ea\59\35\2\u00ea\u00eb\59\35\2\u00eb"+
		"\u00ec\59\35\2\u00ec\u00ed\59\35\2\u00ed8\3\2\2\2\u00ee\u00ef\t\b\2\2"+
		"\u00ef:\3\2\2\2\u00f0\u00f1\7\62\2\2\u00f1\u00f3\t\t\2\2\u00f2\u00f4\t"+
		"\b\2\2\u00f3\u00f2\3\2\2\2\u00f4\u00f5\3\2\2\2\u00f5\u00f3\3\2\2\2\u00f5"+
		"\u00f6\3\2\2\2\u00f6<\3\2\2\2\u00f7\u00f8\7\62\2\2\u00f8\u00fa\t\n\2\2"+
		"\u00f9\u00fb\t\13\2\2\u00fa\u00f9\3\2\2\2\u00fb\u00fc\3\2\2\2\u00fc\u00fa"+
		"\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd>\3\2\2\2\u00fe\u00ff\7\62\2\2\u00ff"+
		"\u0101\t\f\2\2\u0100\u0102\t\r\2\2\u0101\u0100\3\2\2\2\u0102\u0103\3\2"+
		"\2\2\u0103\u0101\3\2\2\2\u0103\u0104\3\2\2\2\u0104@\3\2\2\2\u0105\u0109"+
		"\t\16\2\2\u0106\u0108\t\17\2\2\u0107\u0106\3\2\2\2\u0108\u010b\3\2\2\2"+
		"\u0109\u0107\3\2\2\2\u0109\u010a\3\2\2\2\u010a\u010f\3\2\2\2\u010b\u0109"+
		"\3\2\2\2\u010c\u0110\5G$\2\u010d\u0110\5I%\2\u010e\u0110\5K&\2\u010f\u010c"+
		"\3\2\2\2\u010f\u010d\3\2\2\2\u010f\u010e\3\2\2\2\u0110B\3\2\2\2\u0111"+
		"\u0113\7/\2\2\u0112\u0111\3\2\2\2\u0112\u0113\3\2\2\2\u0113\u0115\3\2"+
		"\2\2\u0114\u0116\t\17\2\2\u0115\u0114\3\2\2\2\u0116\u0117\3\2\2\2\u0117"+
		"\u0115\3\2\2\2\u0117\u0118\3\2\2\2\u0118D\3\2\2\2\u0119\u011b\7/\2\2\u011a"+
		"\u0119\3\2\2\2\u011a\u011b\3\2\2\2\u011b\u012d\3\2\2\2\u011c\u011e\t\17"+
		"\2\2\u011d\u011c\3\2\2\2\u011e\u0121\3\2\2\2\u011f\u011d\3\2\2\2\u011f"+
		"\u0120\3\2\2\2\u0120\u0122\3\2\2\2\u0121\u011f\3\2\2\2\u0122\u0124\7\60"+
		"\2\2\u0123\u0125\t\17\2\2\u0124\u0123\3\2\2\2\u0125\u0126\3\2\2\2\u0126"+
		"\u0124\3\2\2\2\u0126\u0127\3\2\2\2\u0127\u012e\3\2\2\2\u0128\u012a\t\16"+
		"\2\2\u0129\u0128\3\2\2\2\u012a\u012b\3\2\2\2\u012b\u0129\3\2\2\2\u012b"+
		"\u012c\3\2\2\2\u012c\u012e\3\2\2\2\u012d\u011f\3\2\2\2\u012d\u0129\3\2"+
		"\2\2\u012e\u013a\3\2\2\2\u012f\u0131\t\20\2\2\u0130\u0132\t\21\2\2\u0131"+
		"\u0130\3\2\2\2\u0131\u0132\3\2\2\2\u0132\u0133\3\2\2\2\u0133\u0137\t\16"+
		"\2\2\u0134\u0136\t\17\2\2\u0135\u0134\3\2\2\2\u0136\u0139\3\2\2\2\u0137"+
		"\u0135\3\2\2\2\u0137\u0138\3\2\2\2\u0138\u013b\3\2\2\2\u0139\u0137\3\2"+
		"\2\2\u013a\u012f\3\2\2\2\u013a\u013b\3\2\2\2\u013bF\3\2\2\2\u013c\u013d"+
		"\7d\2\2\u013dH\3\2\2\2\u013e\u013f\7m\2\2\u013fJ\3\2\2\2\u0140\u0141\7"+
		"o\2\2\u0141L\3\2\2\2\u0142\u0146\t\22\2\2\u0143\u0145\t\23\2\2\u0144\u0143"+
		"\3\2\2\2\u0145\u0148\3\2\2\2\u0146\u0144\3\2\2\2\u0146\u0147\3\2\2\2\u0147"+
		"N\3\2\2\2\u0148\u0146\3\2\2\2\u0149\u014a\5M\'\2\u014a\u014b\7\60\2\2"+
		"\u014b\u014d\3\2\2\2\u014c\u0149\3\2\2\2\u014d\u014e\3\2\2\2\u014e\u014c"+
		"\3\2\2\2\u014e\u014f\3\2\2\2\u014f\u0151\3\2\2\2\u0150\u014c\3\2\2\2\u0150"+
		"\u0151\3\2\2\2\u0151\u0152\3\2\2\2\u0152\u0153\5M\'\2\u0153P\3\2\2\2#"+
		"\2T^bjnx\u00c5\u00c7\u00d0\u00d2\u00db\u00dd\u00e1\u00e6\u00f5\u00fc\u0103"+
		"\u0109\u010f\u0112\u0117\u011a\u011f\u0126\u012b\u012d\u0131\u0137\u013a"+
		"\u0146\u014e\u0150\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}