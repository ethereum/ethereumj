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
		T__34=1, T__33=2, T__32=3, T__31=4, T__30=5, T__29=6, T__28=7, T__27=8, 
		T__26=9, T__25=10, T__24=11, T__23=12, T__22=13, T__21=14, T__20=15, T__19=16, 
		T__18=17, T__17=18, T__16=19, T__15=20, T__14=21, T__13=22, T__12=23, 
		T__11=24, T__10=25, T__9=26, T__8=27, T__7=28, T__6=29, T__5=30, T__4=31, 
		T__3=32, T__2=33, T__1=34, T__0=35, INT=36, ASM_SYMBOLS=37, OP_EX_OR=38, 
		OP_LOG_AND=39, OP_LOG_OR=40, OP_NOT=41, EQ_OP=42, NL=43, WS=44, LINE_COMMENT=45, 
		VAR=46, OP_ADD=47, OP_MUL=48, OP_REL=49, OP_EQ=50, OP_AND=51, OP_IN_OR=52, 
		HEX_DIGIT=53, HEX_NUMBER=54, INDENT=55, DEDENT=56;
	public static final String[] tokenNames = {
		"<INVALID>", "']'", "'stop'", "'block.coinbase'", "','", "'msg'", "'tx.gas'", 
		"'while'", "'['", "':'", "'('", "'if'", "'send'", "'block.timestamp'", 
		"'contract.address'", "'[asm'", "'msg.value'", "'init'", "'block.prevhash'", 
		"'contract.storage'", "'suicide'", "'block.difficulty'", "'msg.datasize'", 
		"')'", "'tx.gasprice'", "'else:'", "'tx.origin'", "'msg.data'", "'elif'", 
		"'return'", "'msg.sender'", "'contract.balance'", "'asm]'", "'block.gaslimit'", 
		"'block.number'", "'code'", "INT", "ASM_SYMBOLS", "'xor'", "OP_LOG_AND", 
		"OP_LOG_OR", "OP_NOT", "'='", "NL", "WS", "LINE_COMMENT", "VAR", "OP_ADD", 
		"OP_MUL", "OP_REL", "OP_EQ", "'&'", "'|'", "HEX_DIGIT", "HEX_NUMBER", 
		"INDENT", "DEDENT"
	};
	public static final int
		RULE_parse = 0, RULE_parse_init_code_block = 1, RULE_block = 2, RULE_asm = 3, 
		RULE_asm_symbol = 4, RULE_if_elif_else_stmt = 5, RULE_while_stmt = 6, 
		RULE_special_func = 7, RULE_msg_datasize = 8, RULE_msg_sender = 9, RULE_msg_value = 10, 
		RULE_tx_gasprice = 11, RULE_tx_origin = 12, RULE_tx_gas = 13, RULE_contract_balance = 14, 
		RULE_contract_address = 15, RULE_block_prevhash = 16, RULE_block_coinbase = 17, 
		RULE_block_timestamp = 18, RULE_block_number = 19, RULE_block_difficulty = 20, 
		RULE_block_gaslimit = 21, RULE_msg_func = 22, RULE_send_func = 23, RULE_single_send_fund = 24, 
		RULE_msg_data = 25, RULE_array_assign = 26, RULE_array_retreive = 27, 
		RULE_assign = 28, RULE_arr_def = 29, RULE_contract_storage_assign = 30, 
		RULE_contract_storage_load = 31, RULE_mul_expr = 32, RULE_add_expr = 33, 
		RULE_rel_exp = 34, RULE_eq_exp = 35, RULE_and_exp = 36, RULE_ex_or_exp = 37, 
		RULE_in_or_exp = 38, RULE_log_and_exp = 39, RULE_log_or_exp = 40, RULE_expression = 41, 
		RULE_condition = 42, RULE_int_val = 43, RULE_hex_num = 44, RULE_ret_func_1 = 45, 
		RULE_ret_func_2 = 46, RULE_suicide_func = 47, RULE_stop_func = 48, RULE_get_var = 49;
	public static final String[] ruleNames = {
		"parse", "parse_init_code_block", "block", "asm", "asm_symbol", "if_elif_else_stmt", 
		"while_stmt", "special_func", "msg_datasize", "msg_sender", "msg_value", 
		"tx_gasprice", "tx_origin", "tx_gas", "contract_balance", "contract_address", 
		"block_prevhash", "block_coinbase", "block_timestamp", "block_number", 
		"block_difficulty", "block_gaslimit", "msg_func", "send_func", "single_send_fund", 
		"msg_data", "array_assign", "array_retreive", "assign", "arr_def", "contract_storage_assign", 
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
			setState(100); block();
			setState(101); match(EOF);
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
			setState(103); match(17);
			setState(104); match(9);
			setState(105); match(INDENT);
			setState(106); block();
			setState(107); match(DEDENT);
			setState(108); match(35);
			setState(109); match(9);
			setState(110); match(INDENT);
			setState(111); block();
			setState(112); match(DEDENT);
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
		public Array_assignContext array_assign(int i) {
			return getRuleContext(Array_assignContext.class,i);
		}
		public List<Single_send_fundContext> single_send_fund() {
			return getRuleContexts(Single_send_fundContext.class);
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
		public List<Suicide_funcContext> suicide_func() {
			return getRuleContexts(Suicide_funcContext.class);
		}
		public List<While_stmtContext> while_stmt() {
			return getRuleContexts(While_stmtContext.class);
		}
		public List<Stop_funcContext> stop_func() {
			return getRuleContexts(Stop_funcContext.class);
		}
		public List<Special_funcContext> special_func() {
			return getRuleContexts(Special_funcContext.class);
		}
		public AsmContext asm(int i) {
			return getRuleContext(AsmContext.class,i);
		}
		public List<Contract_storage_assignContext> contract_storage_assign() {
			return getRuleContexts(Contract_storage_assignContext.class);
		}
		public Ret_func_1Context ret_func_1(int i) {
			return getRuleContext(Ret_func_1Context.class,i);
		}
		public List<Msg_funcContext> msg_func() {
			return getRuleContexts(Msg_funcContext.class);
		}
		public Stop_funcContext stop_func(int i) {
			return getRuleContext(Stop_funcContext.class,i);
		}
		public List<If_elif_else_stmtContext> if_elif_else_stmt() {
			return getRuleContexts(If_elif_else_stmtContext.class);
		}
		public List<Array_assignContext> array_assign() {
			return getRuleContexts(Array_assignContext.class);
		}
		public Contract_storage_assignContext contract_storage_assign(int i) {
			return getRuleContext(Contract_storage_assignContext.class,i);
		}
		public Special_funcContext special_func(int i) {
			return getRuleContext(Special_funcContext.class,i);
		}
		public While_stmtContext while_stmt(int i) {
			return getRuleContext(While_stmtContext.class,i);
		}
		public Ret_func_2Context ret_func_2(int i) {
			return getRuleContext(Ret_func_2Context.class,i);
		}
		public AssignContext assign(int i) {
			return getRuleContext(AssignContext.class,i);
		}
		public Msg_funcContext msg_func(int i) {
			return getRuleContext(Msg_funcContext.class,i);
		}
		public List<Ret_func_2Context> ret_func_2() {
			return getRuleContexts(Ret_func_2Context.class);
		}
		public Suicide_funcContext suicide_func(int i) {
			return getRuleContext(Suicide_funcContext.class,i);
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
			setState(129);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 3) | (1L << 5) | (1L << 6) | (1L << 7) | (1L << 11) | (1L << 12) | (1L << 13) | (1L << 14) | (1L << 15) | (1L << 16) | (1L << 18) | (1L << 19) | (1L << 20) | (1L << 21) | (1L << 22) | (1L << 24) | (1L << 26) | (1L << 29) | (1L << 30) | (1L << 31) | (1L << 33) | (1L << 34) | (1L << VAR))) != 0)) {
				{
				setState(127);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(114); asm();
					}
					break;

				case 2:
					{
					setState(115); array_assign();
					}
					break;

				case 3:
					{
					setState(116); assign();
					}
					break;

				case 4:
					{
					setState(117); contract_storage_assign();
					}
					break;

				case 5:
					{
					setState(118); special_func();
					}
					break;

				case 6:
					{
					setState(119); if_elif_else_stmt();
					}
					break;

				case 7:
					{
					setState(120); while_stmt();
					}
					break;

				case 8:
					{
					setState(121); ret_func_1();
					}
					break;

				case 9:
					{
					setState(122); ret_func_2();
					}
					break;

				case 10:
					{
					setState(123); suicide_func();
					}
					break;

				case 11:
					{
					setState(124); stop_func();
					}
					break;

				case 12:
					{
					setState(125); single_send_fund();
					}
					break;

				case 13:
					{
					setState(126); msg_func();
					}
					break;
				}
				}
				setState(131);
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
			setState(132); match(15);
			setState(133); asm_symbol();
			setState(134); match(32);
			setState(135); match(NL);
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
			setState(140);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INT) | (1L << ASM_SYMBOLS) | (1L << HEX_NUMBER))) != 0)) {
				{
				{
				setState(137);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INT) | (1L << ASM_SYMBOLS) | (1L << HEX_NUMBER))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				}
				setState(142);
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
			setState(143); match(11);
			setState(144); condition();
			setState(145); match(9);
			setState(146); match(INDENT);
			setState(147); block();
			setState(148); match(DEDENT);
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==28) {
				{
				{
				setState(149); match(28);
				setState(150); condition();
				setState(151); match(9);
				setState(152); match(INDENT);
				setState(153); block();
				setState(154); match(DEDENT);
				}
				}
				setState(160);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(166);
			_la = _input.LA(1);
			if (_la==25) {
				{
				setState(161); match(25);
				setState(162); match(INDENT);
				setState(163); block();
				setState(164); match(DEDENT);
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
			setState(168); match(7);
			setState(169); condition();
			setState(170); match(9);
			setState(171); match(INDENT);
			setState(172); block();
			setState(173); match(DEDENT);
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
		public Block_timestampContext block_timestamp() {
			return getRuleContext(Block_timestampContext.class,0);
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
		public Block_prevhashContext block_prevhash() {
			return getRuleContext(Block_prevhashContext.class,0);
		}
		public Block_numberContext block_number() {
			return getRuleContext(Block_numberContext.class,0);
		}
		public Block_gaslimitContext block_gaslimit() {
			return getRuleContext(Block_gaslimitContext.class,0);
		}
		public Tx_gaspriceContext tx_gasprice() {
			return getRuleContext(Tx_gaspriceContext.class,0);
		}
		public Tx_originContext tx_origin() {
			return getRuleContext(Tx_originContext.class,0);
		}
		public Contract_addressContext contract_address() {
			return getRuleContext(Contract_addressContext.class,0);
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
			setState(189);
			switch (_input.LA(1)) {
			case 22:
				enterOuterAlt(_localctx, 1);
				{
				setState(175); msg_datasize();
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 2);
				{
				setState(176); msg_sender();
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 3);
				{
				setState(177); msg_value();
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 4);
				{
				setState(178); tx_gasprice();
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 5);
				{
				setState(179); tx_origin();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(180); tx_gas();
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 7);
				{
				setState(181); contract_balance();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 8);
				{
				setState(182); contract_address();
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 9);
				{
				setState(183); block_prevhash();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 10);
				{
				setState(184); block_coinbase();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 11);
				{
				setState(185); block_timestamp();
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 12);
				{
				setState(186); block_number();
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 13);
				{
				setState(187); block_difficulty();
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 14);
				{
				setState(188); block_gaslimit();
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
			setState(191); match(22);
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
			setState(193); match(30);
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
			setState(195); match(16);
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
			setState(197); match(24);
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
			setState(199); match(26);
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
			setState(201); match(6);
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
			setState(203); match(31);
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

	public static class Contract_addressContext extends ParserRuleContext {
		public Contract_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_contract_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).enterContract_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SerpentListener ) ((SerpentListener)listener).exitContract_address(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SerpentVisitor ) return ((SerpentVisitor<? extends T>)visitor).visitContract_address(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Contract_addressContext contract_address() throws RecognitionException {
		Contract_addressContext _localctx = new Contract_addressContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_contract_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205); match(14);
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
		enterRule(_localctx, 32, RULE_block_prevhash);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(207); match(18);
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
		enterRule(_localctx, 34, RULE_block_coinbase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209); match(3);
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
		enterRule(_localctx, 36, RULE_block_timestamp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211); match(13);
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
		enterRule(_localctx, 38, RULE_block_number);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(213); match(34);
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
		enterRule(_localctx, 40, RULE_block_difficulty);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215); match(21);
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
		enterRule(_localctx, 42, RULE_block_gaslimit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(217); match(33);
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
		public Arr_defContext arr_def() {
			return getRuleContext(Arr_defContext.class,0);
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
		enterRule(_localctx, 44, RULE_msg_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219); match(5);
			setState(220); match(10);
			setState(221); int_val();
			setState(222); match(4);
			setState(223); int_val();
			setState(224); match(4);
			setState(225); int_val();
			setState(226); match(4);
			setState(227); arr_def();
			setState(228); match(4);
			setState(229); int_val();
			setState(230); match(4);
			setState(231); int_val();
			setState(232); match(23);
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
		enterRule(_localctx, 46, RULE_send_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234); match(12);
			setState(235); match(10);
			setState(236); int_val();
			setState(237); match(4);
			setState(238); int_val();
			setState(239); match(4);
			setState(240); int_val();
			setState(241); match(23);
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
		enterRule(_localctx, 48, RULE_single_send_fund);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(243); send_func();
			setState(244); match(NL);
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
		enterRule(_localctx, 50, RULE_msg_data);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(246); match(27);
			setState(247); match(8);
			setState(248); expression();
			setState(249); match(1);
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
		enterRule(_localctx, 52, RULE_array_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(251); match(VAR);
			setState(252); match(8);
			setState(253); int_val();
			setState(254); match(1);
			setState(255); match(EQ_OP);
			setState(256); expression();
			setState(257); match(NL);
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
		enterRule(_localctx, 54, RULE_array_retreive);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259); match(VAR);
			setState(260); match(8);
			setState(261); int_val();
			setState(262); match(1);
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
		public Msg_funcContext msg_func() {
			return getRuleContext(Msg_funcContext.class,0);
		}
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
		enterRule(_localctx, 56, RULE_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(264); match(VAR);
			setState(265); match(EQ_OP);
			setState(269);
			switch (_input.LA(1)) {
			case 3:
			case 6:
			case 10:
			case 12:
			case 13:
			case 14:
			case 16:
			case 18:
			case 19:
			case 21:
			case 22:
			case 24:
			case 26:
			case 27:
			case 30:
			case 31:
			case 33:
			case 34:
			case INT:
			case OP_NOT:
			case VAR:
			case HEX_NUMBER:
				{
				setState(266); expression();
				}
				break;
			case 8:
				{
				setState(267); arr_def();
				}
				break;
			case 5:
				{
				setState(268); msg_func();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(271); match(NL);
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
		enterRule(_localctx, 58, RULE_arr_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(273); match(8);
			setState(280);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 3) | (1L << 6) | (1L << 10) | (1L << 12) | (1L << 13) | (1L << 14) | (1L << 16) | (1L << 18) | (1L << 19) | (1L << 21) | (1L << 22) | (1L << 24) | (1L << 26) | (1L << 27) | (1L << 30) | (1L << 31) | (1L << 33) | (1L << 34) | (1L << INT) | (1L << OP_NOT) | (1L << VAR) | (1L << HEX_NUMBER))) != 0)) {
				{
				{
				setState(274); int_val();
				setState(276);
				_la = _input.LA(1);
				if (_la==4) {
					{
					setState(275); match(4);
					}
				}

				}
				}
				setState(282);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(283); match(1);
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
		enterRule(_localctx, 60, RULE_contract_storage_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(285); match(19);
			setState(286); match(8);
			setState(287); expression();
			setState(288); match(1);
			setState(289); match(EQ_OP);
			setState(290); expression();
			setState(291); match(NL);
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
		enterRule(_localctx, 62, RULE_contract_storage_load);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(293); match(19);
			setState(294); match(8);
			setState(295); expression();
			setState(296); match(1);
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
		int _startState = 64;
		enterRecursionRule(_localctx, RULE_mul_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(299); int_val();
			}
			_ctx.stop = _input.LT(-1);
			setState(306);
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
					setState(301);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(302); match(OP_MUL);
					setState(303); int_val();
					}
					} 
				}
				setState(308);
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
		int _startState = 66;
		enterRecursionRule(_localctx, RULE_add_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(310); mul_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(317);
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
					setState(312);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(313); match(OP_ADD);
					setState(314); mul_expr(0);
					}
					} 
				}
				setState(319);
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
		int _startState = 68;
		enterRecursionRule(_localctx, RULE_rel_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(321); add_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(328);
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
					setState(323);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(324); match(OP_REL);
					setState(325); add_expr(0);
					}
					} 
				}
				setState(330);
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
		int _startState = 70;
		enterRecursionRule(_localctx, RULE_eq_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(332); rel_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(339);
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
					setState(334);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(335); match(OP_EQ);
					setState(336); rel_exp(0);
					}
					} 
				}
				setState(341);
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
		int _startState = 72;
		enterRecursionRule(_localctx, RULE_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(343); eq_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(350);
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
					setState(345);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(346); match(OP_AND);
					setState(347); eq_exp(0);
					}
					} 
				}
				setState(352);
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
		int _startState = 74;
		enterRecursionRule(_localctx, RULE_ex_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(354); and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(361);
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
					setState(356);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(357); match(OP_EX_OR);
					setState(358); and_exp(0);
					}
					} 
				}
				setState(363);
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
		int _startState = 76;
		enterRecursionRule(_localctx, RULE_in_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(365); ex_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(372);
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
					setState(367);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(368); match(OP_IN_OR);
					setState(369); ex_or_exp(0);
					}
					} 
				}
				setState(374);
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
		int _startState = 78;
		enterRecursionRule(_localctx, RULE_log_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(376); in_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(383);
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
					setState(378);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(379); match(OP_LOG_AND);
					setState(380); in_or_exp(0);
					}
					} 
				}
				setState(385);
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
		int _startState = 80;
		enterRecursionRule(_localctx, RULE_log_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(387); log_and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(394);
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
					setState(389);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(390); match(OP_LOG_OR);
					setState(391); log_and_exp(0);
					}
					} 
				}
				setState(396);
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
		enterRule(_localctx, 82, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(397); log_or_exp(0);
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
		enterRule(_localctx, 84, RULE_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(399); expression();
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
		enterRule(_localctx, 86, RULE_int_val);
		try {
			setState(418);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(401); match(INT);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(402); hex_num();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(403); get_var();
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(404); special_func();
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(405); match(10);
				setState(406); expression();
				setState(407); match(23);
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(409); match(OP_NOT);
				setState(410); match(10);
				setState(411); expression();
				setState(412); match(23);
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(414); msg_data();
				}
				break;

			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(415); send_func();
				}
				break;

			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(416); contract_storage_load();
				}
				break;

			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(417); array_retreive();
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
		enterRule(_localctx, 88, RULE_hex_num);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(420); match(HEX_NUMBER);
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
		enterRule(_localctx, 90, RULE_ret_func_1);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(422); match(29);
			setState(423); match(10);
			setState(424); expression();
			setState(425); match(23);
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
		enterRule(_localctx, 92, RULE_ret_func_2);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428); match(29);
			setState(429); match(10);
			setState(430); expression();
			setState(431); match(4);
			setState(432); expression();
			setState(433); match(23);
			setState(434); match(NL);
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
		enterRule(_localctx, 94, RULE_suicide_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(436); match(20);
			setState(437); match(10);
			setState(438); expression();
			setState(439); match(23);
			setState(440); match(NL);
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
		enterRule(_localctx, 96, RULE_stop_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(442); match(2);
			setState(443); match(NL);
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
		enterRule(_localctx, 98, RULE_get_var);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(445); match(VAR);
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
		case 32: return mul_expr_sempred((Mul_exprContext)_localctx, predIndex);

		case 33: return add_expr_sempred((Add_exprContext)_localctx, predIndex);

		case 34: return rel_exp_sempred((Rel_expContext)_localctx, predIndex);

		case 35: return eq_exp_sempred((Eq_expContext)_localctx, predIndex);

		case 36: return and_exp_sempred((And_expContext)_localctx, predIndex);

		case 37: return ex_or_exp_sempred((Ex_or_expContext)_localctx, predIndex);

		case 38: return in_or_exp_sempred((In_or_expContext)_localctx, predIndex);

		case 39: return log_and_exp_sempred((Log_and_expContext)_localctx, predIndex);

		case 40: return log_or_exp_sempred((Log_or_expContext)_localctx, predIndex);
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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3:\u01c2\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\3\2\3\2"+
		"\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\7\4\u0082\n\4\f\4\16\4\u0085\13\4\3\5\3"+
		"\5\3\5\3\5\3\5\3\6\7\6\u008d\n\6\f\6\16\6\u0090\13\6\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\7\7\u009f\n\7\f\7\16\7\u00a2\13\7\3"+
		"\7\3\7\3\7\3\7\3\7\5\7\u00a9\n\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u00c0\n\t\3\n\3\n\3"+
		"\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22"+
		"\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\30"+
		"\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31"+
		"\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\33\3\33\3\33\3\33"+
		"\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\35"+
		"\3\36\3\36\3\36\3\36\3\36\5\36\u0110\n\36\3\36\3\36\3\37\3\37\3\37\5\37"+
		"\u0117\n\37\7\37\u0119\n\37\f\37\16\37\u011c\13\37\3\37\3\37\3 \3 \3 "+
		"\3 \3 \3 \3 \3 \3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3\"\7\"\u0133\n\"\f"+
		"\"\16\"\u0136\13\"\3#\3#\3#\3#\3#\3#\7#\u013e\n#\f#\16#\u0141\13#\3$\3"+
		"$\3$\3$\3$\3$\7$\u0149\n$\f$\16$\u014c\13$\3%\3%\3%\3%\3%\3%\7%\u0154"+
		"\n%\f%\16%\u0157\13%\3&\3&\3&\3&\3&\3&\7&\u015f\n&\f&\16&\u0162\13&\3"+
		"\'\3\'\3\'\3\'\3\'\3\'\7\'\u016a\n\'\f\'\16\'\u016d\13\'\3(\3(\3(\3(\3"+
		"(\3(\7(\u0175\n(\f(\16(\u0178\13(\3)\3)\3)\3)\3)\3)\7)\u0180\n)\f)\16"+
		")\u0183\13)\3*\3*\3*\3*\3*\3*\7*\u018b\n*\f*\16*\u018e\13*\3+\3+\3,\3"+
		",\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\5-\u01a5\n-\3.\3"+
		".\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\60\3\60\3\60\3\60\3\61\3\61"+
		"\3\61\3\61\3\61\3\61\3\62\3\62\3\62\3\63\3\63\3\63\2\64\2\4\6\b\n\f\16"+
		"\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bd"+
		"\2\3\4\2&\'88\u01c2\2f\3\2\2\2\4i\3\2\2\2\6\u0083\3\2\2\2\b\u0086\3\2"+
		"\2\2\n\u008e\3\2\2\2\f\u0091\3\2\2\2\16\u00aa\3\2\2\2\20\u00bf\3\2\2\2"+
		"\22\u00c1\3\2\2\2\24\u00c3\3\2\2\2\26\u00c5\3\2\2\2\30\u00c7\3\2\2\2\32"+
		"\u00c9\3\2\2\2\34\u00cb\3\2\2\2\36\u00cd\3\2\2\2 \u00cf\3\2\2\2\"\u00d1"+
		"\3\2\2\2$\u00d3\3\2\2\2&\u00d5\3\2\2\2(\u00d7\3\2\2\2*\u00d9\3\2\2\2,"+
		"\u00db\3\2\2\2.\u00dd\3\2\2\2\60\u00ec\3\2\2\2\62\u00f5\3\2\2\2\64\u00f8"+
		"\3\2\2\2\66\u00fd\3\2\2\28\u0105\3\2\2\2:\u010a\3\2\2\2<\u0113\3\2\2\2"+
		">\u011f\3\2\2\2@\u0127\3\2\2\2B\u012c\3\2\2\2D\u0137\3\2\2\2F\u0142\3"+
		"\2\2\2H\u014d\3\2\2\2J\u0158\3\2\2\2L\u0163\3\2\2\2N\u016e\3\2\2\2P\u0179"+
		"\3\2\2\2R\u0184\3\2\2\2T\u018f\3\2\2\2V\u0191\3\2\2\2X\u01a4\3\2\2\2Z"+
		"\u01a6\3\2\2\2\\\u01a8\3\2\2\2^\u01ae\3\2\2\2`\u01b6\3\2\2\2b\u01bc\3"+
		"\2\2\2d\u01bf\3\2\2\2fg\5\6\4\2gh\7\2\2\3h\3\3\2\2\2ij\7\23\2\2jk\7\13"+
		"\2\2kl\79\2\2lm\5\6\4\2mn\7:\2\2no\7%\2\2op\7\13\2\2pq\79\2\2qr\5\6\4"+
		"\2rs\7:\2\2s\5\3\2\2\2t\u0082\5\b\5\2u\u0082\5\66\34\2v\u0082\5:\36\2"+
		"w\u0082\5> \2x\u0082\5\20\t\2y\u0082\5\f\7\2z\u0082\5\16\b\2{\u0082\5"+
		"\\/\2|\u0082\5^\60\2}\u0082\5`\61\2~\u0082\5b\62\2\177\u0082\5\62\32\2"+
		"\u0080\u0082\5.\30\2\u0081t\3\2\2\2\u0081u\3\2\2\2\u0081v\3\2\2\2\u0081"+
		"w\3\2\2\2\u0081x\3\2\2\2\u0081y\3\2\2\2\u0081z\3\2\2\2\u0081{\3\2\2\2"+
		"\u0081|\3\2\2\2\u0081}\3\2\2\2\u0081~\3\2\2\2\u0081\177\3\2\2\2\u0081"+
		"\u0080\3\2\2\2\u0082\u0085\3\2\2\2\u0083\u0081\3\2\2\2\u0083\u0084\3\2"+
		"\2\2\u0084\7\3\2\2\2\u0085\u0083\3\2\2\2\u0086\u0087\7\21\2\2\u0087\u0088"+
		"\5\n\6\2\u0088\u0089\7\"\2\2\u0089\u008a\7-\2\2\u008a\t\3\2\2\2\u008b"+
		"\u008d\t\2\2\2\u008c\u008b\3\2\2\2\u008d\u0090\3\2\2\2\u008e\u008c\3\2"+
		"\2\2\u008e\u008f\3\2\2\2\u008f\13\3\2\2\2\u0090\u008e\3\2\2\2\u0091\u0092"+
		"\7\r\2\2\u0092\u0093\5V,\2\u0093\u0094\7\13\2\2\u0094\u0095\79\2\2\u0095"+
		"\u0096\5\6\4\2\u0096\u00a0\7:\2\2\u0097\u0098\7\36\2\2\u0098\u0099\5V"+
		",\2\u0099\u009a\7\13\2\2\u009a\u009b\79\2\2\u009b\u009c\5\6\4\2\u009c"+
		"\u009d\7:\2\2\u009d\u009f\3\2\2\2\u009e\u0097\3\2\2\2\u009f\u00a2\3\2"+
		"\2\2\u00a0\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a8\3\2\2\2\u00a2"+
		"\u00a0\3\2\2\2\u00a3\u00a4\7\33\2\2\u00a4\u00a5\79\2\2\u00a5\u00a6\5\6"+
		"\4\2\u00a6\u00a7\7:\2\2\u00a7\u00a9\3\2\2\2\u00a8\u00a3\3\2\2\2\u00a8"+
		"\u00a9\3\2\2\2\u00a9\r\3\2\2\2\u00aa\u00ab\7\t\2\2\u00ab\u00ac\5V,\2\u00ac"+
		"\u00ad\7\13\2\2\u00ad\u00ae\79\2\2\u00ae\u00af\5\6\4\2\u00af\u00b0\7:"+
		"\2\2\u00b0\17\3\2\2\2\u00b1\u00c0\5\22\n\2\u00b2\u00c0\5\24\13\2\u00b3"+
		"\u00c0\5\26\f\2\u00b4\u00c0\5\30\r\2\u00b5\u00c0\5\32\16\2\u00b6\u00c0"+
		"\5\34\17\2\u00b7\u00c0\5\36\20\2\u00b8\u00c0\5 \21\2\u00b9\u00c0\5\"\22"+
		"\2\u00ba\u00c0\5$\23\2\u00bb\u00c0\5&\24\2\u00bc\u00c0\5(\25\2\u00bd\u00c0"+
		"\5*\26\2\u00be\u00c0\5,\27\2\u00bf\u00b1\3\2\2\2\u00bf\u00b2\3\2\2\2\u00bf"+
		"\u00b3\3\2\2\2\u00bf\u00b4\3\2\2\2\u00bf\u00b5\3\2\2\2\u00bf\u00b6\3\2"+
		"\2\2\u00bf\u00b7\3\2\2\2\u00bf\u00b8\3\2\2\2\u00bf\u00b9\3\2\2\2\u00bf"+
		"\u00ba\3\2\2\2\u00bf\u00bb\3\2\2\2\u00bf\u00bc\3\2\2\2\u00bf\u00bd\3\2"+
		"\2\2\u00bf\u00be\3\2\2\2\u00c0\21\3\2\2\2\u00c1\u00c2\7\30\2\2\u00c2\23"+
		"\3\2\2\2\u00c3\u00c4\7 \2\2\u00c4\25\3\2\2\2\u00c5\u00c6\7\22\2\2\u00c6"+
		"\27\3\2\2\2\u00c7\u00c8\7\32\2\2\u00c8\31\3\2\2\2\u00c9\u00ca\7\34\2\2"+
		"\u00ca\33\3\2\2\2\u00cb\u00cc\7\b\2\2\u00cc\35\3\2\2\2\u00cd\u00ce\7!"+
		"\2\2\u00ce\37\3\2\2\2\u00cf\u00d0\7\20\2\2\u00d0!\3\2\2\2\u00d1\u00d2"+
		"\7\24\2\2\u00d2#\3\2\2\2\u00d3\u00d4\7\5\2\2\u00d4%\3\2\2\2\u00d5\u00d6"+
		"\7\17\2\2\u00d6\'\3\2\2\2\u00d7\u00d8\7$\2\2\u00d8)\3\2\2\2\u00d9\u00da"+
		"\7\27\2\2\u00da+\3\2\2\2\u00db\u00dc\7#\2\2\u00dc-\3\2\2\2\u00dd\u00de"+
		"\7\7\2\2\u00de\u00df\7\f\2\2\u00df\u00e0\5X-\2\u00e0\u00e1\7\6\2\2\u00e1"+
		"\u00e2\5X-\2\u00e2\u00e3\7\6\2\2\u00e3\u00e4\5X-\2\u00e4\u00e5\7\6\2\2"+
		"\u00e5\u00e6\5<\37\2\u00e6\u00e7\7\6\2\2\u00e7\u00e8\5X-\2\u00e8\u00e9"+
		"\7\6\2\2\u00e9\u00ea\5X-\2\u00ea\u00eb\7\31\2\2\u00eb/\3\2\2\2\u00ec\u00ed"+
		"\7\16\2\2\u00ed\u00ee\7\f\2\2\u00ee\u00ef\5X-\2\u00ef\u00f0\7\6\2\2\u00f0"+
		"\u00f1\5X-\2\u00f1\u00f2\7\6\2\2\u00f2\u00f3\5X-\2\u00f3\u00f4\7\31\2"+
		"\2\u00f4\61\3\2\2\2\u00f5\u00f6\5\60\31\2\u00f6\u00f7\7-\2\2\u00f7\63"+
		"\3\2\2\2\u00f8\u00f9\7\35\2\2\u00f9\u00fa\7\n\2\2\u00fa\u00fb\5T+\2\u00fb"+
		"\u00fc\7\3\2\2\u00fc\65\3\2\2\2\u00fd\u00fe\7\60\2\2\u00fe\u00ff\7\n\2"+
		"\2\u00ff\u0100\5X-\2\u0100\u0101\7\3\2\2\u0101\u0102\7,\2\2\u0102\u0103"+
		"\5T+\2\u0103\u0104\7-\2\2\u0104\67\3\2\2\2\u0105\u0106\7\60\2\2\u0106"+
		"\u0107\7\n\2\2\u0107\u0108\5X-\2\u0108\u0109\7\3\2\2\u01099\3\2\2\2\u010a"+
		"\u010b\7\60\2\2\u010b\u010f\7,\2\2\u010c\u0110\5T+\2\u010d\u0110\5<\37"+
		"\2\u010e\u0110\5.\30\2\u010f\u010c\3\2\2\2\u010f\u010d\3\2\2\2\u010f\u010e"+
		"\3\2\2\2\u0110\u0111\3\2\2\2\u0111\u0112\7-\2\2\u0112;\3\2\2\2\u0113\u011a"+
		"\7\n\2\2\u0114\u0116\5X-\2\u0115\u0117\7\6\2\2\u0116\u0115\3\2\2\2\u0116"+
		"\u0117\3\2\2\2\u0117\u0119\3\2\2\2\u0118\u0114\3\2\2\2\u0119\u011c\3\2"+
		"\2\2\u011a\u0118\3\2\2\2\u011a\u011b\3\2\2\2\u011b\u011d\3\2\2\2\u011c"+
		"\u011a\3\2\2\2\u011d\u011e\7\3\2\2\u011e=\3\2\2\2\u011f\u0120\7\25\2\2"+
		"\u0120\u0121\7\n\2\2\u0121\u0122\5T+\2\u0122\u0123\7\3\2\2\u0123\u0124"+
		"\7,\2\2\u0124\u0125\5T+\2\u0125\u0126\7-\2\2\u0126?\3\2\2\2\u0127\u0128"+
		"\7\25\2\2\u0128\u0129\7\n\2\2\u0129\u012a\5T+\2\u012a\u012b\7\3\2\2\u012b"+
		"A\3\2\2\2\u012c\u012d\b\"\1\2\u012d\u012e\5X-\2\u012e\u0134\3\2\2\2\u012f"+
		"\u0130\6\"\2\3\u0130\u0131\7\62\2\2\u0131\u0133\5X-\2\u0132\u012f\3\2"+
		"\2\2\u0133\u0136\3\2\2\2\u0134\u0132\3\2\2\2\u0134\u0135\3\2\2\2\u0135"+
		"C\3\2\2\2\u0136\u0134\3\2\2\2\u0137\u0138\b#\1\2\u0138\u0139\5B\"\2\u0139"+
		"\u013f\3\2\2\2\u013a\u013b\6#\3\3\u013b\u013c\7\61\2\2\u013c\u013e\5B"+
		"\"\2\u013d\u013a\3\2\2\2\u013e\u0141\3\2\2\2\u013f\u013d\3\2\2\2\u013f"+
		"\u0140\3\2\2\2\u0140E\3\2\2\2\u0141\u013f\3\2\2\2\u0142\u0143\b$\1\2\u0143"+
		"\u0144\5D#\2\u0144\u014a\3\2\2\2\u0145\u0146\6$\4\3\u0146\u0147\7\63\2"+
		"\2\u0147\u0149\5D#\2\u0148\u0145\3\2\2\2\u0149\u014c\3\2\2\2\u014a\u0148"+
		"\3\2\2\2\u014a\u014b\3\2\2\2\u014bG\3\2\2\2\u014c\u014a\3\2\2\2\u014d"+
		"\u014e\b%\1\2\u014e\u014f\5F$\2\u014f\u0155\3\2\2\2\u0150\u0151\6%\5\3"+
		"\u0151\u0152\7\64\2\2\u0152\u0154\5F$\2\u0153\u0150\3\2\2\2\u0154\u0157"+
		"\3\2\2\2\u0155\u0153\3\2\2\2\u0155\u0156\3\2\2\2\u0156I\3\2\2\2\u0157"+
		"\u0155\3\2\2\2\u0158\u0159\b&\1\2\u0159\u015a\5H%\2\u015a\u0160\3\2\2"+
		"\2\u015b\u015c\6&\6\3\u015c\u015d\7\65\2\2\u015d\u015f\5H%\2\u015e\u015b"+
		"\3\2\2\2\u015f\u0162\3\2\2\2\u0160\u015e\3\2\2\2\u0160\u0161\3\2\2\2\u0161"+
		"K\3\2\2\2\u0162\u0160\3\2\2\2\u0163\u0164\b\'\1\2\u0164\u0165\5J&\2\u0165"+
		"\u016b\3\2\2\2\u0166\u0167\6\'\7\3\u0167\u0168\7(\2\2\u0168\u016a\5J&"+
		"\2\u0169\u0166\3\2\2\2\u016a\u016d\3\2\2\2\u016b\u0169\3\2\2\2\u016b\u016c"+
		"\3\2\2\2\u016cM\3\2\2\2\u016d\u016b\3\2\2\2\u016e\u016f\b(\1\2\u016f\u0170"+
		"\5L\'\2\u0170\u0176\3\2\2\2\u0171\u0172\6(\b\3\u0172\u0173\7\66\2\2\u0173"+
		"\u0175\5L\'\2\u0174\u0171\3\2\2\2\u0175\u0178\3\2\2\2\u0176\u0174\3\2"+
		"\2\2\u0176\u0177\3\2\2\2\u0177O\3\2\2\2\u0178\u0176\3\2\2\2\u0179\u017a"+
		"\b)\1\2\u017a\u017b\5N(\2\u017b\u0181\3\2\2\2\u017c\u017d\6)\t\3\u017d"+
		"\u017e\7)\2\2\u017e\u0180\5N(\2\u017f\u017c\3\2\2\2\u0180\u0183\3\2\2"+
		"\2\u0181\u017f\3\2\2\2\u0181\u0182\3\2\2\2\u0182Q\3\2\2\2\u0183\u0181"+
		"\3\2\2\2\u0184\u0185\b*\1\2\u0185\u0186\5P)\2\u0186\u018c\3\2\2\2\u0187"+
		"\u0188\6*\n\3\u0188\u0189\7*\2\2\u0189\u018b\5P)\2\u018a\u0187\3\2\2\2"+
		"\u018b\u018e\3\2\2\2\u018c\u018a\3\2\2\2\u018c\u018d\3\2\2\2\u018dS\3"+
		"\2\2\2\u018e\u018c\3\2\2\2\u018f\u0190\5R*\2\u0190U\3\2\2\2\u0191\u0192"+
		"\5T+\2\u0192W\3\2\2\2\u0193\u01a5\7&\2\2\u0194\u01a5\5Z.\2\u0195\u01a5"+
		"\5d\63\2\u0196\u01a5\5\20\t\2\u0197\u0198\7\f\2\2\u0198\u0199\5T+\2\u0199"+
		"\u019a\7\31\2\2\u019a\u01a5\3\2\2\2\u019b\u019c\7+\2\2\u019c\u019d\7\f"+
		"\2\2\u019d\u019e\5T+\2\u019e\u019f\7\31\2\2\u019f\u01a5\3\2\2\2\u01a0"+
		"\u01a5\5\64\33\2\u01a1\u01a5\5\60\31\2\u01a2\u01a5\5@!\2\u01a3\u01a5\5"+
		"8\35\2\u01a4\u0193\3\2\2\2\u01a4\u0194\3\2\2\2\u01a4\u0195\3\2\2\2\u01a4"+
		"\u0196\3\2\2\2\u01a4\u0197\3\2\2\2\u01a4\u019b\3\2\2\2\u01a4\u01a0\3\2"+
		"\2\2\u01a4\u01a1\3\2\2\2\u01a4\u01a2\3\2\2\2\u01a4\u01a3\3\2\2\2\u01a5"+
		"Y\3\2\2\2\u01a6\u01a7\78\2\2\u01a7[\3\2\2\2\u01a8\u01a9\7\37\2\2\u01a9"+
		"\u01aa\7\f\2\2\u01aa\u01ab\5T+\2\u01ab\u01ac\7\31\2\2\u01ac\u01ad\7-\2"+
		"\2\u01ad]\3\2\2\2\u01ae\u01af\7\37\2\2\u01af\u01b0\7\f\2\2\u01b0\u01b1"+
		"\5T+\2\u01b1\u01b2\7\6\2\2\u01b2\u01b3\5T+\2\u01b3\u01b4\7\31\2\2\u01b4"+
		"\u01b5\7-\2\2\u01b5_\3\2\2\2\u01b6\u01b7\7\26\2\2\u01b7\u01b8\7\f\2\2"+
		"\u01b8\u01b9\5T+\2\u01b9\u01ba\7\31\2\2\u01ba\u01bb\7-\2\2\u01bba\3\2"+
		"\2\2\u01bc\u01bd\7\4\2\2\u01bd\u01be\7-\2\2\u01bec\3\2\2\2\u01bf\u01c0"+
		"\7\60\2\2\u01c0e\3\2\2\2\25\u0081\u0083\u008e\u00a0\u00a8\u00bf\u010f"+
		"\u0116\u011a\u0134\u013f\u014a\u0155\u0160\u016b\u0176\u0181\u018c\u01a4";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}