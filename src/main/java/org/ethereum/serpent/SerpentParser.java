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
		T__33=1, T__32=2, T__31=3, T__30=4, T__29=5, T__28=6, T__27=7, T__26=8, 
		T__25=9, T__24=10, T__23=11, T__22=12, T__21=13, T__20=14, T__19=15, T__18=16, 
		T__17=17, T__16=18, T__15=19, T__14=20, T__13=21, T__12=22, T__11=23, 
		T__10=24, T__9=25, T__8=26, T__7=27, T__6=28, T__5=29, T__4=30, T__3=31, 
		T__2=32, T__1=33, T__0=34, INT=35, ASM_SYMBOLS=36, OP_EX_OR=37, OP_LOG_AND=38, 
		OP_LOG_OR=39, OP_NOT=40, EQ_OP=41, NL=42, WS=43, LINE_COMMENT=44, VAR=45, 
		OP_ADD=46, OP_MUL=47, OP_REL=48, OP_EQ=49, OP_AND=50, OP_IN_OR=51, HEX_DIGIT=52, 
		HEX_NUMBER=53, INDENT=54, DEDENT=55;
	public static final String[] tokenNames = {
		"<INVALID>", "']'", "'stop'", "'block.coinbase'", "','", "'msg'", "'tx.gas'", 
		"'while'", "'['", "'('", "':'", "'if'", "'send'", "'block.timestamp'", 
		"'[asm'", "'msg.value'", "'init'", "'block.prevhash'", "'contract.storage'", 
		"'suicide'", "'block.difficulty'", "'msg.datasize'", "')'", "'tx.gasprice'", 
		"'else:'", "'tx.origin'", "'msg.data'", "'elif'", "'return'", "'msg.sender'", 
		"'contract.balance'", "'asm]'", "'block.gaslimit'", "'block.number'", 
		"'code'", "INT", "ASM_SYMBOLS", "'xor'", "OP_LOG_AND", "OP_LOG_OR", "OP_NOT", 
		"'='", "NL", "WS", "LINE_COMMENT", "VAR", "OP_ADD", "OP_MUL", "OP_REL", 
		"OP_EQ", "'&'", "'|'", "HEX_DIGIT", "HEX_NUMBER", "INDENT", "DEDENT"
	};
	public static final int
		RULE_parse = 0, RULE_parse_init_code_block = 1, RULE_block = 2, RULE_asm = 3, 
		RULE_asm_symbol = 4, RULE_if_elif_else_stmt = 5, RULE_while_stmt = 6, 
		RULE_special_func = 7, RULE_msg_datasize = 8, RULE_msg_sender = 9, RULE_msg_value = 10, 
		RULE_tx_gasprice = 11, RULE_tx_origin = 12, RULE_tx_gas = 13, RULE_contract_balance = 14, 
		RULE_block_prevhash = 15, RULE_block_coinbase = 16, RULE_block_timestamp = 17, 
		RULE_block_number = 18, RULE_block_difficulty = 19, RULE_block_gaslimit = 20, 
		RULE_msg_func = 21, RULE_send_func = 22, RULE_single_send_fund = 23, RULE_msg_data = 24, 
		RULE_array_assign = 25, RULE_array_retreive = 26, RULE_assign = 27, RULE_arr_def = 28, 
		RULE_contract_storage_assign = 29, RULE_contract_storage_load = 30, RULE_mul_expr = 31, 
		RULE_add_expr = 32, RULE_rel_exp = 33, RULE_eq_exp = 34, RULE_and_exp = 35, 
		RULE_ex_or_exp = 36, RULE_in_or_exp = 37, RULE_log_and_exp = 38, RULE_log_or_exp = 39, 
		RULE_expression = 40, RULE_condition = 41, RULE_int_val = 42, RULE_hex_num = 43, 
		RULE_ret_func_1 = 44, RULE_ret_func_2 = 45, RULE_suicide_func = 46, RULE_stop_func = 47, 
		RULE_get_var = 48;
	public static final String[] ruleNames = {
		"parse", "parse_init_code_block", "block", "asm", "asm_symbol", "if_elif_else_stmt", 
		"while_stmt", "special_func", "msg_datasize", "msg_sender", "msg_value", 
		"tx_gasprice", "tx_origin", "tx_gas", "contract_balance", "block_prevhash", 
		"block_coinbase", "block_timestamp", "block_number", "block_difficulty", 
		"block_gaslimit", "msg_func", "send_func", "single_send_fund", "msg_data", 
		"array_assign", "array_retreive", "assign", "arr_def", "contract_storage_assign", 
		"contract_storage_load", "mul_expr", "add_expr", "rel_exp", "eq_exp", 
		"and_exp", "ex_or_exp", "in_or_exp", "log_and_exp", "log_or_exp", "expression", 
		"condition", "int_val", "hex_num", "ret_func_1", "ret_func_2", "suicide_func", 
		"stop_func", "get_var"
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
			setState(98); block();
			setState(99); match(EOF);
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

	public static class Parse_init_code_blockContext extends ParserRuleContext {
		public List<TerminalNode> INDENT() { return getTokens(SerpentParser.INDENT); }
		public TerminalNode INDENT(int i) {
			return getToken(SerpentParser.INDENT, i);
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
		public Parse_init_code_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse_init_code_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterParse_init_code_block(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitParse_init_code_block(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitParse_init_code_block(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Parse_init_code_blockContext parse_init_code_block() throws RecognitionException {
		Parse_init_code_blockContext _localctx = new Parse_init_code_blockContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_parse_init_code_block);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101); match(16);
			setState(102); match(10);
			setState(103); match(INDENT);
			setState(104); block();
			setState(105); match(DEDENT);
			setState(106); match(34);
			setState(107); match(10);
			setState(108); match(INDENT);
			setState(109); block();
			setState(110); match(DEDENT);
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
		public Single_send_fundContext single_send_fund(int i) {
			return getRuleContext(Single_send_fundContext.class,i);
		}
		public List<Single_send_fundContext> single_send_fund() {
			return getRuleContexts(Single_send_fundContext.class);
		}
		public Array_assignContext array_assign(int i) {
			return getRuleContext(Array_assignContext.class,i);
		}
		public Stop_funcContext stop_func(int i) {
			return getRuleContext(Stop_funcContext.class,i);
		}
		public List<Ret_func_1Context> ret_func_1() {
			return getRuleContexts(Ret_func_1Context.class);
		}
		public List<AssignContext> assign() {
			return getRuleContexts(AssignContext.class);
		}
		public List<AsmContext> asm() {
			return getRuleContexts(AsmContext.class);
		}
		public List<If_elif_else_stmtContext> if_elif_else_stmt() {
			return getRuleContexts(If_elif_else_stmtContext.class);
		}
		public List<Suicide_funcContext> suicide_func() {
			return getRuleContexts(Suicide_funcContext.class);
		}
		public List<Array_assignContext> array_assign() {
			return getRuleContexts(Array_assignContext.class);
		}
		public List<Stop_funcContext> stop_func() {
			return getRuleContexts(Stop_funcContext.class);
		}
		public List<While_stmtContext> while_stmt() {
			return getRuleContexts(While_stmtContext.class);
		}
		public Contract_storage_assignContext contract_storage_assign(int i) {
			return getRuleContext(Contract_storage_assignContext.class,i);
		}
		public List<Special_funcContext> special_func() {
			return getRuleContexts(Special_funcContext.class);
		}
		public AsmContext asm(int i) {
			return getRuleContext(AsmContext.class,i);
		}
		public Special_funcContext special_func(int i) {
			return getRuleContext(Special_funcContext.class,i);
		}
		public While_stmtContext while_stmt(int i) {
			return getRuleContext(While_stmtContext.class,i);
		}
		public List<Contract_storage_assignContext> contract_storage_assign() {
			return getRuleContexts(Contract_storage_assignContext.class);
		}
		public Ret_func_2Context ret_func_2(int i) {
			return getRuleContext(Ret_func_2Context.class,i);
		}
		public AssignContext assign(int i) {
			return getRuleContext(AssignContext.class,i);
		}
		public Suicide_funcContext suicide_func(int i) {
			return getRuleContext(Suicide_funcContext.class,i);
		}
		public List<Ret_func_2Context> ret_func_2() {
			return getRuleContexts(Ret_func_2Context.class);
		}
		public Ret_func_1Context ret_func_1(int i) {
			return getRuleContext(Ret_func_1Context.class,i);
		}
		public If_elif_else_stmtContext if_elif_else_stmt(int i) {
			return getRuleContext(If_elif_else_stmtContext.class,i);
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
		enterRule(_localctx, 4, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 3) | (1L << 6) | (1L << 7) | (1L << 11) | (1L << 12) | (1L << 13) | (1L << 14) | (1L << 15) | (1L << 17) | (1L << 18) | (1L << 19) | (1L << 20) | (1L << 21) | (1L << 23) | (1L << 25) | (1L << 28) | (1L << 29) | (1L << 30) | (1L << 32) | (1L << 33) | (1L << VAR))) != 0)) {
				{
				setState(124);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(112); asm();
					}
					break;

				case 2:
					{
					setState(113); array_assign();
					}
					break;

				case 3:
					{
					setState(114); assign();
					}
					break;

				case 4:
					{
					setState(115); contract_storage_assign();
					}
					break;

				case 5:
					{
					setState(116); special_func();
					}
					break;

				case 6:
					{
					setState(117); if_elif_else_stmt();
					}
					break;

				case 7:
					{
					setState(118); while_stmt();
					}
					break;

				case 8:
					{
					setState(119); ret_func_1();
					}
					break;

				case 9:
					{
					setState(120); ret_func_2();
					}
					break;

				case 10:
					{
					setState(121); suicide_func();
					}
					break;

				case 11:
					{
					setState(122); stop_func();
					}
					break;

				case 12:
					{
					setState(123); single_send_fund();
					}
					break;
				}
				}
				setState(128);
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

	public static class AsmContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public Asm_symbolContext asm_symbol() {
			return getRuleContext(Asm_symbolContext.class,0);
		}
		public AsmContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterAsm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitAsm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitAsm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmContext asm() throws RecognitionException {
		AsmContext _localctx = new AsmContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_asm);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129); match(14);
			setState(130); asm_symbol();
			setState(131); match(31);
			setState(132); match(NL);
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

	public static class Asm_symbolContext extends ParserRuleContext {
		public TerminalNode HEX_NUMBER(int i) {
			return getToken(SerpentParser.HEX_NUMBER, i);
		}
		public TerminalNode ASM_SYMBOLS(int i) {
			return getToken(SerpentParser.ASM_SYMBOLS, i);
		}
		public List<TerminalNode> INT() { return getTokens(SerpentParser.INT); }
		public List<TerminalNode> ASM_SYMBOLS() { return getTokens(SerpentParser.ASM_SYMBOLS); }
		public TerminalNode INT(int i) {
			return getToken(SerpentParser.INT, i);
		}
		public List<TerminalNode> HEX_NUMBER() { return getTokens(SerpentParser.HEX_NUMBER); }
		public Asm_symbolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asm_symbol; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterAsm_symbol(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitAsm_symbol(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitAsm_symbol(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Asm_symbolContext asm_symbol() throws RecognitionException {
		Asm_symbolContext _localctx = new Asm_symbolContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_asm_symbol);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INT) | (1L << ASM_SYMBOLS) | (1L << HEX_NUMBER))) != 0)) {
				{
				{
				setState(134);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INT) | (1L << ASM_SYMBOLS) | (1L << HEX_NUMBER))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				}
				setState(139);
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
		enterRule(_localctx, 10, RULE_if_elif_else_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140); match(11);
			setState(141); condition();
			setState(142); match(10);
			setState(143); match(INDENT);
			setState(144); block();
			setState(145); match(DEDENT);
			setState(155);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==27) {
				{
				{
				setState(146); match(27);
				setState(147); condition();
				setState(148); match(10);
				setState(149); match(INDENT);
				setState(150); block();
				setState(151); match(DEDENT);
				}
				}
				setState(157);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(163);
			_la = _input.LA(1);
			if (_la==24) {
				{
				setState(158); match(24);
				setState(159); match(INDENT);
				setState(160); block();
				setState(161); match(DEDENT);
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
		enterRule(_localctx, 12, RULE_while_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165); match(7);
			setState(166); condition();
			setState(167); match(10);
			setState(168); match(INDENT);
			setState(169); block();
			setState(170); match(DEDENT);
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
		enterRule(_localctx, 14, RULE_special_func);
		try {
			setState(185);
			switch (_input.LA(1)) {
			case 21:
				enterOuterAlt(_localctx, 1);
				{
				setState(172); msg_datasize();
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 2);
				{
				setState(173); msg_sender();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 3);
				{
				setState(174); msg_value();
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 4);
				{
				setState(175); tx_gasprice();
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 5);
				{
				setState(176); tx_origin();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(177); tx_gas();
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 7);
				{
				setState(178); contract_balance();
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 8);
				{
				setState(179); block_prevhash();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 9);
				{
				setState(180); block_coinbase();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 10);
				{
				setState(181); block_timestamp();
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 11);
				{
				setState(182); block_number();
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 12);
				{
				setState(183); block_difficulty();
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 13);
				{
				setState(184); block_gaslimit();
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
		enterRule(_localctx, 16, RULE_msg_datasize);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187); match(21);
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
		enterRule(_localctx, 18, RULE_msg_sender);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189); match(29);
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
		enterRule(_localctx, 20, RULE_msg_value);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(191); match(15);
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
		enterRule(_localctx, 22, RULE_tx_gasprice);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193); match(23);
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
		enterRule(_localctx, 24, RULE_tx_origin);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(195); match(25);
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
		enterRule(_localctx, 26, RULE_tx_gas);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197); match(6);
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
		enterRule(_localctx, 28, RULE_contract_balance);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(199); match(30);
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
		enterRule(_localctx, 30, RULE_block_prevhash);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201); match(17);
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
		enterRule(_localctx, 32, RULE_block_coinbase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203); match(3);
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
		enterRule(_localctx, 34, RULE_block_timestamp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205); match(13);
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
		enterRule(_localctx, 36, RULE_block_number);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(207); match(33);
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
		enterRule(_localctx, 38, RULE_block_difficulty);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209); match(20);
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
		enterRule(_localctx, 40, RULE_block_gaslimit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211); match(32);
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

	public static class Msg_funcContext extends ParserRuleContext {
		public Int_valContext int_val(int i) {
			return getRuleContext(Int_valContext.class,i);
		}
		public List<Int_valContext> int_val() {
			return getRuleContexts(Int_valContext.class);
		}
		public Msg_funcContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_msg_func; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterMsg_func(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitMsg_func(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitMsg_func(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Msg_funcContext msg_func() throws RecognitionException {
		Msg_funcContext _localctx = new Msg_funcContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_msg_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(213); match(5);
			setState(214); match(9);
			setState(215); int_val();
			setState(216); match(4);
			setState(217); int_val();
			setState(218); match(4);
			setState(219); int_val();
			setState(220); match(4);
			setState(221); int_val();
			setState(222); match(4);
			setState(223); int_val();
			setState(224); match(22);
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

	public static class Send_funcContext extends ParserRuleContext {
		public Int_valContext int_val(int i) {
			return getRuleContext(Int_valContext.class,i);
		}
		public List<Int_valContext> int_val() {
			return getRuleContexts(Int_valContext.class);
		}
		public Send_funcContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_send_func; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterSend_func(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitSend_func(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitSend_func(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Send_funcContext send_func() throws RecognitionException {
		Send_funcContext _localctx = new Send_funcContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_send_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226); match(12);
			setState(227); match(9);
			setState(228); int_val();
			setState(229); match(4);
			setState(230); int_val();
			setState(231); match(4);
			setState(232); int_val();
			setState(233); match(22);
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

	public static class Single_send_fundContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public Send_funcContext send_func() {
			return getRuleContext(Send_funcContext.class,0);
		}
		public Single_send_fundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_single_send_fund; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterSingle_send_fund(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitSingle_send_fund(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitSingle_send_fund(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Single_send_fundContext single_send_fund() throws RecognitionException {
		Single_send_fundContext _localctx = new Single_send_fundContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_single_send_fund);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(235); send_func();
			setState(236); match(NL);
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

	public static class Msg_dataContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Msg_dataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_msg_data; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterMsg_data(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitMsg_data(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitMsg_data(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Msg_dataContext msg_data() throws RecognitionException {
		Msg_dataContext _localctx = new Msg_dataContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_msg_data);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238); match(26);
			setState(239); match(8);
			setState(240); expression();
			setState(241); match(1);
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

	public static class Array_assignContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public TerminalNode VAR() { return getToken(SerpentParser.VAR, 0); }
		public TerminalNode EQ_OP() { return getToken(SerpentParser.EQ_OP, 0); }
		public Int_valContext int_val() {
			return getRuleContext(Int_valContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Array_assignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_assign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterArray_assign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitArray_assign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitArray_assign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_assignContext array_assign() throws RecognitionException {
		Array_assignContext _localctx = new Array_assignContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_array_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(243); match(VAR);
			setState(244); match(8);
			setState(245); int_val();
			setState(246); match(1);
			setState(247); match(EQ_OP);
			setState(248); expression();
			setState(249); match(NL);
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

	public static class Array_retreiveContext extends ParserRuleContext {
		public TerminalNode VAR() { return getToken(SerpentParser.VAR, 0); }
		public Int_valContext int_val() {
			return getRuleContext(Int_valContext.class,0);
		}
		public Array_retreiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_retreive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterArray_retreive(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitArray_retreive(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitArray_retreive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_retreiveContext array_retreive() throws RecognitionException {
		Array_retreiveContext _localctx = new Array_retreiveContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_array_retreive);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(251); match(VAR);
			setState(252); match(8);
			setState(253); int_val();
			setState(254); match(1);
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
		public Arr_defContext arr_def() {
			return getRuleContext(Arr_defContext.class,0);
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
		enterRule(_localctx, 54, RULE_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(256); match(VAR);
			setState(257); match(EQ_OP);
			setState(260);
			switch (_input.LA(1)) {
			case 3:
			case 5:
			case 6:
			case 9:
			case 12:
			case 13:
			case 15:
			case 17:
			case 18:
			case 20:
			case 21:
			case 23:
			case 25:
			case 26:
			case 29:
			case 30:
			case 32:
			case 33:
			case INT:
			case OP_NOT:
			case VAR:
			case HEX_NUMBER:
				{
				setState(258); expression();
				}
				break;
			case 8:
				{
				setState(259); arr_def();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(262); match(NL);
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

	public static class Arr_defContext extends ParserRuleContext {
		public Int_valContext int_val(int i) {
			return getRuleContext(Int_valContext.class,i);
		}
		public List<Int_valContext> int_val() {
			return getRuleContexts(Int_valContext.class);
		}
		public Arr_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arr_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterArr_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitArr_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitArr_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Arr_defContext arr_def() throws RecognitionException {
		Arr_defContext _localctx = new Arr_defContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_arr_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(264); match(8);
			setState(271);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 3) | (1L << 5) | (1L << 6) | (1L << 9) | (1L << 12) | (1L << 13) | (1L << 15) | (1L << 17) | (1L << 18) | (1L << 20) | (1L << 21) | (1L << 23) | (1L << 25) | (1L << 26) | (1L << 29) | (1L << 30) | (1L << 32) | (1L << 33) | (1L << INT) | (1L << OP_NOT) | (1L << VAR) | (1L << HEX_NUMBER))) != 0)) {
				{
				{
				setState(265); int_val();
				setState(267);
				_la = _input.LA(1);
				if (_la==4) {
					{
					setState(266); match(4);
					}
				}

				}
				}
				setState(273);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(274); match(1);
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

	public static class Contract_storage_assignContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode EQ_OP() { return getToken(SerpentParser.EQ_OP, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public Contract_storage_assignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_contract_storage_assign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterContract_storage_assign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitContract_storage_assign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitContract_storage_assign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Contract_storage_assignContext contract_storage_assign() throws RecognitionException {
		Contract_storage_assignContext _localctx = new Contract_storage_assignContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_contract_storage_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(276); match(18);
			setState(277); match(8);
			setState(278); expression();
			setState(279); match(1);
			setState(280); match(EQ_OP);
			setState(281); expression();
			setState(282); match(NL);
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

	public static class Contract_storage_loadContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Contract_storage_loadContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_contract_storage_load; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterContract_storage_load(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitContract_storage_load(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitContract_storage_load(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Contract_storage_loadContext contract_storage_load() throws RecognitionException {
		Contract_storage_loadContext _localctx = new Contract_storage_loadContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_contract_storage_load);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(284); match(18);
			setState(285); match(8);
			setState(286); expression();
			setState(287); match(1);
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
		int _startState = 62;
		enterRecursionRule(_localctx, RULE_mul_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(290); int_val();
			}
			_ctx.stop = _input.LT(-1);
			setState(297);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Mul_exprContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_mul_expr);
					setState(292);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(293); match(OP_MUL);
					setState(294); int_val();
					}
					} 
				}
				setState(299);
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
		int _startState = 64;
		enterRecursionRule(_localctx, RULE_add_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(301); mul_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(308);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Add_exprContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_add_expr);
					setState(303);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(304); match(OP_ADD);
					setState(305); mul_expr(0);
					}
					} 
				}
				setState(310);
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
		int _startState = 66;
		enterRecursionRule(_localctx, RULE_rel_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(312); add_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(319);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Rel_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_rel_exp);
					setState(314);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(315); match(OP_REL);
					setState(316); add_expr(0);
					}
					} 
				}
				setState(321);
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
		int _startState = 68;
		enterRecursionRule(_localctx, RULE_eq_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(323); rel_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(330);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Eq_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_eq_exp);
					setState(325);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(326); match(OP_EQ);
					setState(327); rel_exp(0);
					}
					} 
				}
				setState(332);
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
		int _startState = 70;
		enterRecursionRule(_localctx, RULE_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(334); eq_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(341);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new And_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_and_exp);
					setState(336);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(337); match(OP_AND);
					setState(338); eq_exp(0);
					}
					} 
				}
				setState(343);
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
		int _startState = 72;
		enterRecursionRule(_localctx, RULE_ex_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(345); and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(352);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Ex_or_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_ex_or_exp);
					setState(347);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(348); match(OP_EX_OR);
					setState(349); and_exp(0);
					}
					} 
				}
				setState(354);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
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
		int _startState = 74;
		enterRecursionRule(_localctx, RULE_in_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(356); ex_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(363);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new In_or_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_in_or_exp);
					setState(358);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(359); match(OP_IN_OR);
					setState(360); ex_or_exp(0);
					}
					} 
				}
				setState(365);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
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
		int _startState = 76;
		enterRecursionRule(_localctx, RULE_log_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(367); in_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(374);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Log_and_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_log_and_exp);
					setState(369);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(370); match(OP_LOG_AND);
					setState(371); in_or_exp(0);
					}
					} 
				}
				setState(376);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
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
		int _startState = 78;
		enterRecursionRule(_localctx, RULE_log_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(378); log_and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(385);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Log_or_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_log_or_exp);
					setState(380);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(381); match(OP_LOG_OR);
					setState(382); log_and_exp(0);
					}
					} 
				}
				setState(387);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
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
		enterRule(_localctx, 80, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(388); log_or_exp(0);
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
		enterRule(_localctx, 82, RULE_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(390); expression();
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
		public Send_funcContext send_func() {
			return getRuleContext(Send_funcContext.class,0);
		}
		public Get_varContext get_var() {
			return getRuleContext(Get_varContext.class,0);
		}
		public Special_funcContext special_func() {
			return getRuleContext(Special_funcContext.class,0);
		}
		public TerminalNode INT() { return getToken(SerpentParser.INT, 0); }
		public Msg_funcContext msg_func() {
			return getRuleContext(Msg_funcContext.class,0);
		}
		public Hex_numContext hex_num() {
			return getRuleContext(Hex_numContext.class,0);
		}
		public TerminalNode OP_NOT() { return getToken(SerpentParser.OP_NOT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Msg_dataContext msg_data() {
			return getRuleContext(Msg_dataContext.class,0);
		}
		public Array_retreiveContext array_retreive() {
			return getRuleContext(Array_retreiveContext.class,0);
		}
		public Contract_storage_loadContext contract_storage_load() {
			return getRuleContext(Contract_storage_loadContext.class,0);
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
		enterRule(_localctx, 84, RULE_int_val);
		try {
			setState(410);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(392); match(INT);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(393); hex_num();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(394); get_var();
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(395); special_func();
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(396); match(9);
				setState(397); expression();
				setState(398); match(22);
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(400); match(OP_NOT);
				setState(401); match(9);
				setState(402); expression();
				setState(403); match(22);
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(405); msg_func();
				}
				break;

			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(406); msg_data();
				}
				break;

			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(407); send_func();
				}
				break;

			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(408); contract_storage_load();
				}
				break;

			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(409); array_retreive();
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
		enterRule(_localctx, 86, RULE_hex_num);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(412); match(HEX_NUMBER);
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

	public static class Ret_func_1Context extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Ret_func_1Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ret_func_1; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterRet_func_1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitRet_func_1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitRet_func_1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ret_func_1Context ret_func_1() throws RecognitionException {
		Ret_func_1Context _localctx = new Ret_func_1Context(_ctx, getState());
		enterRule(_localctx, 88, RULE_ret_func_1);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(414); match(28);
			setState(415); match(9);
			setState(416); expression();
			setState(417); match(22);
			setState(418); match(NL);
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

	public static class Ret_func_2Context extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public Ret_func_2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ret_func_2; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterRet_func_2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitRet_func_2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitRet_func_2(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ret_func_2Context ret_func_2() throws RecognitionException {
		Ret_func_2Context _localctx = new Ret_func_2Context(_ctx, getState());
		enterRule(_localctx, 90, RULE_ret_func_2);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(420); match(28);
			setState(421); match(9);
			setState(422); expression();
			setState(423); match(4);
			setState(424); expression();
			setState(425); match(22);
			setState(426); match(NL);
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

	public static class Suicide_funcContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Suicide_funcContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_suicide_func; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterSuicide_func(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitSuicide_func(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitSuicide_func(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Suicide_funcContext suicide_func() throws RecognitionException {
		Suicide_funcContext _localctx = new Suicide_funcContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_suicide_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428); match(19);
			setState(429); match(9);
			setState(430); expression();
			setState(431); match(22);
			setState(432); match(NL);
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

	public static class Stop_funcContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(SerpentParser.NL, 0); }
		public Stop_funcContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stop_func; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterStop_func(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitStop_func(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitStop_func(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Stop_funcContext stop_func() throws RecognitionException {
		Stop_funcContext _localctx = new Stop_funcContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_stop_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(434); match(2);
			setState(435); match(NL);
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
		enterRule(_localctx, 96, RULE_get_var);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(437); match(VAR);
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
		case 31: return mul_expr_sempred((Mul_exprContext)_localctx, predIndex);

		case 32: return add_expr_sempred((Add_exprContext)_localctx, predIndex);

		case 33: return rel_exp_sempred((Rel_expContext)_localctx, predIndex);

		case 34: return eq_exp_sempred((Eq_expContext)_localctx, predIndex);

		case 35: return and_exp_sempred((And_expContext)_localctx, predIndex);

		case 36: return ex_or_exp_sempred((Ex_or_expContext)_localctx, predIndex);

		case 37: return in_or_exp_sempred((In_or_expContext)_localctx, predIndex);

		case 38: return log_and_exp_sempred((Log_and_expContext)_localctx, predIndex);

		case 39: return log_or_exp_sempred((Log_or_expContext)_localctx, predIndex);
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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\39\u01ba\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\3\2\3\2\3\2\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\3\4\7\4\177\n\4\f\4\16\4\u0082\13\4\3\5\3\5\3\5\3\5\3\5\3"+
		"\6\7\6\u008a\n\6\f\6\16\6\u008d\13\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\7\7\u009c\n\7\f\7\16\7\u009f\13\7\3\7\3\7\3\7\3\7\3"+
		"\7\5\7\u00a6\n\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u00bc\n\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r"+
		"\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24"+
		"\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\31"+
		"\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\5\35\u0107\n\35\3\35"+
		"\3\35\3\36\3\36\3\36\5\36\u010e\n\36\7\36\u0110\n\36\f\36\16\36\u0113"+
		"\13\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3"+
		" \3!\3!\3!\3!\3!\3!\7!\u012a\n!\f!\16!\u012d\13!\3\"\3\"\3\"\3\"\3\"\3"+
		"\"\7\"\u0135\n\"\f\"\16\"\u0138\13\"\3#\3#\3#\3#\3#\3#\7#\u0140\n#\f#"+
		"\16#\u0143\13#\3$\3$\3$\3$\3$\3$\7$\u014b\n$\f$\16$\u014e\13$\3%\3%\3"+
		"%\3%\3%\3%\7%\u0156\n%\f%\16%\u0159\13%\3&\3&\3&\3&\3&\3&\7&\u0161\n&"+
		"\f&\16&\u0164\13&\3\'\3\'\3\'\3\'\3\'\3\'\7\'\u016c\n\'\f\'\16\'\u016f"+
		"\13\'\3(\3(\3(\3(\3(\3(\7(\u0177\n(\f(\16(\u017a\13(\3)\3)\3)\3)\3)\3"+
		")\7)\u0182\n)\f)\16)\u0185\13)\3*\3*\3+\3+\3,\3,\3,\3,\3,\3,\3,\3,\3,"+
		"\3,\3,\3,\3,\3,\3,\3,\3,\3,\5,\u019d\n,\3-\3-\3.\3.\3.\3.\3.\3.\3/\3/"+
		"\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\60\3\60\3\61\3\61\3\61\3\62\3"+
		"\62\3\62\2\63\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64"+
		"\668:<>@BDFHJLNPRTVXZ\\^`b\2\3\4\2%&\67\67\u01b9\2d\3\2\2\2\4g\3\2\2\2"+
		"\6\u0080\3\2\2\2\b\u0083\3\2\2\2\n\u008b\3\2\2\2\f\u008e\3\2\2\2\16\u00a7"+
		"\3\2\2\2\20\u00bb\3\2\2\2\22\u00bd\3\2\2\2\24\u00bf\3\2\2\2\26\u00c1\3"+
		"\2\2\2\30\u00c3\3\2\2\2\32\u00c5\3\2\2\2\34\u00c7\3\2\2\2\36\u00c9\3\2"+
		"\2\2 \u00cb\3\2\2\2\"\u00cd\3\2\2\2$\u00cf\3\2\2\2&\u00d1\3\2\2\2(\u00d3"+
		"\3\2\2\2*\u00d5\3\2\2\2,\u00d7\3\2\2\2.\u00e4\3\2\2\2\60\u00ed\3\2\2\2"+
		"\62\u00f0\3\2\2\2\64\u00f5\3\2\2\2\66\u00fd\3\2\2\28\u0102\3\2\2\2:\u010a"+
		"\3\2\2\2<\u0116\3\2\2\2>\u011e\3\2\2\2@\u0123\3\2\2\2B\u012e\3\2\2\2D"+
		"\u0139\3\2\2\2F\u0144\3\2\2\2H\u014f\3\2\2\2J\u015a\3\2\2\2L\u0165\3\2"+
		"\2\2N\u0170\3\2\2\2P\u017b\3\2\2\2R\u0186\3\2\2\2T\u0188\3\2\2\2V\u019c"+
		"\3\2\2\2X\u019e\3\2\2\2Z\u01a0\3\2\2\2\\\u01a6\3\2\2\2^\u01ae\3\2\2\2"+
		"`\u01b4\3\2\2\2b\u01b7\3\2\2\2de\5\6\4\2ef\7\2\2\3f\3\3\2\2\2gh\7\22\2"+
		"\2hi\7\f\2\2ij\78\2\2jk\5\6\4\2kl\79\2\2lm\7$\2\2mn\7\f\2\2no\78\2\2o"+
		"p\5\6\4\2pq\79\2\2q\5\3\2\2\2r\177\5\b\5\2s\177\5\64\33\2t\177\58\35\2"+
		"u\177\5<\37\2v\177\5\20\t\2w\177\5\f\7\2x\177\5\16\b\2y\177\5Z.\2z\177"+
		"\5\\/\2{\177\5^\60\2|\177\5`\61\2}\177\5\60\31\2~r\3\2\2\2~s\3\2\2\2~"+
		"t\3\2\2\2~u\3\2\2\2~v\3\2\2\2~w\3\2\2\2~x\3\2\2\2~y\3\2\2\2~z\3\2\2\2"+
		"~{\3\2\2\2~|\3\2\2\2~}\3\2\2\2\177\u0082\3\2\2\2\u0080~\3\2\2\2\u0080"+
		"\u0081\3\2\2\2\u0081\7\3\2\2\2\u0082\u0080\3\2\2\2\u0083\u0084\7\20\2"+
		"\2\u0084\u0085\5\n\6\2\u0085\u0086\7!\2\2\u0086\u0087\7,\2\2\u0087\t\3"+
		"\2\2\2\u0088\u008a\t\2\2\2\u0089\u0088\3\2\2\2\u008a\u008d\3\2\2\2\u008b"+
		"\u0089\3\2\2\2\u008b\u008c\3\2\2\2\u008c\13\3\2\2\2\u008d\u008b\3\2\2"+
		"\2\u008e\u008f\7\r\2\2\u008f\u0090\5T+\2\u0090\u0091\7\f\2\2\u0091\u0092"+
		"\78\2\2\u0092\u0093\5\6\4\2\u0093\u009d\79\2\2\u0094\u0095\7\35\2\2\u0095"+
		"\u0096\5T+\2\u0096\u0097\7\f\2\2\u0097\u0098\78\2\2\u0098\u0099\5\6\4"+
		"\2\u0099\u009a\79\2\2\u009a\u009c\3\2\2\2\u009b\u0094\3\2\2\2\u009c\u009f"+
		"\3\2\2\2\u009d\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u00a5\3\2\2\2\u009f"+
		"\u009d\3\2\2\2\u00a0\u00a1\7\32\2\2\u00a1\u00a2\78\2\2\u00a2\u00a3\5\6"+
		"\4\2\u00a3\u00a4\79\2\2\u00a4\u00a6\3\2\2\2\u00a5\u00a0\3\2\2\2\u00a5"+
		"\u00a6\3\2\2\2\u00a6\r\3\2\2\2\u00a7\u00a8\7\t\2\2\u00a8\u00a9\5T+\2\u00a9"+
		"\u00aa\7\f\2\2\u00aa\u00ab\78\2\2\u00ab\u00ac\5\6\4\2\u00ac\u00ad\79\2"+
		"\2\u00ad\17\3\2\2\2\u00ae\u00bc\5\22\n\2\u00af\u00bc\5\24\13\2\u00b0\u00bc"+
		"\5\26\f\2\u00b1\u00bc\5\30\r\2\u00b2\u00bc\5\32\16\2\u00b3\u00bc\5\34"+
		"\17\2\u00b4\u00bc\5\36\20\2\u00b5\u00bc\5 \21\2\u00b6\u00bc\5\"\22\2\u00b7"+
		"\u00bc\5$\23\2\u00b8\u00bc\5&\24\2\u00b9\u00bc\5(\25\2\u00ba\u00bc\5*"+
		"\26\2\u00bb\u00ae\3\2\2\2\u00bb\u00af\3\2\2\2\u00bb\u00b0\3\2\2\2\u00bb"+
		"\u00b1\3\2\2\2\u00bb\u00b2\3\2\2\2\u00bb\u00b3\3\2\2\2\u00bb\u00b4\3\2"+
		"\2\2\u00bb\u00b5\3\2\2\2\u00bb\u00b6\3\2\2\2\u00bb\u00b7\3\2\2\2\u00bb"+
		"\u00b8\3\2\2\2\u00bb\u00b9\3\2\2\2\u00bb\u00ba\3\2\2\2\u00bc\21\3\2\2"+
		"\2\u00bd\u00be\7\27\2\2\u00be\23\3\2\2\2\u00bf\u00c0\7\37\2\2\u00c0\25"+
		"\3\2\2\2\u00c1\u00c2\7\21\2\2\u00c2\27\3\2\2\2\u00c3\u00c4\7\31\2\2\u00c4"+
		"\31\3\2\2\2\u00c5\u00c6\7\33\2\2\u00c6\33\3\2\2\2\u00c7\u00c8\7\b\2\2"+
		"\u00c8\35\3\2\2\2\u00c9\u00ca\7 \2\2\u00ca\37\3\2\2\2\u00cb\u00cc\7\23"+
		"\2\2\u00cc!\3\2\2\2\u00cd\u00ce\7\5\2\2\u00ce#\3\2\2\2\u00cf\u00d0\7\17"+
		"\2\2\u00d0%\3\2\2\2\u00d1\u00d2\7#\2\2\u00d2\'\3\2\2\2\u00d3\u00d4\7\26"+
		"\2\2\u00d4)\3\2\2\2\u00d5\u00d6\7\"\2\2\u00d6+\3\2\2\2\u00d7\u00d8\7\7"+
		"\2\2\u00d8\u00d9\7\13\2\2\u00d9\u00da\5V,\2\u00da\u00db\7\6\2\2\u00db"+
		"\u00dc\5V,\2\u00dc\u00dd\7\6\2\2\u00dd\u00de\5V,\2\u00de\u00df\7\6\2\2"+
		"\u00df\u00e0\5V,\2\u00e0\u00e1\7\6\2\2\u00e1\u00e2\5V,\2\u00e2\u00e3\7"+
		"\30\2\2\u00e3-\3\2\2\2\u00e4\u00e5\7\16\2\2\u00e5\u00e6\7\13\2\2\u00e6"+
		"\u00e7\5V,\2\u00e7\u00e8\7\6\2\2\u00e8\u00e9\5V,\2\u00e9\u00ea\7\6\2\2"+
		"\u00ea\u00eb\5V,\2\u00eb\u00ec\7\30\2\2\u00ec/\3\2\2\2\u00ed\u00ee\5."+
		"\30\2\u00ee\u00ef\7,\2\2\u00ef\61\3\2\2\2\u00f0\u00f1\7\34\2\2\u00f1\u00f2"+
		"\7\n\2\2\u00f2\u00f3\5R*\2\u00f3\u00f4\7\3\2\2\u00f4\63\3\2\2\2\u00f5"+
		"\u00f6\7/\2\2\u00f6\u00f7\7\n\2\2\u00f7\u00f8\5V,\2\u00f8\u00f9\7\3\2"+
		"\2\u00f9\u00fa\7+\2\2\u00fa\u00fb\5R*\2\u00fb\u00fc\7,\2\2\u00fc\65\3"+
		"\2\2\2\u00fd\u00fe\7/\2\2\u00fe\u00ff\7\n\2\2\u00ff\u0100\5V,\2\u0100"+
		"\u0101\7\3\2\2\u0101\67\3\2\2\2\u0102\u0103\7/\2\2\u0103\u0106\7+\2\2"+
		"\u0104\u0107\5R*\2\u0105\u0107\5:\36\2\u0106\u0104\3\2\2\2\u0106\u0105"+
		"\3\2\2\2\u0107\u0108\3\2\2\2\u0108\u0109\7,\2\2\u01099\3\2\2\2\u010a\u0111"+
		"\7\n\2\2\u010b\u010d\5V,\2\u010c\u010e\7\6\2\2\u010d\u010c\3\2\2\2\u010d"+
		"\u010e\3\2\2\2\u010e\u0110\3\2\2\2\u010f\u010b\3\2\2\2\u0110\u0113\3\2"+
		"\2\2\u0111\u010f\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0114\3\2\2\2\u0113"+
		"\u0111\3\2\2\2\u0114\u0115\7\3\2\2\u0115;\3\2\2\2\u0116\u0117\7\24\2\2"+
		"\u0117\u0118\7\n\2\2\u0118\u0119\5R*\2\u0119\u011a\7\3\2\2\u011a\u011b"+
		"\7+\2\2\u011b\u011c\5R*\2\u011c\u011d\7,\2\2\u011d=\3\2\2\2\u011e\u011f"+
		"\7\24\2\2\u011f\u0120\7\n\2\2\u0120\u0121\5R*\2\u0121\u0122\7\3\2\2\u0122"+
		"?\3\2\2\2\u0123\u0124\b!\1\2\u0124\u0125\5V,\2\u0125\u012b\3\2\2\2\u0126"+
		"\u0127\6!\2\3\u0127\u0128\7\61\2\2\u0128\u012a\5V,\2\u0129\u0126\3\2\2"+
		"\2\u012a\u012d\3\2\2\2\u012b\u0129\3\2\2\2\u012b\u012c\3\2\2\2\u012cA"+
		"\3\2\2\2\u012d\u012b\3\2\2\2\u012e\u012f\b\"\1\2\u012f\u0130\5@!\2\u0130"+
		"\u0136\3\2\2\2\u0131\u0132\6\"\3\3\u0132\u0133\7\60\2\2\u0133\u0135\5"+
		"@!\2\u0134\u0131\3\2\2\2\u0135\u0138\3\2\2\2\u0136\u0134\3\2\2\2\u0136"+
		"\u0137\3\2\2\2\u0137C\3\2\2\2\u0138\u0136\3\2\2\2\u0139\u013a\b#\1\2\u013a"+
		"\u013b\5B\"\2\u013b\u0141\3\2\2\2\u013c\u013d\6#\4\3\u013d\u013e\7\62"+
		"\2\2\u013e\u0140\5B\"\2\u013f\u013c\3\2\2\2\u0140\u0143\3\2\2\2\u0141"+
		"\u013f\3\2\2\2\u0141\u0142\3\2\2\2\u0142E\3\2\2\2\u0143\u0141\3\2\2\2"+
		"\u0144\u0145\b$\1\2\u0145\u0146\5D#\2\u0146\u014c\3\2\2\2\u0147\u0148"+
		"\6$\5\3\u0148\u0149\7\63\2\2\u0149\u014b\5D#\2\u014a\u0147\3\2\2\2\u014b"+
		"\u014e\3\2\2\2\u014c\u014a\3\2\2\2\u014c\u014d\3\2\2\2\u014dG\3\2\2\2"+
		"\u014e\u014c\3\2\2\2\u014f\u0150\b%\1\2\u0150\u0151\5F$\2\u0151\u0157"+
		"\3\2\2\2\u0152\u0153\6%\6\3\u0153\u0154\7\64\2\2\u0154\u0156\5F$\2\u0155"+
		"\u0152\3\2\2\2\u0156\u0159\3\2\2\2\u0157\u0155\3\2\2\2\u0157\u0158\3\2"+
		"\2\2\u0158I\3\2\2\2\u0159\u0157\3\2\2\2\u015a\u015b\b&\1\2\u015b\u015c"+
		"\5H%\2\u015c\u0162\3\2\2\2\u015d\u015e\6&\7\3\u015e\u015f\7\'\2\2\u015f"+
		"\u0161\5H%\2\u0160\u015d\3\2\2\2\u0161\u0164\3\2\2\2\u0162\u0160\3\2\2"+
		"\2\u0162\u0163\3\2\2\2\u0163K\3\2\2\2\u0164\u0162\3\2\2\2\u0165\u0166"+
		"\b\'\1\2\u0166\u0167\5J&\2\u0167\u016d\3\2\2\2\u0168\u0169\6\'\b\3\u0169"+
		"\u016a\7\65\2\2\u016a\u016c\5J&\2\u016b\u0168\3\2\2\2\u016c\u016f\3\2"+
		"\2\2\u016d\u016b\3\2\2\2\u016d\u016e\3\2\2\2\u016eM\3\2\2\2\u016f\u016d"+
		"\3\2\2\2\u0170\u0171\b(\1\2\u0171\u0172\5L\'\2\u0172\u0178\3\2\2\2\u0173"+
		"\u0174\6(\t\3\u0174\u0175\7(\2\2\u0175\u0177\5L\'\2\u0176\u0173\3\2\2"+
		"\2\u0177\u017a\3\2\2\2\u0178\u0176\3\2\2\2\u0178\u0179\3\2\2\2\u0179O"+
		"\3\2\2\2\u017a\u0178\3\2\2\2\u017b\u017c\b)\1\2\u017c\u017d\5N(\2\u017d"+
		"\u0183\3\2\2\2\u017e\u017f\6)\n\3\u017f\u0180\7)\2\2\u0180\u0182\5N(\2"+
		"\u0181\u017e\3\2\2\2\u0182\u0185\3\2\2\2\u0183\u0181\3\2\2\2\u0183\u0184"+
		"\3\2\2\2\u0184Q\3\2\2\2\u0185\u0183\3\2\2\2\u0186\u0187\5P)\2\u0187S\3"+
		"\2\2\2\u0188\u0189\5R*\2\u0189U\3\2\2\2\u018a\u019d\7%\2\2\u018b\u019d"+
		"\5X-\2\u018c\u019d\5b\62\2\u018d\u019d\5\20\t\2\u018e\u018f\7\13\2\2\u018f"+
		"\u0190\5R*\2\u0190\u0191\7\30\2\2\u0191\u019d\3\2\2\2\u0192\u0193\7*\2"+
		"\2\u0193\u0194\7\13\2\2\u0194\u0195\5R*\2\u0195\u0196\7\30\2\2\u0196\u019d"+
		"\3\2\2\2\u0197\u019d\5,\27\2\u0198\u019d\5\62\32\2\u0199\u019d\5.\30\2"+
		"\u019a\u019d\5> \2\u019b\u019d\5\66\34\2\u019c\u018a\3\2\2\2\u019c\u018b"+
		"\3\2\2\2\u019c\u018c\3\2\2\2\u019c\u018d\3\2\2\2\u019c\u018e\3\2\2\2\u019c"+
		"\u0192\3\2\2\2\u019c\u0197\3\2\2\2\u019c\u0198\3\2\2\2\u019c\u0199\3\2"+
		"\2\2\u019c\u019a\3\2\2\2\u019c\u019b\3\2\2\2\u019dW\3\2\2\2\u019e\u019f"+
		"\7\67\2\2\u019fY\3\2\2\2\u01a0\u01a1\7\36\2\2\u01a1\u01a2\7\13\2\2\u01a2"+
		"\u01a3\5R*\2\u01a3\u01a4\7\30\2\2\u01a4\u01a5\7,\2\2\u01a5[\3\2\2\2\u01a6"+
		"\u01a7\7\36\2\2\u01a7\u01a8\7\13\2\2\u01a8\u01a9\5R*\2\u01a9\u01aa\7\6"+
		"\2\2\u01aa\u01ab\5R*\2\u01ab\u01ac\7\30\2\2\u01ac\u01ad\7,\2\2\u01ad]"+
		"\3\2\2\2\u01ae\u01af\7\25\2\2\u01af\u01b0\7\13\2\2\u01b0\u01b1\5R*\2\u01b1"+
		"\u01b2\7\30\2\2\u01b2\u01b3\7,\2\2\u01b3_\3\2\2\2\u01b4\u01b5\7\4\2\2"+
		"\u01b5\u01b6\7,\2\2\u01b6a\3\2\2\2\u01b7\u01b8\7/\2\2\u01b8c\3\2\2\2\25"+
		"~\u0080\u008b\u009d\u00a5\u00bb\u0106\u010d\u0111\u012b\u0136\u0141\u014c"+
		"\u0157\u0162\u016d\u0178\u0183\u019c";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}