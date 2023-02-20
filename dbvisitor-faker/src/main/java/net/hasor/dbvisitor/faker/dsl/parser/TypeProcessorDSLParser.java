// Generated from TypeProcessorDSLParser.g4 by ANTLR 4.9.3
package net.hasor.dbvisitor.faker.dsl.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TypeProcessorDSLParser extends Parser {
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
	public static final int
		RULE_rootInstSet = 0, RULE_defineInst = 1, RULE_defineConf = 2, RULE_typeSetInst = 3, 
		RULE_typeInst = 4, RULE_colTypeName = 5, RULE_colTypeConf = 6, RULE_flowName = 7, 
		RULE_anyValue = 8, RULE_funcCall = 9, RULE_baseValue = 10, RULE_extValue = 11, 
		RULE_envValue = 12, RULE_listValue = 13, RULE_objectValue = 14, RULE_objectItem = 15, 
		RULE_idStr = 16;
	private static String[] makeRuleNames() {
		return new String[] {
			"rootInstSet", "defineInst", "defineConf", "typeSetInst", "typeInst", 
			"colTypeName", "colTypeConf", "flowName", "anyValue", "funcCall", "baseValue", 
			"extValue", "envValue", "listValue", "objectValue", "objectItem", "idStr"
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

	@Override
	public String getGrammarFileName() { return "TypeProcessorDSLParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TypeProcessorDSLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class RootInstSetContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(TypeProcessorDSLParser.EOF, 0); }
		public DefineInstContext defineInst() {
			return getRuleContext(DefineInstContext.class,0);
		}
		public List<TypeSetInstContext> typeSetInst() {
			return getRuleContexts(TypeSetInstContext.class);
		}
		public TypeSetInstContext typeSetInst(int i) {
			return getRuleContext(TypeSetInstContext.class,i);
		}
		public RootInstSetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rootInstSet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterRootInstSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitRootInstSet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitRootInstSet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootInstSetContext rootInstSet() throws RecognitionException {
		RootInstSetContext _localctx = new RootInstSetContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_rootInstSet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(35);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEFINE) {
				{
				setState(34);
				defineInst();
				}
			}

			setState(40);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LSBT) {
				{
				{
				setState(37);
				typeSetInst();
				}
				}
				setState(42);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(43);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefineInstContext extends ParserRuleContext {
		public TerminalNode DEFINE() { return getToken(TypeProcessorDSLParser.DEFINE, 0); }
		public IdStrContext idStr() {
			return getRuleContext(IdStrContext.class,0);
		}
		public TerminalNode OCBR() { return getToken(TypeProcessorDSLParser.OCBR, 0); }
		public TerminalNode CCBR() { return getToken(TypeProcessorDSLParser.CCBR, 0); }
		public List<DefineConfContext> defineConf() {
			return getRuleContexts(DefineConfContext.class);
		}
		public DefineConfContext defineConf(int i) {
			return getRuleContext(DefineConfContext.class,i);
		}
		public DefineInstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defineInst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterDefineInst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitDefineInst(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitDefineInst(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefineInstContext defineInst() throws RecognitionException {
		DefineInstContext _localctx = new DefineInstContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_defineInst);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			match(DEFINE);
			setState(46);
			idStr();
			setState(55);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OCBR) {
				{
				setState(47);
				match(OCBR);
				setState(51);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << IDENTIFIER) | (1L << TYPE))) != 0)) {
					{
					{
					setState(48);
					defineConf();
					}
					}
					setState(53);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(54);
				match(CCBR);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefineConfContext extends ParserRuleContext {
		public IdStrContext idStr() {
			return getRuleContext(IdStrContext.class,0);
		}
		public TerminalNode SET() { return getToken(TypeProcessorDSLParser.SET, 0); }
		public AnyValueContext anyValue() {
			return getRuleContext(AnyValueContext.class,0);
		}
		public TerminalNode SEM() { return getToken(TypeProcessorDSLParser.SEM, 0); }
		public DefineConfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defineConf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterDefineConf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitDefineConf(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitDefineConf(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefineConfContext defineConf() throws RecognitionException {
		DefineConfContext _localctx = new DefineConfContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_defineConf);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57);
			idStr();
			setState(58);
			match(SET);
			setState(59);
			anyValue();
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEM) {
				{
				setState(60);
				match(SEM);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeSetInstContext extends ParserRuleContext {
		public List<TypeInstContext> typeInst() {
			return getRuleContexts(TypeInstContext.class);
		}
		public TypeInstContext typeInst(int i) {
			return getRuleContext(TypeInstContext.class,i);
		}
		public List<TerminalNode> SEM() { return getTokens(TypeProcessorDSLParser.SEM); }
		public TerminalNode SEM(int i) {
			return getToken(TypeProcessorDSLParser.SEM, i);
		}
		public TypeSetInstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeSetInst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterTypeSetInst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitTypeSetInst(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitTypeSetInst(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeSetInstContext typeSetInst() throws RecognitionException {
		TypeSetInstContext _localctx = new TypeSetInstContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_typeSetInst);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(67); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(63);
					typeInst();
					setState(65);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==SEM) {
						{
						setState(64);
						match(SEM);
						}
					}

					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(69); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TypeInstContext extends ParserRuleContext {
		public ColTypeNameContext colTypeName() {
			return getRuleContext(ColTypeNameContext.class,0);
		}
		public TerminalNode FOLLOW() { return getToken(TypeProcessorDSLParser.FOLLOW, 0); }
		public FlowNameContext flowName() {
			return getRuleContext(FlowNameContext.class,0);
		}
		public TerminalNode THROW() { return getToken(TypeProcessorDSLParser.THROW, 0); }
		public TerminalNode STRING() { return getToken(TypeProcessorDSLParser.STRING, 0); }
		public List<ColTypeConfContext> colTypeConf() {
			return getRuleContexts(ColTypeConfContext.class);
		}
		public ColTypeConfContext colTypeConf(int i) {
			return getRuleContext(ColTypeConfContext.class,i);
		}
		public TypeInstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeInst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterTypeInst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitTypeInst(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitTypeInst(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeInstContext typeInst() throws RecognitionException {
		TypeInstContext _localctx = new TypeInstContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_typeInst);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			colTypeName();
			setState(81);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
			case IDENTIFIER:
			case TYPE:
				{
				setState(73); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(72);
					colTypeConf();
					}
					}
					setState(75); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << IDENTIFIER) | (1L << TYPE))) != 0) );
				}
				break;
			case FOLLOW:
				{
				{
				setState(77);
				match(FOLLOW);
				setState(78);
				flowName();
				}
				}
				break;
			case THROW:
				{
				{
				setState(79);
				match(THROW);
				setState(80);
				match(STRING);
				}
				}
				break;
			case EOF:
			case SEM:
			case LSBT:
				break;
			default:
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColTypeNameContext extends ParserRuleContext {
		public TerminalNode LSBT() { return getToken(TypeProcessorDSLParser.LSBT, 0); }
		public TerminalNode RSBT() { return getToken(TypeProcessorDSLParser.RSBT, 0); }
		public TerminalNode ALL() { return getToken(TypeProcessorDSLParser.ALL, 0); }
		public IdStrContext idStr() {
			return getRuleContext(IdStrContext.class,0);
		}
		public ColTypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colTypeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterColTypeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitColTypeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitColTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColTypeNameContext colTypeName() throws RecognitionException {
		ColTypeNameContext _localctx = new ColTypeNameContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_colTypeName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			match(LSBT);
			setState(86);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				{
				setState(84);
				match(ALL);
				}
				break;
			case STRING:
			case IDENTIFIER:
			case TYPE:
				{
				setState(85);
				idStr();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(88);
			match(RSBT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColTypeConfContext extends ParserRuleContext {
		public IdStrContext idStr() {
			return getRuleContext(IdStrContext.class,0);
		}
		public AnyValueContext anyValue() {
			return getRuleContext(AnyValueContext.class,0);
		}
		public TerminalNode SET() { return getToken(TypeProcessorDSLParser.SET, 0); }
		public TerminalNode APPEND() { return getToken(TypeProcessorDSLParser.APPEND, 0); }
		public ColTypeConfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_colTypeConf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterColTypeConf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitColTypeConf(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitColTypeConf(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColTypeConfContext colTypeConf() throws RecognitionException {
		ColTypeConfContext _localctx = new ColTypeConfContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_colTypeConf);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			idStr();
			setState(91);
			_la = _input.LA(1);
			if ( !(_la==SET || _la==APPEND) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(92);
			anyValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FlowNameContext extends ParserRuleContext {
		public ColTypeNameContext colTypeName() {
			return getRuleContext(ColTypeNameContext.class,0);
		}
		public FlowNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flowName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterFlowName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitFlowName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitFlowName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlowNameContext flowName() throws RecognitionException {
		FlowNameContext _localctx = new FlowNameContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_flowName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			colTypeName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnyValueContext extends ParserRuleContext {
		public ExtValueContext extValue() {
			return getRuleContext(ExtValueContext.class,0);
		}
		public BaseValueContext baseValue() {
			return getRuleContext(BaseValueContext.class,0);
		}
		public ListValueContext listValue() {
			return getRuleContext(ListValueContext.class,0);
		}
		public ObjectValueContext objectValue() {
			return getRuleContext(ObjectValueContext.class,0);
		}
		public EnvValueContext envValue() {
			return getRuleContext(EnvValueContext.class,0);
		}
		public FuncCallContext funcCall() {
			return getRuleContext(FuncCallContext.class,0);
		}
		public AnyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterAnyValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitAnyValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitAnyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnyValueContext anyValue() throws RecognitionException {
		AnyValueContext _localctx = new AnyValueContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_anyValue);
		try {
			setState(102);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(96);
				extValue();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(97);
				baseValue();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(98);
				listValue();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(99);
				objectValue();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(100);
				envValue();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(101);
				funcCall();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FuncCallContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(TypeProcessorDSLParser.IDENTIFIER, 0); }
		public TerminalNode LBT() { return getToken(TypeProcessorDSLParser.LBT, 0); }
		public TerminalNode RBT() { return getToken(TypeProcessorDSLParser.RBT, 0); }
		public List<AnyValueContext> anyValue() {
			return getRuleContexts(AnyValueContext.class);
		}
		public AnyValueContext anyValue(int i) {
			return getRuleContext(AnyValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TypeProcessorDSLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TypeProcessorDSLParser.COMMA, i);
		}
		public FuncCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_funcCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterFuncCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitFuncCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitFuncCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FuncCallContext funcCall() throws RecognitionException {
		FuncCallContext _localctx = new FuncCallContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_funcCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			match(IDENTIFIER);
			setState(105);
			match(LBT);
			setState(114);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << NULL) | (1L << LSBT) | (1L << ENV) | (1L << OCBR) | (1L << STRING) | (1L << HEX_NUM) | (1L << OCT_NUM) | (1L << BIT_NUM) | (1L << SIZE) | (1L << INTEGER_NUM) | (1L << DECIMAL_NUM) | (1L << IDENTIFIER) | (1L << TYPE))) != 0)) {
				{
				setState(106);
				anyValue();
				setState(111);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(107);
					match(COMMA);
					setState(108);
					anyValue();
					}
					}
					setState(113);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(116);
			match(RBT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BaseValueContext extends ParserRuleContext {
		public BaseValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_baseValue; }
	 
		public BaseValueContext() { }
		public void copyFrom(BaseValueContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class StringValueContext extends BaseValueContext {
		public TerminalNode STRING() { return getToken(TypeProcessorDSLParser.STRING, 0); }
		public StringValueContext(BaseValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterStringValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitStringValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitStringValue(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BooleanValueContext extends BaseValueContext {
		public TerminalNode TRUE() { return getToken(TypeProcessorDSLParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(TypeProcessorDSLParser.FALSE, 0); }
		public BooleanValueContext(BaseValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterBooleanValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitBooleanValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitBooleanValue(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TypeValueContext extends BaseValueContext {
		public TerminalNode TYPE() { return getToken(TypeProcessorDSLParser.TYPE, 0); }
		public TypeValueContext(BaseValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterTypeValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitTypeValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitTypeValue(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumberValueContext extends BaseValueContext {
		public TerminalNode DECIMAL_NUM() { return getToken(TypeProcessorDSLParser.DECIMAL_NUM, 0); }
		public TerminalNode INTEGER_NUM() { return getToken(TypeProcessorDSLParser.INTEGER_NUM, 0); }
		public TerminalNode HEX_NUM() { return getToken(TypeProcessorDSLParser.HEX_NUM, 0); }
		public TerminalNode OCT_NUM() { return getToken(TypeProcessorDSLParser.OCT_NUM, 0); }
		public TerminalNode BIT_NUM() { return getToken(TypeProcessorDSLParser.BIT_NUM, 0); }
		public NumberValueContext(BaseValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterNumberValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitNumberValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitNumberValue(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NullValueContext extends BaseValueContext {
		public TerminalNode NULL() { return getToken(TypeProcessorDSLParser.NULL, 0); }
		public NullValueContext(BaseValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterNullValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitNullValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitNullValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BaseValueContext baseValue() throws RecognitionException {
		BaseValueContext _localctx = new BaseValueContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_baseValue);
		int _la;
		try {
			setState(123);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				_localctx = new StringValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(118);
				match(STRING);
				}
				break;
			case NULL:
				_localctx = new NullValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(119);
				match(NULL);
				}
				break;
			case TRUE:
			case FALSE:
				_localctx = new BooleanValueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(120);
				_la = _input.LA(1);
				if ( !(_la==TRUE || _la==FALSE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case HEX_NUM:
			case OCT_NUM:
			case BIT_NUM:
			case INTEGER_NUM:
			case DECIMAL_NUM:
				_localctx = new NumberValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(121);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << HEX_NUM) | (1L << OCT_NUM) | (1L << BIT_NUM) | (1L << INTEGER_NUM) | (1L << DECIMAL_NUM))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case TYPE:
				_localctx = new TypeValueContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(122);
				match(TYPE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtValueContext extends ParserRuleContext {
		public TerminalNode SIZE() { return getToken(TypeProcessorDSLParser.SIZE, 0); }
		public TerminalNode IDENTIFIER() { return getToken(TypeProcessorDSLParser.IDENTIFIER, 0); }
		public ExtValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterExtValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitExtValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitExtValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExtValueContext extValue() throws RecognitionException {
		ExtValueContext _localctx = new ExtValueContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_extValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			_la = _input.LA(1);
			if ( !(_la==SIZE || _la==IDENTIFIER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EnvValueContext extends ParserRuleContext {
		public TerminalNode ENV() { return getToken(TypeProcessorDSLParser.ENV, 0); }
		public IdStrContext idStr() {
			return getRuleContext(IdStrContext.class,0);
		}
		public TerminalNode CCBR() { return getToken(TypeProcessorDSLParser.CCBR, 0); }
		public EnvValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_envValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterEnvValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitEnvValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitEnvValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnvValueContext envValue() throws RecognitionException {
		EnvValueContext _localctx = new EnvValueContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_envValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(ENV);
			setState(128);
			idStr();
			setState(129);
			match(CCBR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListValueContext extends ParserRuleContext {
		public TerminalNode LSBT() { return getToken(TypeProcessorDSLParser.LSBT, 0); }
		public TerminalNode RSBT() { return getToken(TypeProcessorDSLParser.RSBT, 0); }
		public List<AnyValueContext> anyValue() {
			return getRuleContexts(AnyValueContext.class);
		}
		public AnyValueContext anyValue(int i) {
			return getRuleContext(AnyValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TypeProcessorDSLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TypeProcessorDSLParser.COMMA, i);
		}
		public ListValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterListValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitListValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitListValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListValueContext listValue() throws RecognitionException {
		ListValueContext _localctx = new ListValueContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_listValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			match(LSBT);
			setState(144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << NULL) | (1L << LSBT) | (1L << ENV) | (1L << OCBR) | (1L << STRING) | (1L << HEX_NUM) | (1L << OCT_NUM) | (1L << BIT_NUM) | (1L << SIZE) | (1L << INTEGER_NUM) | (1L << DECIMAL_NUM) | (1L << IDENTIFIER) | (1L << TYPE))) != 0)) {
				{
				setState(133); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(132);
					anyValue();
					}
					}
					setState(135); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << TRUE) | (1L << FALSE) | (1L << NULL) | (1L << LSBT) | (1L << ENV) | (1L << OCBR) | (1L << STRING) | (1L << HEX_NUM) | (1L << OCT_NUM) | (1L << BIT_NUM) | (1L << SIZE) | (1L << INTEGER_NUM) | (1L << DECIMAL_NUM) | (1L << IDENTIFIER) | (1L << TYPE))) != 0) );
				setState(141);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(137);
					match(COMMA);
					setState(138);
					anyValue();
					}
					}
					setState(143);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(146);
			match(RSBT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectValueContext extends ParserRuleContext {
		public TerminalNode OCBR() { return getToken(TypeProcessorDSLParser.OCBR, 0); }
		public TerminalNode CCBR() { return getToken(TypeProcessorDSLParser.CCBR, 0); }
		public List<ObjectItemContext> objectItem() {
			return getRuleContexts(ObjectItemContext.class);
		}
		public ObjectItemContext objectItem(int i) {
			return getRuleContext(ObjectItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TypeProcessorDSLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TypeProcessorDSLParser.COMMA, i);
		}
		public ObjectValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterObjectValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitObjectValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitObjectValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectValueContext objectValue() throws RecognitionException {
		ObjectValueContext _localctx = new ObjectValueContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_objectValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148);
			match(OCBR);
			setState(161);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << IDENTIFIER) | (1L << TYPE))) != 0)) {
				{
				setState(150); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(149);
					objectItem();
					}
					}
					setState(152); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << IDENTIFIER) | (1L << TYPE))) != 0) );
				setState(158);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(154);
					match(COMMA);
					setState(155);
					objectItem();
					}
					}
					setState(160);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(163);
			match(CCBR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectItemContext extends ParserRuleContext {
		public IdStrContext idStr() {
			return getRuleContext(IdStrContext.class,0);
		}
		public TerminalNode COLON() { return getToken(TypeProcessorDSLParser.COLON, 0); }
		public AnyValueContext anyValue() {
			return getRuleContext(AnyValueContext.class,0);
		}
		public ObjectItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterObjectItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitObjectItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitObjectItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectItemContext objectItem() throws RecognitionException {
		ObjectItemContext _localctx = new ObjectItemContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_objectItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			idStr();
			setState(166);
			match(COLON);
			setState(167);
			anyValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdStrContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(TypeProcessorDSLParser.STRING, 0); }
		public TerminalNode TYPE() { return getToken(TypeProcessorDSLParser.TYPE, 0); }
		public TerminalNode IDENTIFIER() { return getToken(TypeProcessorDSLParser.IDENTIFIER, 0); }
		public IdStrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idStr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).enterIdStr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeProcessorDSLParserListener ) ((TypeProcessorDSLParserListener)listener).exitIdStr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeProcessorDSLParserVisitor ) return ((TypeProcessorDSLParserVisitor<? extends T>)visitor).visitIdStr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdStrContext idStr() throws RecognitionException {
		IdStrContext _localctx = new IdStrContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_idStr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << IDENTIFIER) | (1L << TYPE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3#\u00ae\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\5\2&\n\2\3\2\7\2)\n\2\f\2\16\2,\13\2\3\2\3\2\3\3\3\3\3\3\3\3\7\3"+
		"\64\n\3\f\3\16\3\67\13\3\3\3\5\3:\n\3\3\4\3\4\3\4\3\4\5\4@\n\4\3\5\3\5"+
		"\5\5D\n\5\6\5F\n\5\r\5\16\5G\3\6\3\6\6\6L\n\6\r\6\16\6M\3\6\3\6\3\6\3"+
		"\6\5\6T\n\6\3\7\3\7\3\7\5\7Y\n\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\n\3"+
		"\n\3\n\3\n\3\n\3\n\5\ni\n\n\3\13\3\13\3\13\3\13\3\13\7\13p\n\13\f\13\16"+
		"\13s\13\13\5\13u\n\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\5\f~\n\f\3\r\3\r\3"+
		"\16\3\16\3\16\3\16\3\17\3\17\6\17\u0088\n\17\r\17\16\17\u0089\3\17\3\17"+
		"\7\17\u008e\n\17\f\17\16\17\u0091\13\17\5\17\u0093\n\17\3\17\3\17\3\20"+
		"\3\20\6\20\u0099\n\20\r\20\16\20\u009a\3\20\3\20\7\20\u009f\n\20\f\20"+
		"\16\20\u00a2\13\20\5\20\u00a4\n\20\3\20\3\20\3\21\3\21\3\21\3\21\3\22"+
		"\3\22\3\22\2\2\23\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"\2\7\3\2\16"+
		"\17\3\2\13\f\4\2\34\36 !\4\2\37\37\"\"\4\2\33\33\"#\2\u00b9\2%\3\2\2\2"+
		"\4/\3\2\2\2\6;\3\2\2\2\bE\3\2\2\2\nI\3\2\2\2\fU\3\2\2\2\16\\\3\2\2\2\20"+
		"`\3\2\2\2\22h\3\2\2\2\24j\3\2\2\2\26}\3\2\2\2\30\177\3\2\2\2\32\u0081"+
		"\3\2\2\2\34\u0085\3\2\2\2\36\u0096\3\2\2\2 \u00a7\3\2\2\2\"\u00ab\3\2"+
		"\2\2$&\5\4\3\2%$\3\2\2\2%&\3\2\2\2&*\3\2\2\2\')\5\b\5\2(\'\3\2\2\2),\3"+
		"\2\2\2*(\3\2\2\2*+\3\2\2\2+-\3\2\2\2,*\3\2\2\2-.\7\2\2\3.\3\3\2\2\2/\60"+
		"\7\b\2\2\609\5\"\22\2\61\65\7\31\2\2\62\64\5\6\4\2\63\62\3\2\2\2\64\67"+
		"\3\2\2\2\65\63\3\2\2\2\65\66\3\2\2\2\668\3\2\2\2\67\65\3\2\2\28:\7\32"+
		"\2\29\61\3\2\2\29:\3\2\2\2:\5\3\2\2\2;<\5\"\22\2<=\7\16\2\2=?\5\22\n\2"+
		">@\7\20\2\2?>\3\2\2\2?@\3\2\2\2@\7\3\2\2\2AC\5\n\6\2BD\7\20\2\2CB\3\2"+
		"\2\2CD\3\2\2\2DF\3\2\2\2EA\3\2\2\2FG\3\2\2\2GE\3\2\2\2GH\3\2\2\2H\t\3"+
		"\2\2\2IS\5\f\7\2JL\5\16\b\2KJ\3\2\2\2LM\3\2\2\2MK\3\2\2\2MN\3\2\2\2NT"+
		"\3\2\2\2OP\7\t\2\2PT\5\20\t\2QR\7\n\2\2RT\7\33\2\2SK\3\2\2\2SO\3\2\2\2"+
		"SQ\3\2\2\2ST\3\2\2\2T\13\3\2\2\2UX\7\26\2\2VY\7\21\2\2WY\5\"\22\2XV\3"+
		"\2\2\2XW\3\2\2\2YZ\3\2\2\2Z[\7\27\2\2[\r\3\2\2\2\\]\5\"\22\2]^\t\2\2\2"+
		"^_\5\22\n\2_\17\3\2\2\2`a\5\f\7\2a\21\3\2\2\2bi\5\30\r\2ci\5\26\f\2di"+
		"\5\34\17\2ei\5\36\20\2fi\5\32\16\2gi\5\24\13\2hb\3\2\2\2hc\3\2\2\2hd\3"+
		"\2\2\2he\3\2\2\2hf\3\2\2\2hg\3\2\2\2i\23\3\2\2\2jk\7\"\2\2kt\7\24\2\2"+
		"lq\5\22\n\2mn\7\23\2\2np\5\22\n\2om\3\2\2\2ps\3\2\2\2qo\3\2\2\2qr\3\2"+
		"\2\2ru\3\2\2\2sq\3\2\2\2tl\3\2\2\2tu\3\2\2\2uv\3\2\2\2vw\7\25\2\2w\25"+
		"\3\2\2\2x~\7\33\2\2y~\7\r\2\2z~\t\3\2\2{~\t\4\2\2|~\7#\2\2}x\3\2\2\2}"+
		"y\3\2\2\2}z\3\2\2\2}{\3\2\2\2}|\3\2\2\2~\27\3\2\2\2\177\u0080\t\5\2\2"+
		"\u0080\31\3\2\2\2\u0081\u0082\7\30\2\2\u0082\u0083\5\"\22\2\u0083\u0084"+
		"\7\32\2\2\u0084\33\3\2\2\2\u0085\u0092\7\26\2\2\u0086\u0088\5\22\n\2\u0087"+
		"\u0086\3\2\2\2\u0088\u0089\3\2\2\2\u0089\u0087\3\2\2\2\u0089\u008a\3\2"+
		"\2\2\u008a\u008f\3\2\2\2\u008b\u008c\7\23\2\2\u008c\u008e\5\22\n\2\u008d"+
		"\u008b\3\2\2\2\u008e\u0091\3\2\2\2\u008f\u008d\3\2\2\2\u008f\u0090\3\2"+
		"\2\2\u0090\u0093\3\2\2\2\u0091\u008f\3\2\2\2\u0092\u0087\3\2\2\2\u0092"+
		"\u0093\3\2\2\2\u0093\u0094\3\2\2\2\u0094\u0095\7\27\2\2\u0095\35\3\2\2"+
		"\2\u0096\u00a3\7\31\2\2\u0097\u0099\5 \21\2\u0098\u0097\3\2\2\2\u0099"+
		"\u009a\3\2\2\2\u009a\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b\u00a0\3\2"+
		"\2\2\u009c\u009d\7\23\2\2\u009d\u009f\5 \21\2\u009e\u009c\3\2\2\2\u009f"+
		"\u00a2\3\2\2\2\u00a0\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a4\3\2"+
		"\2\2\u00a2\u00a0\3\2\2\2\u00a3\u0098\3\2\2\2\u00a3\u00a4\3\2\2\2\u00a4"+
		"\u00a5\3\2\2\2\u00a5\u00a6\7\32\2\2\u00a6\37\3\2\2\2\u00a7\u00a8\5\"\22"+
		"\2\u00a8\u00a9\7\22\2\2\u00a9\u00aa\5\22\n\2\u00aa!\3\2\2\2\u00ab\u00ac"+
		"\t\6\2\2\u00ac#\3\2\2\2\26%*\659?CGMSXhqt}\u0089\u008f\u0092\u009a\u00a0"+
		"\u00a3";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}