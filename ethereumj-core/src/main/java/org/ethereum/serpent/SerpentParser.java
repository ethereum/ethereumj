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
		RULE_msg_func = 21, RULE_send_func = 22, RULE_msg_data = 23, RULE_array_assign = 24, 
		RULE_array_retreive = 25, RULE_assign = 26, RULE_arr_def = 27, RULE_contract_storage_assign = 28, 
		RULE_contract_storage_load = 29, RULE_mul_expr = 30, RULE_add_expr = 31, 
		RULE_rel_exp = 32, RULE_eq_exp = 33, RULE_and_exp = 34, RULE_ex_or_exp = 35, 
		RULE_in_or_exp = 36, RULE_log_and_exp = 37, RULE_log_or_exp = 38, RULE_expression = 39, 
		RULE_condition = 40, RULE_int_val = 41, RULE_hex_num = 42, RULE_ret_func_1 = 43, 
		RULE_ret_func_2 = 44, RULE_suicide_func = 45, RULE_stop_func = 46, RULE_get_var = 47;
	public static final String[] ruleNames = {
		"parse", "parse_init_code_block", "block", "asm", "asm_symbol", "if_elif_else_stmt", 
		"while_stmt", "special_func", "msg_datasize", "msg_sender", "msg_value", 
		"tx_gasprice", "tx_origin", "tx_gas", "contract_balance", "block_prevhash", 
		"block_coinbase", "block_timestamp", "block_number", "block_difficulty", 
		"block_gaslimit", "msg_func", "send_func", "msg_data", "array_assign", 
		"array_retreive", "assign", "arr_def", "contract_storage_assign", "contract_storage_load", 
		"mul_expr", "add_expr", "rel_exp", "eq_exp", "and_exp", "ex_or_exp", "in_or_exp", 
		"log_and_exp", "log_or_exp", "expression", "condition", "int_val", "hex_num", 
		"ret_func_1", "ret_func_2", "suicide_func", "stop_func", "get_var"
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
			setState(96); block();
			setState(97); match(EOF);
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
			setState(99); match(16);
			setState(100); match(10);
			setState(101); match(INDENT);
			setState(102); block();
			setState(103); match(DEDENT);
			setState(104); match(34);
			setState(105); match(10);
			setState(106); match(INDENT);
			setState(107); block();
			setState(108); match(DEDENT);
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
			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 3) | (1L << 6) | (1L << 7) | (1L << 11) | (1L << 13) | (1L << 14) | (1L << 15) | (1L << 17) | (1L << 18) | (1L << 19) | (1L << 20) | (1L << 21) | (1L << 23) | (1L << 25) | (1L << 28) | (1L << 29) | (1L << 30) | (1L << 32) | (1L << 33) | (1L << VAR))) != 0)) {
				{
				setState(121);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(110); asm();
					}
					break;

				case 2:
					{
					setState(111); array_assign();
					}
					break;

				case 3:
					{
					setState(112); assign();
					}
					break;

				case 4:
					{
					setState(113); contract_storage_assign();
					}
					break;

				case 5:
					{
					setState(114); special_func();
					}
					break;

				case 6:
					{
					setState(115); if_elif_else_stmt();
					}
					break;

				case 7:
					{
					setState(116); while_stmt();
					}
					break;

				case 8:
					{
					setState(117); ret_func_1();
					}
					break;

				case 9:
					{
					setState(118); ret_func_2();
					}
					break;

				case 10:
					{
					setState(119); suicide_func();
					}
					break;

				case 11:
					{
					setState(120); stop_func();
					}
					break;
				}
				}
				setState(125);
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
			setState(126); match(14);
			setState(127); asm_symbol();
			setState(128); match(31);
			setState(129); match(NL);
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
			setState(134);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INT) | (1L << ASM_SYMBOLS) | (1L << HEX_NUMBER))) != 0)) {
				{
				{
				setState(131);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INT) | (1L << ASM_SYMBOLS) | (1L << HEX_NUMBER))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				}
				setState(136);
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
			setState(137); match(11);
			setState(138); condition();
			setState(139); match(10);
			setState(140); match(INDENT);
			setState(141); block();
			setState(142); match(DEDENT);
			setState(152);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==27) {
				{
				{
				setState(143); match(27);
				setState(144); condition();
				setState(145); match(10);
				setState(146); match(INDENT);
				setState(147); block();
				setState(148); match(DEDENT);
				}
				}
				setState(154);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(160);
			_la = _input.LA(1);
			if (_la==24) {
				{
				setState(155); match(24);
				setState(156); match(INDENT);
				setState(157); block();
				setState(158); match(DEDENT);
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
			setState(162); match(7);
			setState(163); condition();
			setState(164); match(10);
			setState(165); match(INDENT);
			setState(166); block();
			setState(167); match(DEDENT);
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
			setState(182);
			switch (_input.LA(1)) {
			case 21:
				enterOuterAlt(_localctx, 1);
				{
				setState(169); msg_datasize();
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 2);
				{
				setState(170); msg_sender();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 3);
				{
				setState(171); msg_value();
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 4);
				{
				setState(172); tx_gasprice();
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 5);
				{
				setState(173); tx_origin();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(174); tx_gas();
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 7);
				{
				setState(175); contract_balance();
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 8);
				{
				setState(176); block_prevhash();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 9);
				{
				setState(177); block_coinbase();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 10);
				{
				setState(178); block_timestamp();
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 11);
				{
				setState(179); block_number();
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 12);
				{
				setState(180); block_difficulty();
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 13);
				{
				setState(181); block_gaslimit();
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
			setState(184); match(21);
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
			setState(186); match(29);
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
			setState(188); match(15);
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
			setState(190); match(23);
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
			setState(192); match(25);
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
			setState(194); match(6);
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
			setState(196); match(30);
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
			setState(198); match(17);
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
			setState(200); match(3);
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
			setState(202); match(13);
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
			setState(204); match(33);
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
			setState(206); match(20);
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
			setState(208); match(32);
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
			setState(210); match(5);
			setState(211); match(9);
			setState(212); int_val();
			setState(213); match(4);
			setState(214); int_val();
			setState(215); match(4);
			setState(216); int_val();
			setState(217); match(4);
			setState(218); int_val();
			setState(219); match(4);
			setState(220); int_val();
			setState(221); match(22);
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
			setState(223); match(12);
			setState(224); match(9);
			setState(225); int_val();
			setState(226); match(4);
			setState(227); int_val();
			setState(228); match(4);
			setState(229); int_val();
			setState(230); match(22);
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
		enterRule(_localctx, 46, RULE_msg_data);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232); match(26);
			setState(233); match(8);
			setState(234); expression();
			setState(235); match(1);
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
		enterRule(_localctx, 48, RULE_array_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(237); match(VAR);
			setState(238); match(8);
			setState(239); int_val();
			setState(240); match(1);
			setState(241); match(EQ_OP);
			setState(242); expression();
			setState(243); match(NL);
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
		enterRule(_localctx, 50, RULE_array_retreive);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245); match(VAR);
			setState(246); match(8);
			setState(247); int_val();
			setState(248); match(1);
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
		enterRule(_localctx, 52, RULE_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(250); match(VAR);
			setState(251); match(EQ_OP);
			setState(254);
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
				setState(252); expression();
				}
				break;
			case 8:
				{
				setState(253); arr_def();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(256); match(NL);
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
		enterRule(_localctx, 54, RULE_arr_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(258); match(8);
			setState(265);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 3) | (1L << 5) | (1L << 6) | (1L << 9) | (1L << 12) | (1L << 13) | (1L << 15) | (1L << 17) | (1L << 18) | (1L << 20) | (1L << 21) | (1L << 23) | (1L << 25) | (1L << 26) | (1L << 29) | (1L << 30) | (1L << 32) | (1L << 33) | (1L << INT) | (1L << OP_NOT) | (1L << VAR) | (1L << HEX_NUMBER))) != 0)) {
				{
				{
				setState(259); int_val();
				setState(261);
				_la = _input.LA(1);
				if (_la==4) {
					{
					setState(260); match(4);
					}
				}

				}
				}
				setState(267);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(268); match(1);
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
		enterRule(_localctx, 56, RULE_contract_storage_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(270); match(18);
			setState(271); match(8);
			setState(272); expression();
			setState(273); match(1);
			setState(274); match(EQ_OP);
			setState(275); expression();
			setState(276); match(NL);
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
		enterRule(_localctx, 58, RULE_contract_storage_load);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(278); match(18);
			setState(279); match(8);
			setState(280); expression();
			setState(281); match(1);
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
		int _startState = 60;
		enterRecursionRule(_localctx, RULE_mul_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(284); int_val();
			}
			_ctx.stop = _input.LT(-1);
			setState(291);
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
					setState(286);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(287); match(OP_MUL);
					setState(288); int_val();
					}
					} 
				}
				setState(293);
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
		int _startState = 62;
		enterRecursionRule(_localctx, RULE_add_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(295); mul_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(302);
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
					setState(297);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(298); match(OP_ADD);
					setState(299); mul_expr(0);
					}
					} 
				}
				setState(304);
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
		int _startState = 64;
		enterRecursionRule(_localctx, RULE_rel_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(306); add_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(313);
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
					setState(308);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(309); match(OP_REL);
					setState(310); add_expr(0);
					}
					} 
				}
				setState(315);
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
		int _startState = 66;
		enterRecursionRule(_localctx, RULE_eq_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(317); rel_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(324);
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
					setState(319);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(320); match(OP_EQ);
					setState(321); rel_exp(0);
					}
					} 
				}
				setState(326);
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
		int _startState = 68;
		enterRecursionRule(_localctx, RULE_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(328); eq_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(335);
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
					setState(330);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(331); match(OP_AND);
					setState(332); eq_exp(0);
					}
					} 
				}
				setState(337);
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
		int _startState = 70;
		enterRecursionRule(_localctx, RULE_ex_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(339); and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(346);
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
					setState(341);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(342); match(OP_EX_OR);
					setState(343); and_exp(0);
					}
					} 
				}
				setState(348);
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
		int _startState = 72;
		enterRecursionRule(_localctx, RULE_in_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(350); ex_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(357);
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
					setState(352);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(353); match(OP_IN_OR);
					setState(354); ex_or_exp(0);
					}
					} 
				}
				setState(359);
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
		int _startState = 74;
		enterRecursionRule(_localctx, RULE_log_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(361); in_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(368);
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
					setState(363);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(364); match(OP_LOG_AND);
					setState(365); in_or_exp(0);
					}
					} 
				}
				setState(370);
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
		int _startState = 76;
		enterRecursionRule(_localctx, RULE_log_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(372); log_and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(379);
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
					setState(374);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(375); match(OP_LOG_OR);
					setState(376); log_and_exp(0);
					}
					} 
				}
				setState(381);
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
		enterRule(_localctx, 78, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(382); log_or_exp(0);
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
		enterRule(_localctx, 80, RULE_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(384); expression();
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
		enterRule(_localctx, 82, RULE_int_val);
		try {
			setState(404);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(386); match(INT);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(387); hex_num();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(388); get_var();
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(389); special_func();
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(390); match(9);
				setState(391); expression();
				setState(392); match(22);
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(394); match(OP_NOT);
				setState(395); match(9);
				setState(396); expression();
				setState(397); match(22);
				}
				break;

			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(399); msg_func();
				}
				break;

			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(400); msg_data();
				}
				break;

			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(401); send_func();
				}
				break;

			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(402); contract_storage_load();
				}
				break;

			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(403); array_retreive();
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
		enterRule(_localctx, 84, RULE_hex_num);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(406); match(HEX_NUMBER);
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
		enterRule(_localctx, 86, RULE_ret_func_1);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(408); match(28);
			setState(409); match(9);
			setState(410); expression();
			setState(411); match(22);
			setState(412); match(NL);
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
		enterRule(_localctx, 88, RULE_ret_func_2);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(414); match(28);
			setState(415); match(9);
			setState(416); expression();
			setState(417); match(4);
			setState(418); expression();
			setState(419); match(22);
			setState(420); match(NL);
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
		enterRule(_localctx, 90, RULE_suicide_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(422); match(19);
			setState(423); match(9);
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
		enterRule(_localctx, 92, RULE_stop_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428); match(2);
			setState(429); match(NL);
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
		enterRule(_localctx, 94, RULE_get_var);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(431); match(VAR);
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
		case 30: return mul_expr_sempred((Mul_exprContext)_localctx, predIndex);

		case 31: return add_expr_sempred((Add_exprContext)_localctx, predIndex);

		case 32: return rel_exp_sempred((Rel_expContext)_localctx, predIndex);

		case 33: return eq_exp_sempred((Eq_expContext)_localctx, predIndex);

		case 34: return and_exp_sempred((And_expContext)_localctx, predIndex);

		case 35: return ex_or_exp_sempred((Ex_or_expContext)_localctx, predIndex);

		case 36: return in_or_exp_sempred((In_or_expContext)_localctx, predIndex);

		case 37: return log_and_exp_sempred((Log_and_expContext)_localctx, predIndex);

		case 38: return log_or_exp_sempred((Log_or_expContext)_localctx, predIndex);
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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\39\u01b4\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\3\2\3\2\3\2\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\4\7\4|\n\4\f\4\16\4\177\13\4\3\5\3\5\3\5\3\5\3\5\3\6\7\6\u0087\n\6\f"+
		"\6\16\6\u008a\13\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\7\7\u0099\n\7\f\7\16\7\u009c\13\7\3\7\3\7\3\7\3\7\3\7\5\7\u00a3\n\7\3"+
		"\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\5\t\u00b9\n\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17"+
		"\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26"+
		"\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\34"+
		"\3\34\3\34\3\34\5\34\u0101\n\34\3\34\3\34\3\35\3\35\3\35\5\35\u0108\n"+
		"\35\7\35\u010a\n\35\f\35\16\35\u010d\13\35\3\35\3\35\3\36\3\36\3\36\3"+
		"\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3 \3 \7 "+
		"\u0124\n \f \16 \u0127\13 \3!\3!\3!\3!\3!\3!\7!\u012f\n!\f!\16!\u0132"+
		"\13!\3\"\3\"\3\"\3\"\3\"\3\"\7\"\u013a\n\"\f\"\16\"\u013d\13\"\3#\3#\3"+
		"#\3#\3#\3#\7#\u0145\n#\f#\16#\u0148\13#\3$\3$\3$\3$\3$\3$\7$\u0150\n$"+
		"\f$\16$\u0153\13$\3%\3%\3%\3%\3%\3%\7%\u015b\n%\f%\16%\u015e\13%\3&\3"+
		"&\3&\3&\3&\3&\7&\u0166\n&\f&\16&\u0169\13&\3\'\3\'\3\'\3\'\3\'\3\'\7\'"+
		"\u0171\n\'\f\'\16\'\u0174\13\'\3(\3(\3(\3(\3(\3(\7(\u017c\n(\f(\16(\u017f"+
		"\13(\3)\3)\3*\3*\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3"+
		"+\5+\u0197\n+\3,\3,\3-\3-\3-\3-\3-\3-\3.\3.\3.\3.\3.\3.\3.\3.\3/\3/\3"+
		"/\3/\3/\3/\3\60\3\60\3\60\3\61\3\61\3\61\2\62\2\4\6\b\n\f\16\20\22\24"+
		"\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`\2\3\4\2%&\67"+
		"\67\u01b3\2b\3\2\2\2\4e\3\2\2\2\6}\3\2\2\2\b\u0080\3\2\2\2\n\u0088\3\2"+
		"\2\2\f\u008b\3\2\2\2\16\u00a4\3\2\2\2\20\u00b8\3\2\2\2\22\u00ba\3\2\2"+
		"\2\24\u00bc\3\2\2\2\26\u00be\3\2\2\2\30\u00c0\3\2\2\2\32\u00c2\3\2\2\2"+
		"\34\u00c4\3\2\2\2\36\u00c6\3\2\2\2 \u00c8\3\2\2\2\"\u00ca\3\2\2\2$\u00cc"+
		"\3\2\2\2&\u00ce\3\2\2\2(\u00d0\3\2\2\2*\u00d2\3\2\2\2,\u00d4\3\2\2\2."+
		"\u00e1\3\2\2\2\60\u00ea\3\2\2\2\62\u00ef\3\2\2\2\64\u00f7\3\2\2\2\66\u00fc"+
		"\3\2\2\28\u0104\3\2\2\2:\u0110\3\2\2\2<\u0118\3\2\2\2>\u011d\3\2\2\2@"+
		"\u0128\3\2\2\2B\u0133\3\2\2\2D\u013e\3\2\2\2F\u0149\3\2\2\2H\u0154\3\2"+
		"\2\2J\u015f\3\2\2\2L\u016a\3\2\2\2N\u0175\3\2\2\2P\u0180\3\2\2\2R\u0182"+
		"\3\2\2\2T\u0196\3\2\2\2V\u0198\3\2\2\2X\u019a\3\2\2\2Z\u01a0\3\2\2\2\\"+
		"\u01a8\3\2\2\2^\u01ae\3\2\2\2`\u01b1\3\2\2\2bc\5\6\4\2cd\7\2\2\3d\3\3"+
		"\2\2\2ef\7\22\2\2fg\7\f\2\2gh\78\2\2hi\5\6\4\2ij\79\2\2jk\7$\2\2kl\7\f"+
		"\2\2lm\78\2\2mn\5\6\4\2no\79\2\2o\5\3\2\2\2p|\5\b\5\2q|\5\62\32\2r|\5"+
		"\66\34\2s|\5:\36\2t|\5\20\t\2u|\5\f\7\2v|\5\16\b\2w|\5X-\2x|\5Z.\2y|\5"+
		"\\/\2z|\5^\60\2{p\3\2\2\2{q\3\2\2\2{r\3\2\2\2{s\3\2\2\2{t\3\2\2\2{u\3"+
		"\2\2\2{v\3\2\2\2{w\3\2\2\2{x\3\2\2\2{y\3\2\2\2{z\3\2\2\2|\177\3\2\2\2"+
		"}{\3\2\2\2}~\3\2\2\2~\7\3\2\2\2\177}\3\2\2\2\u0080\u0081\7\20\2\2\u0081"+
		"\u0082\5\n\6\2\u0082\u0083\7!\2\2\u0083\u0084\7,\2\2\u0084\t\3\2\2\2\u0085"+
		"\u0087\t\2\2\2\u0086\u0085\3\2\2\2\u0087\u008a\3\2\2\2\u0088\u0086\3\2"+
		"\2\2\u0088\u0089\3\2\2\2\u0089\13\3\2\2\2\u008a\u0088\3\2\2\2\u008b\u008c"+
		"\7\r\2\2\u008c\u008d\5R*\2\u008d\u008e\7\f\2\2\u008e\u008f\78\2\2\u008f"+
		"\u0090\5\6\4\2\u0090\u009a\79\2\2\u0091\u0092\7\35\2\2\u0092\u0093\5R"+
		"*\2\u0093\u0094\7\f\2\2\u0094\u0095\78\2\2\u0095\u0096\5\6\4\2\u0096\u0097"+
		"\79\2\2\u0097\u0099\3\2\2\2\u0098\u0091\3\2\2\2\u0099\u009c\3\2\2\2\u009a"+
		"\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b\u00a2\3\2\2\2\u009c\u009a\3\2"+
		"\2\2\u009d\u009e\7\32\2\2\u009e\u009f\78\2\2\u009f\u00a0\5\6\4\2\u00a0"+
		"\u00a1\79\2\2\u00a1\u00a3\3\2\2\2\u00a2\u009d\3\2\2\2\u00a2\u00a3\3\2"+
		"\2\2\u00a3\r\3\2\2\2\u00a4\u00a5\7\t\2\2\u00a5\u00a6\5R*\2\u00a6\u00a7"+
		"\7\f\2\2\u00a7\u00a8\78\2\2\u00a8\u00a9\5\6\4\2\u00a9\u00aa\79\2\2\u00aa"+
		"\17\3\2\2\2\u00ab\u00b9\5\22\n\2\u00ac\u00b9\5\24\13\2\u00ad\u00b9\5\26"+
		"\f\2\u00ae\u00b9\5\30\r\2\u00af\u00b9\5\32\16\2\u00b0\u00b9\5\34\17\2"+
		"\u00b1\u00b9\5\36\20\2\u00b2\u00b9\5 \21\2\u00b3\u00b9\5\"\22\2\u00b4"+
		"\u00b9\5$\23\2\u00b5\u00b9\5&\24\2\u00b6\u00b9\5(\25\2\u00b7\u00b9\5*"+
		"\26\2\u00b8\u00ab\3\2\2\2\u00b8\u00ac\3\2\2\2\u00b8\u00ad\3\2\2\2\u00b8"+
		"\u00ae\3\2\2\2\u00b8\u00af\3\2\2\2\u00b8\u00b0\3\2\2\2\u00b8\u00b1\3\2"+
		"\2\2\u00b8\u00b2\3\2\2\2\u00b8\u00b3\3\2\2\2\u00b8\u00b4\3\2\2\2\u00b8"+
		"\u00b5\3\2\2\2\u00b8\u00b6\3\2\2\2\u00b8\u00b7\3\2\2\2\u00b9\21\3\2\2"+
		"\2\u00ba\u00bb\7\27\2\2\u00bb\23\3\2\2\2\u00bc\u00bd\7\37\2\2\u00bd\25"+
		"\3\2\2\2\u00be\u00bf\7\21\2\2\u00bf\27\3\2\2\2\u00c0\u00c1\7\31\2\2\u00c1"+
		"\31\3\2\2\2\u00c2\u00c3\7\33\2\2\u00c3\33\3\2\2\2\u00c4\u00c5\7\b\2\2"+
		"\u00c5\35\3\2\2\2\u00c6\u00c7\7 \2\2\u00c7\37\3\2\2\2\u00c8\u00c9\7\23"+
		"\2\2\u00c9!\3\2\2\2\u00ca\u00cb\7\5\2\2\u00cb#\3\2\2\2\u00cc\u00cd\7\17"+
		"\2\2\u00cd%\3\2\2\2\u00ce\u00cf\7#\2\2\u00cf\'\3\2\2\2\u00d0\u00d1\7\26"+
		"\2\2\u00d1)\3\2\2\2\u00d2\u00d3\7\"\2\2\u00d3+\3\2\2\2\u00d4\u00d5\7\7"+
		"\2\2\u00d5\u00d6\7\13\2\2\u00d6\u00d7\5T+\2\u00d7\u00d8\7\6\2\2\u00d8"+
		"\u00d9\5T+\2\u00d9\u00da\7\6\2\2\u00da\u00db\5T+\2\u00db\u00dc\7\6\2\2"+
		"\u00dc\u00dd\5T+\2\u00dd\u00de\7\6\2\2\u00de\u00df\5T+\2\u00df\u00e0\7"+
		"\30\2\2\u00e0-\3\2\2\2\u00e1\u00e2\7\16\2\2\u00e2\u00e3\7\13\2\2\u00e3"+
		"\u00e4\5T+\2\u00e4\u00e5\7\6\2\2\u00e5\u00e6\5T+\2\u00e6\u00e7\7\6\2\2"+
		"\u00e7\u00e8\5T+\2\u00e8\u00e9\7\30\2\2\u00e9/\3\2\2\2\u00ea\u00eb\7\34"+
		"\2\2\u00eb\u00ec\7\n\2\2\u00ec\u00ed\5P)\2\u00ed\u00ee\7\3\2\2\u00ee\61"+
		"\3\2\2\2\u00ef\u00f0\7/\2\2\u00f0\u00f1\7\n\2\2\u00f1\u00f2\5T+\2\u00f2"+
		"\u00f3\7\3\2\2\u00f3\u00f4\7+\2\2\u00f4\u00f5\5P)\2\u00f5\u00f6\7,\2\2"+
		"\u00f6\63\3\2\2\2\u00f7\u00f8\7/\2\2\u00f8\u00f9\7\n\2\2\u00f9\u00fa\5"+
		"T+\2\u00fa\u00fb\7\3\2\2\u00fb\65\3\2\2\2\u00fc\u00fd\7/\2\2\u00fd\u0100"+
		"\7+\2\2\u00fe\u0101\5P)\2\u00ff\u0101\58\35\2\u0100\u00fe\3\2\2\2\u0100"+
		"\u00ff\3\2\2\2\u0101\u0102\3\2\2\2\u0102\u0103\7,\2\2\u0103\67\3\2\2\2"+
		"\u0104\u010b\7\n\2\2\u0105\u0107\5T+\2\u0106\u0108\7\6\2\2\u0107\u0106"+
		"\3\2\2\2\u0107\u0108\3\2\2\2\u0108\u010a\3\2\2\2\u0109\u0105\3\2\2\2\u010a"+
		"\u010d\3\2\2\2\u010b\u0109\3\2\2\2\u010b\u010c\3\2\2\2\u010c\u010e\3\2"+
		"\2\2\u010d\u010b\3\2\2\2\u010e\u010f\7\3\2\2\u010f9\3\2\2\2\u0110\u0111"+
		"\7\24\2\2\u0111\u0112\7\n\2\2\u0112\u0113\5P)\2\u0113\u0114\7\3\2\2\u0114"+
		"\u0115\7+\2\2\u0115\u0116\5P)\2\u0116\u0117\7,\2\2\u0117;\3\2\2\2\u0118"+
		"\u0119\7\24\2\2\u0119\u011a\7\n\2\2\u011a\u011b\5P)\2\u011b\u011c\7\3"+
		"\2\2\u011c=\3\2\2\2\u011d\u011e\b \1\2\u011e\u011f\5T+\2\u011f\u0125\3"+
		"\2\2\2\u0120\u0121\6 \2\3\u0121\u0122\7\61\2\2\u0122\u0124\5T+\2\u0123"+
		"\u0120\3\2\2\2\u0124\u0127\3\2\2\2\u0125\u0123\3\2\2\2\u0125\u0126\3\2"+
		"\2\2\u0126?\3\2\2\2\u0127\u0125\3\2\2\2\u0128\u0129\b!\1\2\u0129\u012a"+
		"\5> \2\u012a\u0130\3\2\2\2\u012b\u012c\6!\3\3\u012c\u012d\7\60\2\2\u012d"+
		"\u012f\5> \2\u012e\u012b\3\2\2\2\u012f\u0132\3\2\2\2\u0130\u012e\3\2\2"+
		"\2\u0130\u0131\3\2\2\2\u0131A\3\2\2\2\u0132\u0130\3\2\2\2\u0133\u0134"+
		"\b\"\1\2\u0134\u0135\5@!\2\u0135\u013b\3\2\2\2\u0136\u0137\6\"\4\3\u0137"+
		"\u0138\7\62\2\2\u0138\u013a\5@!\2\u0139\u0136\3\2\2\2\u013a\u013d\3\2"+
		"\2\2\u013b\u0139\3\2\2\2\u013b\u013c\3\2\2\2\u013cC\3\2\2\2\u013d\u013b"+
		"\3\2\2\2\u013e\u013f\b#\1\2\u013f\u0140\5B\"\2\u0140\u0146\3\2\2\2\u0141"+
		"\u0142\6#\5\3\u0142\u0143\7\63\2\2\u0143\u0145\5B\"\2\u0144\u0141\3\2"+
		"\2\2\u0145\u0148\3\2\2\2\u0146\u0144\3\2\2\2\u0146\u0147\3\2\2\2\u0147"+
		"E\3\2\2\2\u0148\u0146\3\2\2\2\u0149\u014a\b$\1\2\u014a\u014b\5D#\2\u014b"+
		"\u0151\3\2\2\2\u014c\u014d\6$\6\3\u014d\u014e\7\64\2\2\u014e\u0150\5D"+
		"#\2\u014f\u014c\3\2\2\2\u0150\u0153\3\2\2\2\u0151\u014f\3\2\2\2\u0151"+
		"\u0152\3\2\2\2\u0152G\3\2\2\2\u0153\u0151\3\2\2\2\u0154\u0155\b%\1\2\u0155"+
		"\u0156\5F$\2\u0156\u015c\3\2\2\2\u0157\u0158\6%\7\3\u0158\u0159\7\'\2"+
		"\2\u0159\u015b\5F$\2\u015a\u0157\3\2\2\2\u015b\u015e\3\2\2\2\u015c\u015a"+
		"\3\2\2\2\u015c\u015d\3\2\2\2\u015dI\3\2\2\2\u015e\u015c\3\2\2\2\u015f"+
		"\u0160\b&\1\2\u0160\u0161\5H%\2\u0161\u0167\3\2\2\2\u0162\u0163\6&\b\3"+
		"\u0163\u0164\7\65\2\2\u0164\u0166\5H%\2\u0165\u0162\3\2\2\2\u0166\u0169"+
		"\3\2\2\2\u0167\u0165\3\2\2\2\u0167\u0168\3\2\2\2\u0168K\3\2\2\2\u0169"+
		"\u0167\3\2\2\2\u016a\u016b\b\'\1\2\u016b\u016c\5J&\2\u016c\u0172\3\2\2"+
		"\2\u016d\u016e\6\'\t\3\u016e\u016f\7(\2\2\u016f\u0171\5J&\2\u0170\u016d"+
		"\3\2\2\2\u0171\u0174\3\2\2\2\u0172\u0170\3\2\2\2\u0172\u0173\3\2\2\2\u0173"+
		"M\3\2\2\2\u0174\u0172\3\2\2\2\u0175\u0176\b(\1\2\u0176\u0177\5L\'\2\u0177"+
		"\u017d\3\2\2\2\u0178\u0179\6(\n\3\u0179\u017a\7)\2\2\u017a\u017c\5L\'"+
		"\2\u017b\u0178\3\2\2\2\u017c\u017f\3\2\2\2\u017d\u017b\3\2\2\2\u017d\u017e"+
		"\3\2\2\2\u017eO\3\2\2\2\u017f\u017d\3\2\2\2\u0180\u0181\5N(\2\u0181Q\3"+
		"\2\2\2\u0182\u0183\5P)\2\u0183S\3\2\2\2\u0184\u0197\7%\2\2\u0185\u0197"+
		"\5V,\2\u0186\u0197\5`\61\2\u0187\u0197\5\20\t\2\u0188\u0189\7\13\2\2\u0189"+
		"\u018a\5P)\2\u018a\u018b\7\30\2\2\u018b\u0197\3\2\2\2\u018c\u018d\7*\2"+
		"\2\u018d\u018e\7\13\2\2\u018e\u018f\5P)\2\u018f\u0190\7\30\2\2\u0190\u0197"+
		"\3\2\2\2\u0191\u0197\5,\27\2\u0192\u0197\5\60\31\2\u0193\u0197\5.\30\2"+
		"\u0194\u0197\5<\37\2\u0195\u0197\5\64\33\2\u0196\u0184\3\2\2\2\u0196\u0185"+
		"\3\2\2\2\u0196\u0186\3\2\2\2\u0196\u0187\3\2\2\2\u0196\u0188\3\2\2\2\u0196"+
		"\u018c\3\2\2\2\u0196\u0191\3\2\2\2\u0196\u0192\3\2\2\2\u0196\u0193\3\2"+
		"\2\2\u0196\u0194\3\2\2\2\u0196\u0195\3\2\2\2\u0197U\3\2\2\2\u0198\u0199"+
		"\7\67\2\2\u0199W\3\2\2\2\u019a\u019b\7\36\2\2\u019b\u019c\7\13\2\2\u019c"+
		"\u019d\5P)\2\u019d\u019e\7\30\2\2\u019e\u019f\7,\2\2\u019fY\3\2\2\2\u01a0"+
		"\u01a1\7\36\2\2\u01a1\u01a2\7\13\2\2\u01a2\u01a3\5P)\2\u01a3\u01a4\7\6"+
		"\2\2\u01a4\u01a5\5P)\2\u01a5\u01a6\7\30\2\2\u01a6\u01a7\7,\2\2\u01a7["+
		"\3\2\2\2\u01a8\u01a9\7\25\2\2\u01a9\u01aa\7\13\2\2\u01aa\u01ab\5P)\2\u01ab"+
		"\u01ac\7\30\2\2\u01ac\u01ad\7,\2\2\u01ad]\3\2\2\2\u01ae\u01af\7\4\2\2"+
		"\u01af\u01b0\7,\2\2\u01b0_\3\2\2\2\u01b1\u01b2\7/\2\2\u01b2a\3\2\2\2\25"+
		"{}\u0088\u009a\u00a2\u00b8\u0100\u0107\u010b\u0125\u0130\u013b\u0146\u0151"+
		"\u015c\u0167\u0172\u017d\u0196";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}