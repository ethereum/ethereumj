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
		T__32=1, T__31=2, T__30=3, T__29=4, T__28=5, T__27=6, T__26=7, T__25=8, 
		T__24=9, T__23=10, T__22=11, T__21=12, T__20=13, T__19=14, T__18=15, T__17=16, 
		T__16=17, T__15=18, T__14=19, T__13=20, T__12=21, T__11=22, T__10=23, 
		T__9=24, T__8=25, T__7=26, T__6=27, T__5=28, T__4=29, T__3=30, T__2=31, 
		T__1=32, T__0=33, INT=34, ASM_SYMBOLS=35, OP_EX_OR=36, OP_LOG_AND=37, 
		OP_LOG_OR=38, OP_NOT=39, EQ_OP=40, NL=41, WS=42, LINE_COMMENT=43, VAR=44, 
		OP_ADD=45, OP_MUL=46, OP_REL=47, OP_EQ=48, OP_AND=49, OP_IN_OR=50, HEX_DIGIT=51, 
		HEX_NUMBER=52, INDENT=53, DEDENT=54;
	public static final String[] tokenNames = {
		"<INVALID>", "']'", "'stop'", "'block.coinbase'", "','", "'msg'", "'tx.gas'", 
		"'while'", "'['", "'('", "':'", "'if'", "'block.timestamp'", "'[asm'", 
		"'msg.value'", "'init'", "'block.prevhash'", "'contract.storage'", "'suicide'", 
		"'block.difficulty'", "'msg.datasize'", "')'", "'tx.gasprice'", "'else:'", 
		"'tx.origin'", "'msg.data'", "'elif'", "'return'", "'msg.sender'", "'contract.balance'", 
		"'asm]'", "'block.gaslimit'", "'block.number'", "'code'", "INT", "ASM_SYMBOLS", 
		"'xor'", "OP_LOG_AND", "OP_LOG_OR", "OP_NOT", "'='", "NL", "WS", "LINE_COMMENT", 
		"VAR", "OP_ADD", "OP_MUL", "OP_REL", "OP_EQ", "'&'", "'|'", "HEX_DIGIT", 
		"HEX_NUMBER", "INDENT", "DEDENT"
	};
	public static final int
		RULE_parse = 0, RULE_parse_init_code_block = 1, RULE_block = 2, RULE_asm = 3, 
		RULE_asm_symbol = 4, RULE_if_elif_else_stmt = 5, RULE_while_stmt = 6, 
		RULE_special_func = 7, RULE_msg_datasize = 8, RULE_msg_sender = 9, RULE_msg_value = 10, 
		RULE_tx_gasprice = 11, RULE_tx_origin = 12, RULE_tx_gas = 13, RULE_contract_balance = 14, 
		RULE_block_prevhash = 15, RULE_block_coinbase = 16, RULE_block_timestamp = 17, 
		RULE_block_number = 18, RULE_block_difficulty = 19, RULE_block_gaslimit = 20, 
		RULE_msg_func = 21, RULE_msg_data = 22, RULE_assign = 23, RULE_contract_storage_assign = 24, 
		RULE_contract_storage_load = 25, RULE_mul_expr = 26, RULE_add_expr = 27, 
		RULE_rel_exp = 28, RULE_eq_exp = 29, RULE_and_exp = 30, RULE_ex_or_exp = 31, 
		RULE_in_or_exp = 32, RULE_log_and_exp = 33, RULE_log_or_exp = 34, RULE_expression = 35, 
		RULE_condition = 36, RULE_int_val = 37, RULE_hex_num = 38, RULE_ret_func_1 = 39, 
		RULE_ret_func_2 = 40, RULE_suicide_func = 41, RULE_stop_func = 42, RULE_get_var = 43;
	public static final String[] ruleNames = {
		"parse", "parse_init_code_block", "block", "asm", "asm_symbol", "if_elif_else_stmt", 
		"while_stmt", "special_func", "msg_datasize", "msg_sender", "msg_value", 
		"tx_gasprice", "tx_origin", "tx_gas", "contract_balance", "block_prevhash", 
		"block_coinbase", "block_timestamp", "block_number", "block_difficulty", 
		"block_gaslimit", "msg_func", "msg_data", "assign", "contract_storage_assign", 
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
			setState(88); block();
			setState(89); match(EOF);
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
			setState(91); match(15);
			setState(92); match(10);
			setState(93); match(INDENT);
			setState(94); block();
			setState(95); match(DEDENT);
			setState(96); match(33);
			setState(97); match(10);
			setState(98); match(INDENT);
			setState(99); block();
			setState(100); match(DEDENT);
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
		public List<Stop_funcContext> stop_func() {
			return getRuleContexts(Stop_funcContext.class);
		}
		public Contract_storage_assignContext contract_storage_assign(int i) {
			return getRuleContext(Contract_storage_assignContext.class,i);
		}
		public List<While_stmtContext> while_stmt() {
			return getRuleContexts(While_stmtContext.class);
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
		public List<Contract_storage_assignContext> contract_storage_assign() {
			return getRuleContexts(Contract_storage_assignContext.class);
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
			setState(114);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << 2) | (1L << 3) | (1L << 6) | (1L << 7) | (1L << 11) | (1L << 12) | (1L << 13) | (1L << 14) | (1L << 16) | (1L << 17) | (1L << 18) | (1L << 19) | (1L << 20) | (1L << 22) | (1L << 24) | (1L << 27) | (1L << 28) | (1L << 29) | (1L << 31) | (1L << 32) | (1L << VAR))) != 0)) {
				{
				setState(112);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(102); asm();
					}
					break;

				case 2:
					{
					setState(103); assign();
					}
					break;

				case 3:
					{
					setState(104); contract_storage_assign();
					}
					break;

				case 4:
					{
					setState(105); special_func();
					}
					break;

				case 5:
					{
					setState(106); if_elif_else_stmt();
					}
					break;

				case 6:
					{
					setState(107); while_stmt();
					}
					break;

				case 7:
					{
					setState(108); ret_func_1();
					}
					break;

				case 8:
					{
					setState(109); ret_func_2();
					}
					break;

				case 9:
					{
					setState(110); suicide_func();
					}
					break;

				case 10:
					{
					setState(111); stop_func();
					}
					break;
				}
				}
				setState(116);
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
			setState(117); match(13);
			setState(118); asm_symbol();
			setState(119); match(30);
			setState(120); match(NL);
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
		public TerminalNode ASM_SYMBOLS(int i) {
			return getToken(SerpentParser.ASM_SYMBOLS, i);
		}
		public List<TerminalNode> INT() { return getTokens(SerpentParser.INT); }
		public List<TerminalNode> ASM_SYMBOLS() { return getTokens(SerpentParser.ASM_SYMBOLS); }
		public TerminalNode INT(int i) {
			return getToken(SerpentParser.INT, i);
		}
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
			setState(125);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==INT || _la==ASM_SYMBOLS) {
				{
				{
				setState(122);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==ASM_SYMBOLS) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				}
				setState(127);
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
			setState(128); match(11);
			setState(129); condition();
			setState(130); match(10);
			setState(131); match(INDENT);
			setState(132); block();
			setState(133); match(DEDENT);
			setState(143);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==26) {
				{
				{
				setState(134); match(26);
				setState(135); condition();
				setState(136); match(10);
				setState(137); match(INDENT);
				setState(138); block();
				setState(139); match(DEDENT);
				}
				}
				setState(145);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(151);
			_la = _input.LA(1);
			if (_la==23) {
				{
				setState(146); match(23);
				setState(147); match(INDENT);
				setState(148); block();
				setState(149); match(DEDENT);
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
			setState(153); match(7);
			setState(154); condition();
			setState(155); match(10);
			setState(156); match(INDENT);
			setState(157); block();
			setState(158); match(DEDENT);
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
			setState(173);
			switch (_input.LA(1)) {
			case 20:
				enterOuterAlt(_localctx, 1);
				{
				setState(160); msg_datasize();
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 2);
				{
				setState(161); msg_sender();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 3);
				{
				setState(162); msg_value();
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 4);
				{
				setState(163); tx_gasprice();
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 5);
				{
				setState(164); tx_origin();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(165); tx_gas();
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 7);
				{
				setState(166); contract_balance();
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 8);
				{
				setState(167); block_prevhash();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 9);
				{
				setState(168); block_coinbase();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 10);
				{
				setState(169); block_timestamp();
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 11);
				{
				setState(170); block_number();
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 12);
				{
				setState(171); block_difficulty();
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 13);
				{
				setState(172); block_gaslimit();
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
			setState(175); match(20);
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
			setState(177); match(28);
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
			setState(179); match(14);
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
			setState(181); match(22);
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
			setState(183); match(24);
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
			setState(185); match(6);
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
			setState(187); match(29);
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
			setState(189); match(16);
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
			setState(191); match(3);
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
			setState(193); match(12);
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
			setState(195); match(32);
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
			setState(197); match(19);
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
			setState(199); match(31);
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
			setState(201); match(5);
			setState(202); match(9);
			setState(203); int_val();
			setState(204); match(4);
			setState(205); int_val();
			setState(206); match(4);
			setState(207); int_val();
			setState(208); match(4);
			setState(209); int_val();
			setState(210); match(4);
			setState(211); int_val();
			setState(212); match(21);
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
		enterRule(_localctx, 44, RULE_msg_data);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214); match(25);
			setState(215); match(8);
			setState(216); expression();
			setState(217); match(1);
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
		enterRule(_localctx, 46, RULE_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219); match(VAR);
			setState(220); match(EQ_OP);
			setState(221); expression();
			setState(222); match(NL);
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
		enterRule(_localctx, 48, RULE_contract_storage_assign);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(224); match(17);
			setState(225); match(8);
			setState(226); expression();
			setState(227); match(1);
			setState(228); match(EQ_OP);
			setState(229); expression();
			setState(230); match(NL);
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
		enterRule(_localctx, 50, RULE_contract_storage_load);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232); match(17);
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
		int _startState = 52;
		enterRecursionRule(_localctx, RULE_mul_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(238); int_val();
			}
			_ctx.stop = _input.LT(-1);
			setState(245);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Mul_exprContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_mul_expr);
					setState(240);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(241); match(OP_MUL);
					setState(242); int_val();
					}
					} 
				}
				setState(247);
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
		int _startState = 54;
		enterRecursionRule(_localctx, RULE_add_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(249); mul_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(256);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Add_exprContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_add_expr);
					setState(251);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(252); match(OP_ADD);
					setState(253); mul_expr(0);
					}
					} 
				}
				setState(258);
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
		int _startState = 56;
		enterRecursionRule(_localctx, RULE_rel_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(260); add_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(267);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Rel_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_rel_exp);
					setState(262);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(263); match(OP_REL);
					setState(264); add_expr(0);
					}
					} 
				}
				setState(269);
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
		int _startState = 58;
		enterRecursionRule(_localctx, RULE_eq_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(271); rel_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(278);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Eq_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_eq_exp);
					setState(273);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(274); match(OP_EQ);
					setState(275); rel_exp(0);
					}
					} 
				}
				setState(280);
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
		int _startState = 60;
		enterRecursionRule(_localctx, RULE_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(282); eq_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(289);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new And_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_and_exp);
					setState(284);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(285); match(OP_AND);
					setState(286); eq_exp(0);
					}
					} 
				}
				setState(291);
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
		int _startState = 62;
		enterRecursionRule(_localctx, RULE_ex_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(293); and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(300);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Ex_or_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_ex_or_exp);
					setState(295);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(296); match(OP_EX_OR);
					setState(297); and_exp(0);
					}
					} 
				}
				setState(302);
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
		int _startState = 64;
		enterRecursionRule(_localctx, RULE_in_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(304); ex_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(311);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new In_or_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_in_or_exp);
					setState(306);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(307); match(OP_IN_OR);
					setState(308); ex_or_exp(0);
					}
					} 
				}
				setState(313);
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
		int _startState = 66;
		enterRecursionRule(_localctx, RULE_log_and_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(315); in_or_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(322);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Log_and_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_log_and_exp);
					setState(317);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(318); match(OP_LOG_AND);
					setState(319); in_or_exp(0);
					}
					} 
				}
				setState(324);
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
		int _startState = 68;
		enterRecursionRule(_localctx, RULE_log_or_exp);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(326); log_and_exp(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(333);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Log_or_expContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_log_or_exp);
					setState(328);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(329); match(OP_LOG_OR);
					setState(330); log_and_exp(0);
					}
					} 
				}
				setState(335);
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
		enterRule(_localctx, 70, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(336); log_or_exp(0);
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
		enterRule(_localctx, 72, RULE_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(338); expression();
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
		enterRule(_localctx, 74, RULE_int_val);
		try {
			setState(356);
			switch (_input.LA(1)) {
			case INT:
				enterOuterAlt(_localctx, 1);
				{
				setState(340); match(INT);
				}
				break;
			case HEX_NUMBER:
				enterOuterAlt(_localctx, 2);
				{
				setState(341); hex_num();
				}
				break;
			case VAR:
				enterOuterAlt(_localctx, 3);
				{
				setState(342); get_var();
				}
				break;
			case 3:
			case 6:
			case 12:
			case 14:
			case 16:
			case 19:
			case 20:
			case 22:
			case 24:
			case 28:
			case 29:
			case 31:
			case 32:
				enterOuterAlt(_localctx, 4);
				{
				setState(343); special_func();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 5);
				{
				setState(344); match(9);
				setState(345); expression();
				setState(346); match(21);
				}
				break;
			case OP_NOT:
				enterOuterAlt(_localctx, 6);
				{
				setState(348); match(OP_NOT);
				setState(349); match(9);
				setState(350); expression();
				setState(351); match(21);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 7);
				{
				setState(353); msg_func();
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 8);
				{
				setState(354); msg_data();
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 9);
				{
				setState(355); contract_storage_load();
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
		enterRule(_localctx, 76, RULE_hex_num);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(358); match(HEX_NUMBER);
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
		enterRule(_localctx, 78, RULE_ret_func_1);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(360); match(27);
			setState(361); match(9);
			setState(362); expression();
			setState(363); match(21);
			setState(364); match(NL);
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
		enterRule(_localctx, 80, RULE_ret_func_2);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(366); match(27);
			setState(367); match(9);
			setState(368); expression();
			setState(369); match(4);
			setState(370); expression();
			setState(371); match(21);
			setState(372); match(NL);
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
		enterRule(_localctx, 82, RULE_suicide_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(374); match(18);
			setState(375); match(9);
			setState(376); expression();
			setState(377); match(21);
			setState(378); match(NL);
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
		enterRule(_localctx, 84, RULE_stop_func);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(380); match(2);
			setState(381); match(NL);
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
		enterRule(_localctx, 86, RULE_get_var);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(383); match(VAR);
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
		case 26: return mul_expr_sempred((Mul_exprContext)_localctx, predIndex);

		case 27: return add_expr_sempred((Add_exprContext)_localctx, predIndex);

		case 28: return rel_exp_sempred((Rel_expContext)_localctx, predIndex);

		case 29: return eq_exp_sempred((Eq_expContext)_localctx, predIndex);

		case 30: return and_exp_sempred((And_expContext)_localctx, predIndex);

		case 31: return ex_or_exp_sempred((Ex_or_expContext)_localctx, predIndex);

		case 32: return in_or_exp_sempred((In_or_expContext)_localctx, predIndex);

		case 33: return log_and_exp_sempred((Log_and_expContext)_localctx, predIndex);

		case 34: return log_or_exp_sempred((Log_or_expContext)_localctx, predIndex);
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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\38\u0184\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\7\4s\n\4\f\4\16\4v\13\4\3\5\3\5\3"+
		"\5\3\5\3\5\3\6\7\6~\n\6\f\6\16\6\u0081\13\6\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\7\7\u0090\n\7\f\7\16\7\u0093\13\7\3\7\3\7\3"+
		"\7\3\7\3\7\5\7\u009a\n\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u00b0\n\t\3\n\3\n\3\13\3\13\3\f"+
		"\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3"+
		"\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3"+
		"\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3"+
		"\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3"+
		"\33\3\34\3\34\3\34\3\34\3\34\3\34\7\34\u00f6\n\34\f\34\16\34\u00f9\13"+
		"\34\3\35\3\35\3\35\3\35\3\35\3\35\7\35\u0101\n\35\f\35\16\35\u0104\13"+
		"\35\3\36\3\36\3\36\3\36\3\36\3\36\7\36\u010c\n\36\f\36\16\36\u010f\13"+
		"\36\3\37\3\37\3\37\3\37\3\37\3\37\7\37\u0117\n\37\f\37\16\37\u011a\13"+
		"\37\3 \3 \3 \3 \3 \3 \7 \u0122\n \f \16 \u0125\13 \3!\3!\3!\3!\3!\3!\7"+
		"!\u012d\n!\f!\16!\u0130\13!\3\"\3\"\3\"\3\"\3\"\3\"\7\"\u0138\n\"\f\""+
		"\16\"\u013b\13\"\3#\3#\3#\3#\3#\3#\7#\u0143\n#\f#\16#\u0146\13#\3$\3$"+
		"\3$\3$\3$\3$\7$\u014e\n$\f$\16$\u0151\13$\3%\3%\3&\3&\3\'\3\'\3\'\3\'"+
		"\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\5\'\u0167\n\'\3(\3(\3"+
		")\3)\3)\3)\3)\3)\3*\3*\3*\3*\3*\3*\3*\3*\3+\3+\3+\3+\3+\3+\3,\3,\3,\3"+
		"-\3-\3-\2.\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\66"+
		"8:<>@BDFHJLNPRTVX\2\3\3\2$%\u0181\2Z\3\2\2\2\4]\3\2\2\2\6t\3\2\2\2\bw"+
		"\3\2\2\2\n\177\3\2\2\2\f\u0082\3\2\2\2\16\u009b\3\2\2\2\20\u00af\3\2\2"+
		"\2\22\u00b1\3\2\2\2\24\u00b3\3\2\2\2\26\u00b5\3\2\2\2\30\u00b7\3\2\2\2"+
		"\32\u00b9\3\2\2\2\34\u00bb\3\2\2\2\36\u00bd\3\2\2\2 \u00bf\3\2\2\2\"\u00c1"+
		"\3\2\2\2$\u00c3\3\2\2\2&\u00c5\3\2\2\2(\u00c7\3\2\2\2*\u00c9\3\2\2\2,"+
		"\u00cb\3\2\2\2.\u00d8\3\2\2\2\60\u00dd\3\2\2\2\62\u00e2\3\2\2\2\64\u00ea"+
		"\3\2\2\2\66\u00ef\3\2\2\28\u00fa\3\2\2\2:\u0105\3\2\2\2<\u0110\3\2\2\2"+
		">\u011b\3\2\2\2@\u0126\3\2\2\2B\u0131\3\2\2\2D\u013c\3\2\2\2F\u0147\3"+
		"\2\2\2H\u0152\3\2\2\2J\u0154\3\2\2\2L\u0166\3\2\2\2N\u0168\3\2\2\2P\u016a"+
		"\3\2\2\2R\u0170\3\2\2\2T\u0178\3\2\2\2V\u017e\3\2\2\2X\u0181\3\2\2\2Z"+
		"[\5\6\4\2[\\\7\2\2\3\\\3\3\2\2\2]^\7\21\2\2^_\7\f\2\2_`\7\67\2\2`a\5\6"+
		"\4\2ab\78\2\2bc\7#\2\2cd\7\f\2\2de\7\67\2\2ef\5\6\4\2fg\78\2\2g\5\3\2"+
		"\2\2hs\5\b\5\2is\5\60\31\2js\5\62\32\2ks\5\20\t\2ls\5\f\7\2ms\5\16\b\2"+
		"ns\5P)\2os\5R*\2ps\5T+\2qs\5V,\2rh\3\2\2\2ri\3\2\2\2rj\3\2\2\2rk\3\2\2"+
		"\2rl\3\2\2\2rm\3\2\2\2rn\3\2\2\2ro\3\2\2\2rp\3\2\2\2rq\3\2\2\2sv\3\2\2"+
		"\2tr\3\2\2\2tu\3\2\2\2u\7\3\2\2\2vt\3\2\2\2wx\7\17\2\2xy\5\n\6\2yz\7 "+
		"\2\2z{\7+\2\2{\t\3\2\2\2|~\t\2\2\2}|\3\2\2\2~\u0081\3\2\2\2\177}\3\2\2"+
		"\2\177\u0080\3\2\2\2\u0080\13\3\2\2\2\u0081\177\3\2\2\2\u0082\u0083\7"+
		"\r\2\2\u0083\u0084\5J&\2\u0084\u0085\7\f\2\2\u0085\u0086\7\67\2\2\u0086"+
		"\u0087\5\6\4\2\u0087\u0091\78\2\2\u0088\u0089\7\34\2\2\u0089\u008a\5J"+
		"&\2\u008a\u008b\7\f\2\2\u008b\u008c\7\67\2\2\u008c\u008d\5\6\4\2\u008d"+
		"\u008e\78\2\2\u008e\u0090\3\2\2\2\u008f\u0088\3\2\2\2\u0090\u0093\3\2"+
		"\2\2\u0091\u008f\3\2\2\2\u0091\u0092\3\2\2\2\u0092\u0099\3\2\2\2\u0093"+
		"\u0091\3\2\2\2\u0094\u0095\7\31\2\2\u0095\u0096\7\67\2\2\u0096\u0097\5"+
		"\6\4\2\u0097\u0098\78\2\2\u0098\u009a\3\2\2\2\u0099\u0094\3\2\2\2\u0099"+
		"\u009a\3\2\2\2\u009a\r\3\2\2\2\u009b\u009c\7\t\2\2\u009c\u009d\5J&\2\u009d"+
		"\u009e\7\f\2\2\u009e\u009f\7\67\2\2\u009f\u00a0\5\6\4\2\u00a0\u00a1\7"+
		"8\2\2\u00a1\17\3\2\2\2\u00a2\u00b0\5\22\n\2\u00a3\u00b0\5\24\13\2\u00a4"+
		"\u00b0\5\26\f\2\u00a5\u00b0\5\30\r\2\u00a6\u00b0\5\32\16\2\u00a7\u00b0"+
		"\5\34\17\2\u00a8\u00b0\5\36\20\2\u00a9\u00b0\5 \21\2\u00aa\u00b0\5\"\22"+
		"\2\u00ab\u00b0\5$\23\2\u00ac\u00b0\5&\24\2\u00ad\u00b0\5(\25\2\u00ae\u00b0"+
		"\5*\26\2\u00af\u00a2\3\2\2\2\u00af\u00a3\3\2\2\2\u00af\u00a4\3\2\2\2\u00af"+
		"\u00a5\3\2\2\2\u00af\u00a6\3\2\2\2\u00af\u00a7\3\2\2\2\u00af\u00a8\3\2"+
		"\2\2\u00af\u00a9\3\2\2\2\u00af\u00aa\3\2\2\2\u00af\u00ab\3\2\2\2\u00af"+
		"\u00ac\3\2\2\2\u00af\u00ad\3\2\2\2\u00af\u00ae\3\2\2\2\u00b0\21\3\2\2"+
		"\2\u00b1\u00b2\7\26\2\2\u00b2\23\3\2\2\2\u00b3\u00b4\7\36\2\2\u00b4\25"+
		"\3\2\2\2\u00b5\u00b6\7\20\2\2\u00b6\27\3\2\2\2\u00b7\u00b8\7\30\2\2\u00b8"+
		"\31\3\2\2\2\u00b9\u00ba\7\32\2\2\u00ba\33\3\2\2\2\u00bb\u00bc\7\b\2\2"+
		"\u00bc\35\3\2\2\2\u00bd\u00be\7\37\2\2\u00be\37\3\2\2\2\u00bf\u00c0\7"+
		"\22\2\2\u00c0!\3\2\2\2\u00c1\u00c2\7\5\2\2\u00c2#\3\2\2\2\u00c3\u00c4"+
		"\7\16\2\2\u00c4%\3\2\2\2\u00c5\u00c6\7\"\2\2\u00c6\'\3\2\2\2\u00c7\u00c8"+
		"\7\25\2\2\u00c8)\3\2\2\2\u00c9\u00ca\7!\2\2\u00ca+\3\2\2\2\u00cb\u00cc"+
		"\7\7\2\2\u00cc\u00cd\7\13\2\2\u00cd\u00ce\5L\'\2\u00ce\u00cf\7\6\2\2\u00cf"+
		"\u00d0\5L\'\2\u00d0\u00d1\7\6\2\2\u00d1\u00d2\5L\'\2\u00d2\u00d3\7\6\2"+
		"\2\u00d3\u00d4\5L\'\2\u00d4\u00d5\7\6\2\2\u00d5\u00d6\5L\'\2\u00d6\u00d7"+
		"\7\27\2\2\u00d7-\3\2\2\2\u00d8\u00d9\7\33\2\2\u00d9\u00da\7\n\2\2\u00da"+
		"\u00db\5H%\2\u00db\u00dc\7\3\2\2\u00dc/\3\2\2\2\u00dd\u00de\7.\2\2\u00de"+
		"\u00df\7*\2\2\u00df\u00e0\5H%\2\u00e0\u00e1\7+\2\2\u00e1\61\3\2\2\2\u00e2"+
		"\u00e3\7\23\2\2\u00e3\u00e4\7\n\2\2\u00e4\u00e5\5H%\2\u00e5\u00e6\7\3"+
		"\2\2\u00e6\u00e7\7*\2\2\u00e7\u00e8\5H%\2\u00e8\u00e9\7+\2\2\u00e9\63"+
		"\3\2\2\2\u00ea\u00eb\7\23\2\2\u00eb\u00ec\7\n\2\2\u00ec\u00ed\5H%\2\u00ed"+
		"\u00ee\7\3\2\2\u00ee\65\3\2\2\2\u00ef\u00f0\b\34\1\2\u00f0\u00f1\5L\'"+
		"\2\u00f1\u00f7\3\2\2\2\u00f2\u00f3\6\34\2\3\u00f3\u00f4\7\60\2\2\u00f4"+
		"\u00f6\5L\'\2\u00f5\u00f2\3\2\2\2\u00f6\u00f9\3\2\2\2\u00f7\u00f5\3\2"+
		"\2\2\u00f7\u00f8\3\2\2\2\u00f8\67\3\2\2\2\u00f9\u00f7\3\2\2\2\u00fa\u00fb"+
		"\b\35\1\2\u00fb\u00fc\5\66\34\2\u00fc\u0102\3\2\2\2\u00fd\u00fe\6\35\3"+
		"\3\u00fe\u00ff\7/\2\2\u00ff\u0101\5\66\34\2\u0100\u00fd\3\2\2\2\u0101"+
		"\u0104\3\2\2\2\u0102\u0100\3\2\2\2\u0102\u0103\3\2\2\2\u01039\3\2\2\2"+
		"\u0104\u0102\3\2\2\2\u0105\u0106\b\36\1\2\u0106\u0107\58\35\2\u0107\u010d"+
		"\3\2\2\2\u0108\u0109\6\36\4\3\u0109\u010a\7\61\2\2\u010a\u010c\58\35\2"+
		"\u010b\u0108\3\2\2\2\u010c\u010f\3\2\2\2\u010d\u010b\3\2\2\2\u010d\u010e"+
		"\3\2\2\2\u010e;\3\2\2\2\u010f\u010d\3\2\2\2\u0110\u0111\b\37\1\2\u0111"+
		"\u0112\5:\36\2\u0112\u0118\3\2\2\2\u0113\u0114\6\37\5\3\u0114\u0115\7"+
		"\62\2\2\u0115\u0117\5:\36\2\u0116\u0113\3\2\2\2\u0117\u011a\3\2\2\2\u0118"+
		"\u0116\3\2\2\2\u0118\u0119\3\2\2\2\u0119=\3\2\2\2\u011a\u0118\3\2\2\2"+
		"\u011b\u011c\b \1\2\u011c\u011d\5<\37\2\u011d\u0123\3\2\2\2\u011e\u011f"+
		"\6 \6\3\u011f\u0120\7\63\2\2\u0120\u0122\5<\37\2\u0121\u011e\3\2\2\2\u0122"+
		"\u0125\3\2\2\2\u0123\u0121\3\2\2\2\u0123\u0124\3\2\2\2\u0124?\3\2\2\2"+
		"\u0125\u0123\3\2\2\2\u0126\u0127\b!\1\2\u0127\u0128\5> \2\u0128\u012e"+
		"\3\2\2\2\u0129\u012a\6!\7\3\u012a\u012b\7&\2\2\u012b\u012d\5> \2\u012c"+
		"\u0129\3\2\2\2\u012d\u0130\3\2\2\2\u012e\u012c\3\2\2\2\u012e\u012f\3\2"+
		"\2\2\u012fA\3\2\2\2\u0130\u012e\3\2\2\2\u0131\u0132\b\"\1\2\u0132\u0133"+
		"\5@!\2\u0133\u0139\3\2\2\2\u0134\u0135\6\"\b\3\u0135\u0136\7\64\2\2\u0136"+
		"\u0138\5@!\2\u0137\u0134\3\2\2\2\u0138\u013b\3\2\2\2\u0139\u0137\3\2\2"+
		"\2\u0139\u013a\3\2\2\2\u013aC\3\2\2\2\u013b\u0139\3\2\2\2\u013c\u013d"+
		"\b#\1\2\u013d\u013e\5B\"\2\u013e\u0144\3\2\2\2\u013f\u0140\6#\t\3\u0140"+
		"\u0141\7\'\2\2\u0141\u0143\5B\"\2\u0142\u013f\3\2\2\2\u0143\u0146\3\2"+
		"\2\2\u0144\u0142\3\2\2\2\u0144\u0145\3\2\2\2\u0145E\3\2\2\2\u0146\u0144"+
		"\3\2\2\2\u0147\u0148\b$\1\2\u0148\u0149\5D#\2\u0149\u014f\3\2\2\2\u014a"+
		"\u014b\6$\n\3\u014b\u014c\7(\2\2\u014c\u014e\5D#\2\u014d\u014a\3\2\2\2"+
		"\u014e\u0151\3\2\2\2\u014f\u014d\3\2\2\2\u014f\u0150\3\2\2\2\u0150G\3"+
		"\2\2\2\u0151\u014f\3\2\2\2\u0152\u0153\5F$\2\u0153I\3\2\2\2\u0154\u0155"+
		"\5H%\2\u0155K\3\2\2\2\u0156\u0167\7$\2\2\u0157\u0167\5N(\2\u0158\u0167"+
		"\5X-\2\u0159\u0167\5\20\t\2\u015a\u015b\7\13\2\2\u015b\u015c\5H%\2\u015c"+
		"\u015d\7\27\2\2\u015d\u0167\3\2\2\2\u015e\u015f\7)\2\2\u015f\u0160\7\13"+
		"\2\2\u0160\u0161\5H%\2\u0161\u0162\7\27\2\2\u0162\u0167\3\2\2\2\u0163"+
		"\u0167\5,\27\2\u0164\u0167\5.\30\2\u0165\u0167\5\64\33\2\u0166\u0156\3"+
		"\2\2\2\u0166\u0157\3\2\2\2\u0166\u0158\3\2\2\2\u0166\u0159\3\2\2\2\u0166"+
		"\u015a\3\2\2\2\u0166\u015e\3\2\2\2\u0166\u0163\3\2\2\2\u0166\u0164\3\2"+
		"\2\2\u0166\u0165\3\2\2\2\u0167M\3\2\2\2\u0168\u0169\7\66\2\2\u0169O\3"+
		"\2\2\2\u016a\u016b\7\35\2\2\u016b\u016c\7\13\2\2\u016c\u016d\5H%\2\u016d"+
		"\u016e\7\27\2\2\u016e\u016f\7+\2\2\u016fQ\3\2\2\2\u0170\u0171\7\35\2\2"+
		"\u0171\u0172\7\13\2\2\u0172\u0173\5H%\2\u0173\u0174\7\6\2\2\u0174\u0175"+
		"\5H%\2\u0175\u0176\7\27\2\2\u0176\u0177\7+\2\2\u0177S\3\2\2\2\u0178\u0179"+
		"\7\24\2\2\u0179\u017a\7\13\2\2\u017a\u017b\5H%\2\u017b\u017c\7\27\2\2"+
		"\u017c\u017d\7+\2\2\u017dU\3\2\2\2\u017e\u017f\7\4\2\2\u017f\u0180\7+"+
		"\2\2\u0180W\3\2\2\2\u0181\u0182\7.\2\2\u0182Y\3\2\2\2\22rt\177\u0091\u0099"+
		"\u00af\u00f7\u0102\u010d\u0118\u0123\u012e\u0139\u0144\u014f\u0166";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}