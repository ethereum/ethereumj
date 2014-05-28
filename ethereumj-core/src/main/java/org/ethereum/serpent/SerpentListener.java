// Generated from E:\WorkingArea\ethereum\ethereumj\src\main\java\org\ethereum\serpent\Serpent.g4 by ANTLR 4.1
package org.ethereum.serpent;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SerpentParser}.
 */
public interface SerpentListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SerpentParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(@NotNull SerpentParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(@NotNull SerpentParser.ExpressionContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#assign}.
	 * @param ctx the parse tree
	 */
	void enterAssign(@NotNull SerpentParser.AssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#assign}.
	 * @param ctx the parse tree
	 */
	void exitAssign(@NotNull SerpentParser.AssignContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#if_elif_else_stmt}.
	 * @param ctx the parse tree
	 */
	void enterIf_elif_else_stmt(@NotNull SerpentParser.If_elif_else_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#if_elif_else_stmt}.
	 * @param ctx the parse tree
	 */
	void exitIf_elif_else_stmt(@NotNull SerpentParser.If_elif_else_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#get_var}.
	 * @param ctx the parse tree
	 */
	void enterGet_var(@NotNull SerpentParser.Get_varContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#get_var}.
	 * @param ctx the parse tree
	 */
	void exitGet_var(@NotNull SerpentParser.Get_varContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#tx_origin}.
	 * @param ctx the parse tree
	 */
	void enterTx_origin(@NotNull SerpentParser.Tx_originContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#tx_origin}.
	 * @param ctx the parse tree
	 */
	void exitTx_origin(@NotNull SerpentParser.Tx_originContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(@NotNull SerpentParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(@NotNull SerpentParser.BlockContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#asm_symbol}.
	 * @param ctx the parse tree
	 */
	void enterAsm_symbol(@NotNull SerpentParser.Asm_symbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#asm_symbol}.
	 * @param ctx the parse tree
	 */
	void exitAsm_symbol(@NotNull SerpentParser.Asm_symbolContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#tx_gas}.
	 * @param ctx the parse tree
	 */
	void enterTx_gas(@NotNull SerpentParser.Tx_gasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#tx_gas}.
	 * @param ctx the parse tree
	 */
	void exitTx_gas(@NotNull SerpentParser.Tx_gasContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#block_number}.
	 * @param ctx the parse tree
	 */
	void enterBlock_number(@NotNull SerpentParser.Block_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#block_number}.
	 * @param ctx the parse tree
	 */
	void exitBlock_number(@NotNull SerpentParser.Block_numberContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#in_or_exp}.
	 * @param ctx the parse tree
	 */
	void enterIn_or_exp(@NotNull SerpentParser.In_or_expContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#in_or_exp}.
	 * @param ctx the parse tree
	 */
	void exitIn_or_exp(@NotNull SerpentParser.In_or_expContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#add_expr}.
	 * @param ctx the parse tree
	 */
	void enterAdd_expr(@NotNull SerpentParser.Add_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#add_expr}.
	 * @param ctx the parse tree
	 */
	void exitAdd_expr(@NotNull SerpentParser.Add_exprContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#msg_datasize}.
	 * @param ctx the parse tree
	 */
	void enterMsg_datasize(@NotNull SerpentParser.Msg_datasizeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#msg_datasize}.
	 * @param ctx the parse tree
	 */
	void exitMsg_datasize(@NotNull SerpentParser.Msg_datasizeContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#msg_sender}.
	 * @param ctx the parse tree
	 */
	void enterMsg_sender(@NotNull SerpentParser.Msg_senderContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#msg_sender}.
	 * @param ctx the parse tree
	 */
	void exitMsg_sender(@NotNull SerpentParser.Msg_senderContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#block_difficulty}.
	 * @param ctx the parse tree
	 */
	void enterBlock_difficulty(@NotNull SerpentParser.Block_difficultyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#block_difficulty}.
	 * @param ctx the parse tree
	 */
	void exitBlock_difficulty(@NotNull SerpentParser.Block_difficultyContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#tx_gasprice}.
	 * @param ctx the parse tree
	 */
	void enterTx_gasprice(@NotNull SerpentParser.Tx_gaspriceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#tx_gasprice}.
	 * @param ctx the parse tree
	 */
	void exitTx_gasprice(@NotNull SerpentParser.Tx_gaspriceContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#ex_or_exp}.
	 * @param ctx the parse tree
	 */
	void enterEx_or_exp(@NotNull SerpentParser.Ex_or_expContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#ex_or_exp}.
	 * @param ctx the parse tree
	 */
	void exitEx_or_exp(@NotNull SerpentParser.Ex_or_expContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#block_gaslimit}.
	 * @param ctx the parse tree
	 */
	void enterBlock_gaslimit(@NotNull SerpentParser.Block_gaslimitContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#block_gaslimit}.
	 * @param ctx the parse tree
	 */
	void exitBlock_gaslimit(@NotNull SerpentParser.Block_gaslimitContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#rel_exp}.
	 * @param ctx the parse tree
	 */
	void enterRel_exp(@NotNull SerpentParser.Rel_expContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#rel_exp}.
	 * @param ctx the parse tree
	 */
	void exitRel_exp(@NotNull SerpentParser.Rel_expContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#msg_func}.
	 * @param ctx the parse tree
	 */
	void enterMsg_func(@NotNull SerpentParser.Msg_funcContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#msg_func}.
	 * @param ctx the parse tree
	 */
	void exitMsg_func(@NotNull SerpentParser.Msg_funcContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(@NotNull SerpentParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(@NotNull SerpentParser.ParseContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#hex_num}.
	 * @param ctx the parse tree
	 */
	void enterHex_num(@NotNull SerpentParser.Hex_numContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#hex_num}.
	 * @param ctx the parse tree
	 */
	void exitHex_num(@NotNull SerpentParser.Hex_numContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#ret_func}.
	 * @param ctx the parse tree
	 */
	void enterRet_func(@NotNull SerpentParser.Ret_funcContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#ret_func}.
	 * @param ctx the parse tree
	 */
	void exitRet_func(@NotNull SerpentParser.Ret_funcContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#contract_balance}.
	 * @param ctx the parse tree
	 */
	void enterContract_balance(@NotNull SerpentParser.Contract_balanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#contract_balance}.
	 * @param ctx the parse tree
	 */
	void exitContract_balance(@NotNull SerpentParser.Contract_balanceContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(@NotNull SerpentParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(@NotNull SerpentParser.ConditionContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#eq_exp}.
	 * @param ctx the parse tree
	 */
	void enterEq_exp(@NotNull SerpentParser.Eq_expContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#eq_exp}.
	 * @param ctx the parse tree
	 */
	void exitEq_exp(@NotNull SerpentParser.Eq_expContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#log_and_exp}.
	 * @param ctx the parse tree
	 */
	void enterLog_and_exp(@NotNull SerpentParser.Log_and_expContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#log_and_exp}.
	 * @param ctx the parse tree
	 */
	void exitLog_and_exp(@NotNull SerpentParser.Log_and_expContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#block_timestamp}.
	 * @param ctx the parse tree
	 */
	void enterBlock_timestamp(@NotNull SerpentParser.Block_timestampContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#block_timestamp}.
	 * @param ctx the parse tree
	 */
	void exitBlock_timestamp(@NotNull SerpentParser.Block_timestampContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#while_stmt}.
	 * @param ctx the parse tree
	 */
	void enterWhile_stmt(@NotNull SerpentParser.While_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#while_stmt}.
	 * @param ctx the parse tree
	 */
	void exitWhile_stmt(@NotNull SerpentParser.While_stmtContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#special_func}.
	 * @param ctx the parse tree
	 */
	void enterSpecial_func(@NotNull SerpentParser.Special_funcContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#special_func}.
	 * @param ctx the parse tree
	 */
	void exitSpecial_func(@NotNull SerpentParser.Special_funcContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#block_coinbase}.
	 * @param ctx the parse tree
	 */
	void enterBlock_coinbase(@NotNull SerpentParser.Block_coinbaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#block_coinbase}.
	 * @param ctx the parse tree
	 */
	void exitBlock_coinbase(@NotNull SerpentParser.Block_coinbaseContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#log_or_exp}.
	 * @param ctx the parse tree
	 */
	void enterLog_or_exp(@NotNull SerpentParser.Log_or_expContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#log_or_exp}.
	 * @param ctx the parse tree
	 */
	void exitLog_or_exp(@NotNull SerpentParser.Log_or_expContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#and_exp}.
	 * @param ctx the parse tree
	 */
	void enterAnd_exp(@NotNull SerpentParser.And_expContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#and_exp}.
	 * @param ctx the parse tree
	 */
	void exitAnd_exp(@NotNull SerpentParser.And_expContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#block_prevhash}.
	 * @param ctx the parse tree
	 */
	void enterBlock_prevhash(@NotNull SerpentParser.Block_prevhashContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#block_prevhash}.
	 * @param ctx the parse tree
	 */
	void exitBlock_prevhash(@NotNull SerpentParser.Block_prevhashContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#mul_expr}.
	 * @param ctx the parse tree
	 */
	void enterMul_expr(@NotNull SerpentParser.Mul_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#mul_expr}.
	 * @param ctx the parse tree
	 */
	void exitMul_expr(@NotNull SerpentParser.Mul_exprContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#int_val}.
	 * @param ctx the parse tree
	 */
	void enterInt_val(@NotNull SerpentParser.Int_valContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#int_val}.
	 * @param ctx the parse tree
	 */
	void exitInt_val(@NotNull SerpentParser.Int_valContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#msg_value}.
	 * @param ctx the parse tree
	 */
	void enterMsg_value(@NotNull SerpentParser.Msg_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#msg_value}.
	 * @param ctx the parse tree
	 */
	void exitMsg_value(@NotNull SerpentParser.Msg_valueContext ctx);

	/**
	 * Enter a parse tree produced by {@link SerpentParser#asm}.
	 * @param ctx the parse tree
	 */
	void enterAsm(@NotNull SerpentParser.AsmContext ctx);
	/**
	 * Exit a parse tree produced by {@link SerpentParser#asm}.
	 * @param ctx the parse tree
	 */
	void exitAsm(@NotNull SerpentParser.AsmContext ctx);
}