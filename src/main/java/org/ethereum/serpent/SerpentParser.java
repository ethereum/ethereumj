// Generated from E:\WorkingArea\ethereum\ethereumj\src\main\java\org\ethereum\serpent\Serpent.g4 by ANTLR 4.1
package org.ethereum.serpent;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SerpentParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__20=1, T__19=2, T__18=3, T__17=4, T__16=5, T__15=6, T__14=7, T__13=8, 
		T__12=9, T__11=10, T__10=11, T__9=12, T__8=13, T__7=14, T__6=15, T__5=16, 
		T__4=17, T__3=18, T__2=19, T__1=20, T__0=21, OP_EX_OR=22, OP_LOG_AND=23, 
		OP_LOG_OR=24, OP_NOT=25, EQ_OP=26, NL=27, WS=28, LINE_COMMENT=29, INT=30, 
		VAR=31, OP_ADD=32, OP_MUL=33, OP_REL=34, OP_EQ=35, OP_AND=36, OP_IN_OR=37, 
		HEX_DIGIT=38, HEX_NUMBER=39, INDENT=40, DEDENT=41;
	public static final String[] tokenNames = {
		"<INVALID>", "'msg.datasize'", "'block.coinbase'", "')'", "'tx.gasprice'", 
		"'else:'", "'tx.gas'", "'tx.origin'", "'while'", "':'", "'('", "'if'", 
		"'elif'", "'return'", "'msg.sender'", "'block.timestamp'", "'msg.value'", 
		"'contract.balance'", "'block.gaslimit'", "'block.prevhash'", "'block.number'", 
		"'block.difficulty'", "'xor'", "OP_LOG_AND", "OP_LOG_OR", "OP_NOT", "'='", 
		"NL", "WS", "LINE_COMMENT", "INT", "VAR", "OP_ADD", "OP_MUL", "OP_REL", 
		"OP_EQ", "'&'", "'|'", "HEX_DIGIT", "HEX_NUMBER", "INDENT", "DEDENT"
	};
	public static final int
		RULE_parse = 0, RULE_block = 1, RULE_if_elif_else_stmt = 2, RULE_while_stmt = 3, 
		RULE_special_func = 4, RULE_msg_datasize = 5, RULE_msg_sender = 6, RULE_msg_value = 7, 
		RULE_tx_gasprice = 8, RULE_tx_origin = 9, RULE_tx_gas = 10, RULE_contract_balance = 11, 
		RULE_block_prevhash = 12, RULE_block_coinbase = 13, RULE_block_timestamp = 14, 
		RULE_block_number = 15, RULE_block_difficulty = 16, RULE_block_gaslimit = 17, 
		RULE_assign = 18, RULE_mul_expr = 19, RULE_add_expr = 20, RULE_rel_exp = 21, 
		RULE_eq_exp = 22, RULE_and_exp = 23, RULE_ex_or_exp = 24, RULE_in_or_exp = 25, 
		RULE_log_and_exp = 26, RULE_log_or_exp = 27, RULE_expression = 28, RULE_condition = 29, 
		RULE_int_val = 30, RULE_hex_num = 31, RULE_ret_func = 32, RULE_get_var = 33;
	public static final String[] ruleNames = {
		"parse", "block", "if_elif_else_stmt", "while_stmt", "special_func", "msg_datasize", 
		"msg_sender", "msg_value", "tx_gasprice", "tx_origin", "tx_gas", "contract_balance", 
		"block_prevhash", "block_coinbase", "block_timestamp", "block_number", 
		"block_difficulty", "block_gaslimit", "assign", "mul_expr", "add_expr", 
		"rel_exp", "eq_exp", "and_exp", "ex_or_exp", "in_or_exp", "log_and_exp", 
		"log_or_exp", "expression", "condition", "int_val", "hex_num", "ret_func", 
		"get_var"
	};

	@Override
	public String getGrammarFileName() { return "Serpent.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public SerpentParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ParseContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(SerpentParser.EOF, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ParseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitParse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitParse(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParseContext parse() throws RecognitionException {
		ParseContext _localctx = new ParseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68); block();
			setState(69); match(EOF);
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

	public static class BlockContext extends ParserRuleContext {
		public Ret_funcContext ret_func(int i) {
			return getRuleContext(Ret_funcContext.class,i);
		}
		public List<Special_funcContext> special_func() {
			return getRuleContexts(Special_funcContext.class);
		}
		public Special_funcContext special_func(int i) {
			return getRuleContext(Special_funcContext.class,i);
		}
		public While_stmtContext while_stmt(int i) {
			return getRuleContext(While_stmtContext.class,i);
		}
		public List<AssignContext> assign() {
			return getRuleContexts(AssignContext.class);
		}
		public List<If_elif_else_stmtContext> if_elif_else_stmt() {
			return getRuleContexts(If_elif_else_stmtContext.class);
		}
		public AssignContext assign(int i) {
			return getRuleContext(AssignContext.class,i);
		}
		public List<Ret_funcContext> ret_func() {
			return getRuleContexts(Ret_funcContext.class);
		}
		public If_elif_else_stmtContext if_elif_else_stmt(int i) {
			return getRuleContext(If_elif_else_stmtContext.class,i);
		}
		public List<While_stmtContext> while_stmt() {
			return getRuleContexts(While_stmtContext.class);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 1) | (1L << 2) | (1L << 4) | (1L << 6) | (1L << 7) | (1L << 8) | (1L << 11) | (1L << 13) | (1L << 14) | (1L << 15) | (1L << 16) | (1L << 17) | (1L << 18) | (1L << 19) | (1L << 20) | (1L << 21) | (1L << VAR))) != 0)) {
				{
				setState(76);
				switch (_input.LA(1)) {
				case VAR:
					{
					setState(71); assign();
					}
					break;
				case 1:
				case 2:
				case 4:
				case 6:
				case 7:
				case 14:
				case 15:
				case 16:
				case 17:
				case 18:
				case 19:
				case 20:
				case 21:
					{
					setState(72); special_func();
					}
					break;
				case 11:
					{
					setState(73); if_elif_else_stmt();
					}
					break;
				case 8:
					{
					setState(74); while_stmt();
					}
					break;
				case 13:
					{
					setState(75); ret_func();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(80);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class If_elif_else_stmtContext extends ParserRuleContext {
		public List<TerminalNode> INDENT() { return getTokens(SerpentParser.INDENT); }
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public TerminalNode INDENT(int i) {
			return getToken(SerpentParser.INDENT, i);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public BlockContext block(int i) {
			return getRuleContext(BlockContext.class,i);
		}
		public List<TerminalNode> DEDENT() { return getTokens(SerpentParser.DEDENT); }
		public List<BlockContext> block() {
			return getRuleContexts(BlockContext.class);
		}
		public TerminalNode DEDENT(int i) {
			return getToken(SerpentParser.DEDENT, i);
		}
		public If_elif_else_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_if_elif_else_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterIf_elif_else_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitIf_elif_else_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitIf_elif_else_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final If_elif_else_stmtContext if_elif_else_stmt() throws RecognitionException {
		If_elif_else_stmtContext _localctx = new If_elif_else_stmtContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_if_elif_else_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81); match(11);
			setState(82); condition();
			setState(83); match(9);
			setState(84); match(INDENT);
			setState(85); block();
			setState(86); match(DEDENT);
			setState(96);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==12) {
				{
				{
				setState(87); match(12);
				setState(88); condition();
				setState(89); match(9);
				setState(90); match(INDENT);
				setState(91); block();
				setState(92); match(DEDENT);
				}
				}
				setState(98);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(104);
			_la = _input.LA(1);
			if (_la==5) {
				{
				setState(99); match(5);
				setState(100); match(INDENT);
				setState(101); block();
				setState(102); match(DEDENT);
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

	public static class While_stmtContext extends ParserRuleContext {
		public TerminalNode INDENT() { return getToken(SerpentParser.INDENT, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public TerminalNode DEDENT() { return getToken(SerpentParser.DEDENT, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public While_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_while_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterWhile_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitWhile_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitWhile_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final While_stmtContext while_stmt() throws RecognitionException {
		While_stmtContext _localctx = new While_stmtContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_while_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106); match(8);
			setState(107); condition();
			setState(108); match(9);
			setState(109); match(INDENT);
			setState(110); block();
			setState(111); match(DEDENT);
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

	public static class Special_funcContext extends ParserRuleContext {
		public Block_numberContext block_number() {
			return getRuleContext(Block_numberContext.class,0);
		}
		public Block_gaslimitContext block_gaslimit() {
			return getRuleContext(Block_gaslimitContext.class,0);
		}
		public Block_timestampContext block_timestamp() {
			return getRuleContext(Block_timestampContext.class,0);
		}
		public Tx_gaspriceContext tx_gasprice() {
			return getRuleContext(Tx_gaspriceContext.class,0);
		}
		public Msg_valueContext msg_value() {
			return getRuleContext(Msg_valueContext.class,0);
		}
		public Block_coinbaseContext block_coinbase() {
			return getRuleContext(Block_coinbaseContext.class,0);
		}
		public Tx_gasContext tx_gas() {
			return getRuleContext(Tx_gasContext.class,0);
		}
		public Tx_originContext tx_origin() {
			return getRuleContext(Tx_originContext.class,0);
		}
		public Block_prevhashContext block_prevhash() {
			return getRuleContext(Block_prevhashContext.class,0);
		}
		public Contract_balanceContext contract_balance() {
			return getRuleContext(Contract_balanceContext.class,0);
		}
		public Msg_senderContext msg_sender() {
			return getRuleContext(Msg_senderContext.class,0);
		}
		public Block_difficultyContext block_difficulty() {
			return getRuleContext(Block_difficultyContext.class,0);
		}
		public Msg_datasizeContext msg_datasize() {
			return getRuleContext(Msg_datasizeContext.class,0);
		}
		public Special_funcContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_special_func; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterSpecial_func(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitSpecial_func(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitSpecial_func(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Special_funcContext special_func() throws RecognitionException {
		Special_funcContext _localctx = new Special_funcContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_special_func);
		try {
			setState(126);
			switch (_input.LA(1)) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(113); msg_datasize();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 2);
				{
				setState(114); msg_sender();
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 3);
				{
				setState(115); msg_value();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(116); tx_gasprice();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 5);
				{
				setState(117); tx_origin();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(118); tx_gas();
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 7);
				{
				setState(119); contract_balance();
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 8);
				{
				setState(120); block_prevhash();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 9);
				{
				setState(121); block_coinbase();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 10);
				{
				setState(122); block_timestamp();
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 11);
				{
				setState(123); block_number();
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 12);
				{
				setState(124); block_difficulty();
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 13);
				{
				setState(125); block_gaslimit();
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

	public static class Msg_datasizeContext extends ParserRuleContext {
		public Msg_datasizeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_msg_datasize; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterMsg_datasize(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitMsg_datasize(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitMsg_datasize(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Msg_datasizeContext msg_datasize() throws RecognitionException {
		Msg_datasizeContext _localctx = new Msg_datasizeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_msg_datasize);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128); match(1);
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

	public static class Msg_senderContext extends ParserRuleContext {
		public Msg_senderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_msg_sender; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterMsg_sender(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitMsg_sender(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitMsg_sender(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Msg_senderContext msg_sender() throws RecognitionException {
		Msg_senderContext _localctx = new Msg_senderContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_msg_sender);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130); match(14);
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

	public static class Msg_valueContext extends ParserRuleContext {
		public Msg_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_msg_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterMsg_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitMsg_value(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitMsg_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Msg_valueContext msg_value() throws RecognitionException {
		Msg_valueContext _localctx = new Msg_valueContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_msg_value);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132); match(16);
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

	public static class Tx_gaspriceContext extends ParserRuleContext {
		public Tx_gaspriceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tx_gasprice; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterTx_gasprice(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitTx_gasprice(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitTx_gasprice(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Tx_gaspriceContext tx_gasprice() throws RecognitionException {
		Tx_gaspriceContext _localctx = new Tx_gaspriceContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_tx_gasprice);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134); match(4);
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

	public static class Tx_originContext extends ParserRuleContext {
		public Tx_originContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tx_origin; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterTx_origin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitTx_origin(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitTx_origin(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Tx_originContext tx_origin() throws RecognitionException {
		Tx_originContext _localctx = new Tx_originContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_tx_origin);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136); match(7);
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

	public static class Tx_gasContext extends ParserRuleContext {
		public Tx_gasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tx_gas; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterTx_gas(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitTx_gas(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitTx_gas(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Tx_gasContext tx_gas() throws RecognitionException {
		Tx_gasContext _localctx = new Tx_gasContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_tx_gas);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138); match(6);
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

	public static class Contract_balanceContext extends ParserRuleContext {
		public Contract_balanceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_contract_balance; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterContract_balance(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitContract_balance(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitContract_balance(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Contract_balanceContext contract_balance() throws RecognitionException {
		Contract_balanceContext _localctx = new Contract_balanceContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_contract_balance);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140); match(17);
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

	public static class Block_prevhashContext extends ParserRuleContext {
		public Block_prevhashContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_prevhash; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterBlock_prevhash(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitBlock_prevhash(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitBlock_prevhash(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Block_prevhashContext block_prevhash() throws RecognitionException {
		Block_prevhashContext _localctx = new Block_prevhashContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_block_prevhash);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(142); match(19);
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

	public static class Block_coinbaseContext extends ParserRuleContext {
		public Block_coinbaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_coinbase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterBlock_coinbase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitBlock_coinbase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitBlock_coinbase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Block_coinbaseContext block_coinbase() throws RecognitionException {
		Block_coinbaseContext _localctx = new Block_coinbaseContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_block_coinbase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144); match(2);
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

	public static class Block_timestampContext extends ParserRuleContext {
		public Block_timestampContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_timestamp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterBlock_timestamp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitBlock_timestamp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitBlock_timestamp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Block_timestampContext block_timestamp() throws RecognitionException {
		Block_timestampContext _localctx = new Block_timestampContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_block_timestamp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146); match(15);
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

	public static class Block_numberContext extends ParserRuleContext {
		public Block_numberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterBlock_number(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitBlock_number(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitBlock_number(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Block_numberContext block_number() throws RecognitionException {
		Block_numberContext _localctx = new Block_numberContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_block_number);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148); match(20);
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

	public static class Block_difficultyContext extends ParserRuleContext {
		public Block_difficultyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_difficulty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterBlock_difficulty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitBlock_difficulty(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitBlock_difficulty(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Block_difficultyContext block_difficulty() throws RecognitionException {
		Block_difficultyContext _localctx = new Block_difficultyContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_block_difficulty);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150); match(21);
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

	public static class Block_gaslimitContext extends ParserRuleContext {
		public Block_gaslimitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_gaslimit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterBlock_gaslimit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitBlock_gaslimit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitBlock_gaslimit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Block_gaslimitContext block_gaslimit() throws RecognitionException {
		Block_gaslimitContext _localctx = new Block_gaslimitContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_block_gaslimit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152); match(18);
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

	public static class AssignContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public TerminalNode VAR() { return getToken(SerpentParser.VAR, 0); }
		public TerminalNode EQ_OP() { return getToken(SerpentParser.EQ_OP, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AssignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterAssign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitAssign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitAssign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignContext assign() throws RecognitionException {
		AssignContext _localctx = new AssignContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154); match(VAR);
			setState(155); match(EQ_OP);
			setState(156); expression();
			setState(157); match(NL);
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

	public static class Mul_exprContext extends ParserRuleContext {
		public int _p;
		public Int_valContext int_val() {
			return getRuleContext(Int_valContext.class,0);
		}
		public TerminalNode OP_MUL() { return getToken(SerpentParser.OP_MUL, 0); }
		public Mul_exprContext mul_expr() {
			return getRuleContext(Mul_exprContext.class,0);
		}
		public Mul_exprContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Mul_exprContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_mul_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterMul_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitMul_expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitMul_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Mul_exprContext mul_expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Mul_exprContext _localctx = new Mul_exprContext(_ctx, _parentState, _p);
		Mul_exprContext _prevctx = _localctx;
		int _startState = 38;
		enterRecursionRule(_localctx, RULE_mul_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(160); int_val();
			}
			_ctx.stop = _input.LT(-1);
			setState(167);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Mul_exprContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_mul_expr);
					setState(162);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(163); match(OP_MUL);
					setState(164); int_val();
					}
					} 
				}
				setState(169);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Add_exprContext extends ParserRuleContext {
		public int _p;
		public TerminalNode OP_ADD() { return getToken(SerpentParser.OP_ADD, 0); }
		public Add_exprContext add_expr() {
			return getRuleContext(Add_exprContext.class,0);
		}
		public Mul_exprContext mul_expr() {
			return getRuleContext(Mul_exprContext.class,0);
		}
		public Add_exprContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Add_exprContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_add_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterAdd_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitAdd_expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitAdd_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Add_exprContext add_expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Add_exprContext _localctx = new Add_exprContext(_ctx, _parentState, _p);
		Add_exprContext _prevctx = _localctx;
		int _startState = 40;
		enterRecursionRule(_localctx, RULE_add_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(171); mul_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(178);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Add_exprContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_add_expr);
					setState(173);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(174); match(OP_ADD);
					setState(175); mul_expr(0);
					}
					} 
				}
				setState(180);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Rel_expContext extends ParserRuleContext {
		public int _p;
		public Rel_expContext rel_exp() {
			return getRuleContext(Rel_expContext.class,0);
		}
		public TerminalNode OP_REL() { return getToken(SerpentParser.OP_REL, 0); }
		public Add_exprContext add_expr() {
			return getRuleContext(Add_exprContext.class,0);
		}
		public Rel_expContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Rel_expContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_rel_exp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterRel_exp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitRel_exp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitRel_exp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Rel_expContext rel_exp(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Rel_expContext _localctx = new Rel_expContext(_ctx, _parentState, _p);
		Rel_expContext _prevctx = _localctx;
		int _startState = 42;
		enterRecursionRule(_localctx, RULE_rel_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(182); add_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(189);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Rel_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_rel_exp);
					setState(184);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(185); match(OP_REL);
					setState(186); add_expr(0);
					}
					} 
				}
				setState(191);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Eq_expContext extends ParserRuleContext {
		public int _p;
		public Eq_expContext eq_exp() {
			return getRuleContext(Eq_expContext.class,0);
		}
		public Rel_expContext rel_exp() {
			return getRuleContext(Rel_expContext.class,0);
		}
		public TerminalNode OP_EQ() { return getToken(SerpentParser.OP_EQ, 0); }
		public Eq_expContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Eq_expContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_eq_exp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterEq_exp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitEq_exp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitEq_exp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Eq_expContext eq_exp(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Eq_expContext _localctx = new Eq_expContext(_ctx, _parentState, _p);
		Eq_expContext _prevctx = _localctx;
		int _startState = 44;
		enterRecursionRule(_localctx, RULE_eq_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(193); rel_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(200);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Eq_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_eq_exp);
					setState(195);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(196); match(OP_EQ);
					setState(197); rel_exp(0);
					}
					} 
				}
				setState(202);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class And_expContext extends ParserRuleContext {
		public int _p;
		public Eq_expContext eq_exp() {
			return getRuleContext(Eq_expContext.class,0);
		}
		public TerminalNode OP_AND() { return getToken(SerpentParser.OP_AND, 0); }
		public And_expContext and_exp() {
			return getRuleContext(And_expContext.class,0);
		}
		public And_expContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public And_expContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_and_exp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterAnd_exp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitAnd_exp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitAnd_exp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final And_expContext and_exp(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		And_expContext _localctx = new And_expContext(_ctx, _parentState, _p);
		And_expContext _prevctx = _localctx;
		int _startState = 46;
		enterRecursionRule(_localctx, RULE_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(204); eq_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(211);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new And_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_and_exp);
					setState(206);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(207); match(OP_AND);
					setState(208); eq_exp(0);
					}
					} 
				}
				setState(213);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Ex_or_expContext extends ParserRuleContext {
		public int _p;
		public And_expContext and_exp() {
			return getRuleContext(And_expContext.class,0);
		}
		public Ex_or_expContext ex_or_exp() {
			return getRuleContext(Ex_or_expContext.class,0);
		}
		public TerminalNode OP_EX_OR() { return getToken(SerpentParser.OP_EX_OR, 0); }
		public Ex_or_expContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Ex_or_expContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_ex_or_exp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterEx_or_exp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitEx_or_exp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitEx_or_exp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ex_or_expContext ex_or_exp(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Ex_or_expContext _localctx = new Ex_or_expContext(_ctx, _parentState, _p);
		Ex_or_expContext _prevctx = _localctx;
		int _startState = 48;
		enterRecursionRule(_localctx, RULE_ex_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(215); and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(222);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Ex_or_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_ex_or_exp);
					setState(217);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(218); match(OP_EX_OR);
					setState(219); and_exp(0);
					}
					} 
				}
				setState(224);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class In_or_expContext extends ParserRuleContext {
		public int _p;
		public TerminalNode OP_IN_OR() { return getToken(SerpentParser.OP_IN_OR, 0); }
		public In_or_expContext in_or_exp() {
			return getRuleContext(In_or_expContext.class,0);
		}
		public Ex_or_expContext ex_or_exp() {
			return getRuleContext(Ex_or_expContext.class,0);
		}
		public In_or_expContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public In_or_expContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_in_or_exp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterIn_or_exp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitIn_or_exp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitIn_or_exp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final In_or_expContext in_or_exp(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		In_or_expContext _localctx = new In_or_expContext(_ctx, _parentState, _p);
		In_or_expContext _prevctx = _localctx;
		int _startState = 50;
		enterRecursionRule(_localctx, RULE_in_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(226); ex_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(233);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new In_or_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_in_or_exp);
					setState(228);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(229); match(OP_IN_OR);
					setState(230); ex_or_exp(0);
					}
					} 
				}
				setState(235);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Log_and_expContext extends ParserRuleContext {
		public int _p;
		public In_or_expContext in_or_exp() {
			return getRuleContext(In_or_expContext.class,0);
		}
		public TerminalNode OP_LOG_AND() { return getToken(SerpentParser.OP_LOG_AND, 0); }
		public Log_and_expContext log_and_exp() {
			return getRuleContext(Log_and_expContext.class,0);
		}
		public Log_and_expContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Log_and_expContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_log_and_exp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterLog_and_exp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitLog_and_exp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitLog_and_exp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Log_and_expContext log_and_exp(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Log_and_expContext _localctx = new Log_and_expContext(_ctx, _parentState, _p);
		Log_and_expContext _prevctx = _localctx;
		int _startState = 52;
		enterRecursionRule(_localctx, RULE_log_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(237); in_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(244);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Log_and_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_log_and_exp);
					setState(239);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(240); match(OP_LOG_AND);
					setState(241); in_or_exp(0);
					}
					} 
				}
				setState(246);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Log_or_expContext extends ParserRuleContext {
		public int _p;
		public Log_or_expContext log_or_exp() {
			return getRuleContext(Log_or_expContext.class,0);
		}
		public TerminalNode OP_LOG_OR() { return getToken(SerpentParser.OP_LOG_OR, 0); }
		public Log_and_expContext log_and_exp() {
			return getRuleContext(Log_and_expContext.class,0);
		}
		public Log_or_expContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Log_or_expContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_log_or_exp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterLog_or_exp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitLog_or_exp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitLog_or_exp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Log_or_expContext log_or_exp(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Log_or_expContext _localctx = new Log_or_expContext(_ctx, _parentState, _p);
		Log_or_expContext _prevctx = _localctx;
		int _startState = 54;
		enterRecursionRule(_localctx, RULE_log_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(248); log_and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(255);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Log_or_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_log_or_exp);
					setState(250);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(251); match(OP_LOG_OR);
					setState(252); log_and_exp(0);
					}
					} 
				}
				setState(257);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public Log_or_expContext log_or_exp() {
			return getRuleContext(Log_or_expContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(258); log_or_exp(0);
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

	public static class ConditionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConditionContext condition() throws RecognitionException {
		ConditionContext _localctx = new ConditionContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(260); expression();
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

	public static class Int_valContext extends ParserRuleContext {
		public Get_varContext get_var() {
			return getRuleContext(Get_varContext.class,0);
		}
		public Special_funcContext special_func() {
			return getRuleContext(Special_funcContext.class,0);
		}
		public TerminalNode INT() { return getToken(SerpentParser.INT, 0); }
		public Hex_numContext hex_num() {
			return getRuleContext(Hex_numContext.class,0);
		}
		public TerminalNode OP_NOT() { return getToken(SerpentParser.OP_NOT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Int_valContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_int_val; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterInt_val(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitInt_val(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitInt_val(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Int_valContext int_val() throws RecognitionException {
		Int_valContext _localctx = new Int_valContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_int_val);
		try {
			setState(275);
			switch (_input.LA(1)) {
			case INT:
				enterOuterAlt(_localctx, 1);
				{
				setState(262); match(INT);
				}
				break;
			case HEX_NUMBER:
				enterOuterAlt(_localctx, 2);
				{
				setState(263); hex_num();
				}
				break;
			case VAR:
				enterOuterAlt(_localctx, 3);
				{
				setState(264); get_var();
				}
				break;
			case 1:
			case 2:
			case 4:
			case 6:
			case 7:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
				enterOuterAlt(_localctx, 4);
				{
				setState(265); special_func();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 5);
				{
				setState(266); match(10);
				setState(267); expression();
				setState(268); match(3);
				}
				break;
			case OP_NOT:
				enterOuterAlt(_localctx, 6);
				{
				setState(270); match(OP_NOT);
				setState(271); match(10);
				setState(272); expression();
				setState(273); match(3);
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

	public static class Hex_numContext extends ParserRuleContext {
		public TerminalNode HEX_NUMBER() { return getToken(SerpentParser.HEX_NUMBER, 0); }
		public Hex_numContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hex_num; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterHex_num(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitHex_num(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitHex_num(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Hex_numContext hex_num() throws RecognitionException {
		Hex_numContext _localctx = new Hex_numContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_hex_num);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(277); match(HEX_NUMBER);
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

	public static class Ret_funcContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public TerminalNode INT() { return getToken(SerpentParser.INT, 0); }
		public Ret_funcContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ret_func; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterRet_func(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitRet_func(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitRet_func(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ret_funcContext ret_func() throws RecognitionException {
		Ret_funcContext _localctx = new Ret_funcContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_ret_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(279); match(13);
			setState(280); match(10);
			setState(281); match(INT);
			setState(282); match(3);
			setState(283); match(NL);
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

	public static class Get_varContext extends ParserRuleContext {
		public TerminalNode VAR() { return getToken(SerpentParser.VAR, 0); }
		public Get_varContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_get_var; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterGet_var(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitGet_var(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitGet_var(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Get_varContext get_var() throws RecognitionException {
		Get_varContext _localctx = new Get_varContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_get_var);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(285); match(VAR);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 19: return mul_expr_sempred((Mul_exprContext)_localctx, predIndex);

		case 20: return add_expr_sempred((Add_exprContext)_localctx, predIndex);

		case 21: return rel_exp_sempred((Rel_expContext)_localctx, predIndex);

		case 22: return eq_exp_sempred((Eq_expContext)_localctx, predIndex);

		case 23: return and_exp_sempred((And_expContext)_localctx, predIndex);

		case 24: return ex_or_exp_sempred((Ex_or_expContext)_localctx, predIndex);

		case 25: return in_or_exp_sempred((In_or_expContext)_localctx, predIndex);

		case 26: return log_and_exp_sempred((Log_and_expContext)_localctx, predIndex);

		case 27: return log_or_exp_sempred((Log_or_expContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean log_or_exp_sempred(Log_or_expContext _localctx, int predIndex) {
		switch (predIndex) {
		case 8: return 1 >= _localctx._p;
		}
		return true;
	}
	private boolean eq_exp_sempred(Eq_expContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3: return 1 >= _localctx._p;
		}
		return true;
	}
	private boolean and_exp_sempred(And_expContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4: return 1 >= _localctx._p;
		}
		return true;
	}
	private boolean log_and_exp_sempred(Log_and_expContext _localctx, int predIndex) {
		switch (predIndex) {
		case 7: return 1 >= _localctx._p;
		}
		return true;
	}
	private boolean mul_expr_sempred(Mul_exprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0: return 1 >= _localctx._p;
		}
		return true;
	}
	private boolean ex_or_exp_sempred(Ex_or_expContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5: return 1 >= _localctx._p;
		}
		return true;
	}
	private boolean rel_exp_sempred(Rel_expContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2: return 1 >= _localctx._p;
		}
		return true;
	}
	private boolean in_or_exp_sempred(In_or_expContext _localctx, int predIndex) {
		switch (predIndex) {
		case 6: return 1 >= _localctx._p;
		}
		return true;
	}
	private boolean add_expr_sempred(Add_exprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1: return 1 >= _localctx._p;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3+\u0122\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\7\3O\n\3\f\3\16\3R\13"+
		"\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\7\4a\n\4\f\4\16"+
		"\4d\13\4\3\4\3\4\3\4\3\4\3\4\5\4k\n\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6\u0081\n\6\3\7\3\7"+
		"\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17"+
		"\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\7\25\u00a8\n\25\f\25\16\25\u00ab\13\25\3\26"+
		"\3\26\3\26\3\26\3\26\3\26\7\26\u00b3\n\26\f\26\16\26\u00b6\13\26\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\7\27\u00be\n\27\f\27\16\27\u00c1\13\27\3\30"+
		"\3\30\3\30\3\30\3\30\3\30\7\30\u00c9\n\30\f\30\16\30\u00cc\13\30\3\31"+
		"\3\31\3\31\3\31\3\31\3\31\7\31\u00d4\n\31\f\31\16\31\u00d7\13\31\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\7\32\u00df\n\32\f\32\16\32\u00e2\13\32\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\7\33\u00ea\n\33\f\33\16\33\u00ed\13\33\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\7\34\u00f5\n\34\f\34\16\34\u00f8\13\34\3\35"+
		"\3\35\3\35\3\35\3\35\3\35\7\35\u0100\n\35\f\35\16\35\u0103\13\35\3\36"+
		"\3\36\3\37\3\37\3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \5 \u0116\n \3!"+
		"\3!\3\"\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\2$\2\4\6\b\n\f\16\20\22\24\26\30"+
		"\32\34\36 \"$&(*,.\60\62\64\668:<>@BD\2\2\u0120\2F\3\2\2\2\4P\3\2\2\2"+
		"\6S\3\2\2\2\bl\3\2\2\2\n\u0080\3\2\2\2\f\u0082\3\2\2\2\16\u0084\3\2\2"+
		"\2\20\u0086\3\2\2\2\22\u0088\3\2\2\2\24\u008a\3\2\2\2\26\u008c\3\2\2\2"+
		"\30\u008e\3\2\2\2\32\u0090\3\2\2\2\34\u0092\3\2\2\2\36\u0094\3\2\2\2 "+
		"\u0096\3\2\2\2\"\u0098\3\2\2\2$\u009a\3\2\2\2&\u009c\3\2\2\2(\u00a1\3"+
		"\2\2\2*\u00ac\3\2\2\2,\u00b7\3\2\2\2.\u00c2\3\2\2\2\60\u00cd\3\2\2\2\62"+
		"\u00d8\3\2\2\2\64\u00e3\3\2\2\2\66\u00ee\3\2\2\28\u00f9\3\2\2\2:\u0104"+
		"\3\2\2\2<\u0106\3\2\2\2>\u0115\3\2\2\2@\u0117\3\2\2\2B\u0119\3\2\2\2D"+
		"\u011f\3\2\2\2FG\5\4\3\2GH\7\2\2\3H\3\3\2\2\2IO\5&\24\2JO\5\n\6\2KO\5"+
		"\6\4\2LO\5\b\5\2MO\5B\"\2NI\3\2\2\2NJ\3\2\2\2NK\3\2\2\2NL\3\2\2\2NM\3"+
		"\2\2\2OR\3\2\2\2PN\3\2\2\2PQ\3\2\2\2Q\5\3\2\2\2RP\3\2\2\2ST\7\r\2\2TU"+
		"\5<\37\2UV\7\13\2\2VW\7*\2\2WX\5\4\3\2Xb\7+\2\2YZ\7\16\2\2Z[\5<\37\2["+
		"\\\7\13\2\2\\]\7*\2\2]^\5\4\3\2^_\7+\2\2_a\3\2\2\2`Y\3\2\2\2ad\3\2\2\2"+
		"b`\3\2\2\2bc\3\2\2\2cj\3\2\2\2db\3\2\2\2ef\7\7\2\2fg\7*\2\2gh\5\4\3\2"+
		"hi\7+\2\2ik\3\2\2\2je\3\2\2\2jk\3\2\2\2k\7\3\2\2\2lm\7\n\2\2mn\5<\37\2"+
		"no\7\13\2\2op\7*\2\2pq\5\4\3\2qr\7+\2\2r\t\3\2\2\2s\u0081\5\f\7\2t\u0081"+
		"\5\16\b\2u\u0081\5\20\t\2v\u0081\5\22\n\2w\u0081\5\24\13\2x\u0081\5\26"+
		"\f\2y\u0081\5\30\r\2z\u0081\5\32\16\2{\u0081\5\34\17\2|\u0081\5\36\20"+
		"\2}\u0081\5 \21\2~\u0081\5\"\22\2\177\u0081\5$\23\2\u0080s\3\2\2\2\u0080"+
		"t\3\2\2\2\u0080u\3\2\2\2\u0080v\3\2\2\2\u0080w\3\2\2\2\u0080x\3\2\2\2"+
		"\u0080y\3\2\2\2\u0080z\3\2\2\2\u0080{\3\2\2\2\u0080|\3\2\2\2\u0080}\3"+
		"\2\2\2\u0080~\3\2\2\2\u0080\177\3\2\2\2\u0081\13\3\2\2\2\u0082\u0083\7"+
		"\3\2\2\u0083\r\3\2\2\2\u0084\u0085\7\20\2\2\u0085\17\3\2\2\2\u0086\u0087"+
		"\7\22\2\2\u0087\21\3\2\2\2\u0088\u0089\7\6\2\2\u0089\23\3\2\2\2\u008a"+
		"\u008b\7\t\2\2\u008b\25\3\2\2\2\u008c\u008d\7\b\2\2\u008d\27\3\2\2\2\u008e"+
		"\u008f\7\23\2\2\u008f\31\3\2\2\2\u0090\u0091\7\25\2\2\u0091\33\3\2\2\2"+
		"\u0092\u0093\7\4\2\2\u0093\35\3\2\2\2\u0094\u0095\7\21\2\2\u0095\37\3"+
		"\2\2\2\u0096\u0097\7\26\2\2\u0097!\3\2\2\2\u0098\u0099\7\27\2\2\u0099"+
		"#\3\2\2\2\u009a\u009b\7\24\2\2\u009b%\3\2\2\2\u009c\u009d\7!\2\2\u009d"+
		"\u009e\7\34\2\2\u009e\u009f\5:\36\2\u009f\u00a0\7\35\2\2\u00a0\'\3\2\2"+
		"\2\u00a1\u00a2\b\25\1\2\u00a2\u00a3\5> \2\u00a3\u00a9\3\2\2\2\u00a4\u00a5"+
		"\6\25\2\3\u00a5\u00a6\7#\2\2\u00a6\u00a8\5> \2\u00a7\u00a4\3\2\2\2\u00a8"+
		"\u00ab\3\2\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa)\3\2\2\2"+
		"\u00ab\u00a9\3\2\2\2\u00ac\u00ad\b\26\1\2\u00ad\u00ae\5(\25\2\u00ae\u00b4"+
		"\3\2\2\2\u00af\u00b0\6\26\3\3\u00b0\u00b1\7\"\2\2\u00b1\u00b3\5(\25\2"+
		"\u00b2\u00af\3\2\2\2\u00b3\u00b6\3\2\2\2\u00b4\u00b2\3\2\2\2\u00b4\u00b5"+
		"\3\2\2\2\u00b5+\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b7\u00b8\b\27\1\2\u00b8"+
		"\u00b9\5*\26\2\u00b9\u00bf\3\2\2\2\u00ba\u00bb\6\27\4\3\u00bb\u00bc\7"+
		"$\2\2\u00bc\u00be\5*\26\2\u00bd\u00ba\3\2\2\2\u00be\u00c1\3\2\2\2\u00bf"+
		"\u00bd\3\2\2\2\u00bf\u00c0\3\2\2\2\u00c0-\3\2\2\2\u00c1\u00bf\3\2\2\2"+
		"\u00c2\u00c3\b\30\1\2\u00c3\u00c4\5,\27\2\u00c4\u00ca\3\2\2\2\u00c5\u00c6"+
		"\6\30\5\3\u00c6\u00c7\7%\2\2\u00c7\u00c9\5,\27\2\u00c8\u00c5\3\2\2\2\u00c9"+
		"\u00cc\3\2\2\2\u00ca\u00c8\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb/\3\2\2\2"+
		"\u00cc\u00ca\3\2\2\2\u00cd\u00ce\b\31\1\2\u00ce\u00cf\5.\30\2\u00cf\u00d5"+
		"\3\2\2\2\u00d0\u00d1\6\31\6\3\u00d1\u00d2\7&\2\2\u00d2\u00d4\5.\30\2\u00d3"+
		"\u00d0\3\2\2\2\u00d4\u00d7\3\2\2\2\u00d5\u00d3\3\2\2\2\u00d5\u00d6\3\2"+
		"\2\2\u00d6\61\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d8\u00d9\b\32\1\2\u00d9\u00da"+
		"\5\60\31\2\u00da\u00e0\3\2\2\2\u00db\u00dc\6\32\7\3\u00dc\u00dd\7\30\2"+
		"\2\u00dd\u00df\5\60\31\2\u00de\u00db\3\2\2\2\u00df\u00e2\3\2\2\2\u00e0"+
		"\u00de\3\2\2\2\u00e0\u00e1\3\2\2\2\u00e1\63\3\2\2\2\u00e2\u00e0\3\2\2"+
		"\2\u00e3\u00e4\b\33\1\2\u00e4\u00e5\5\62\32\2\u00e5\u00eb\3\2\2\2\u00e6"+
		"\u00e7\6\33\b\3\u00e7\u00e8\7\'\2\2\u00e8\u00ea\5\62\32\2\u00e9\u00e6"+
		"\3\2\2\2\u00ea\u00ed\3\2\2\2\u00eb\u00e9\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec"+
		"\65\3\2\2\2\u00ed\u00eb\3\2\2\2\u00ee\u00ef\b\34\1\2\u00ef\u00f0\5\64"+
		"\33\2\u00f0\u00f6\3\2\2\2\u00f1\u00f2\6\34\t\3\u00f2\u00f3\7\31\2\2\u00f3"+
		"\u00f5\5\64\33\2\u00f4\u00f1\3\2\2\2\u00f5\u00f8\3\2\2\2\u00f6\u00f4\3"+
		"\2\2\2\u00f6\u00f7\3\2\2\2\u00f7\67\3\2\2\2\u00f8\u00f6\3\2\2\2\u00f9"+
		"\u00fa\b\35\1\2\u00fa\u00fb\5\66\34\2\u00fb\u0101\3\2\2\2\u00fc\u00fd"+
		"\6\35\n\3\u00fd\u00fe\7\32\2\2\u00fe\u0100\5\66\34\2\u00ff\u00fc\3\2\2"+
		"\2\u0100\u0103\3\2\2\2\u0101\u00ff\3\2\2\2\u0101\u0102\3\2\2\2\u01029"+
		"\3\2\2\2\u0103\u0101\3\2\2\2\u0104\u0105\58\35\2\u0105;\3\2\2\2\u0106"+
		"\u0107\5:\36\2\u0107=\3\2\2\2\u0108\u0116\7 \2\2\u0109\u0116\5@!\2\u010a"+
		"\u0116\5D#\2\u010b\u0116\5\n\6\2\u010c\u010d\7\f\2\2\u010d\u010e\5:\36"+
		"\2\u010e\u010f\7\5\2\2\u010f\u0116\3\2\2\2\u0110\u0111\7\33\2\2\u0111"+
		"\u0112\7\f\2\2\u0112\u0113\5:\36\2\u0113\u0114\7\5\2\2\u0114\u0116\3\2"+
		"\2\2\u0115\u0108\3\2\2\2\u0115\u0109\3\2\2\2\u0115\u010a\3\2\2\2\u0115"+
		"\u010b\3\2\2\2\u0115\u010c\3\2\2\2\u0115\u0110\3\2\2\2\u0116?\3\2\2\2"+
		"\u0117\u0118\7)\2\2\u0118A\3\2\2\2\u0119\u011a\7\17\2\2\u011a\u011b\7"+
		"\f\2\2\u011b\u011c\7 \2\2\u011c\u011d\7\5\2\2\u011d\u011e\7\35\2\2\u011e"+
		"C\3\2\2\2\u011f\u0120\7!\2\2\u0120E\3\2\2\2\21NPbj\u0080\u00a9\u00b4\u00bf"+
		"\u00ca\u00d5\u00e0\u00eb\u00f6\u0101\u0115";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}