// Generated from E:\WorkingArea\ethereum\ethereumj\src\main\java\org\ethereum\serpent\Serpent.g4 by ANTLR 4.1
package org.ethereum.serpent;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SerpentParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SerpentVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SerpentParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(@NotNull SerpentParser.ExpressionContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#assign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssign(@NotNull SerpentParser.AssignContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#if_elif_else_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_elif_else_stmt(@NotNull SerpentParser.If_elif_else_stmtContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#get_var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGet_var(@NotNull SerpentParser.Get_varContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(@NotNull SerpentParser.BlockContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#tx_origin}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTx_origin(@NotNull SerpentParser.Tx_originContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#asm_symbol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsm_symbol(@NotNull SerpentParser.Asm_symbolContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#contract_storage_assign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContract_storage_assign(@NotNull SerpentParser.Contract_storage_assignContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#tx_gas}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTx_gas(@NotNull SerpentParser.Tx_gasContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#block_number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock_number(@NotNull SerpentParser.Block_numberContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#in_or_exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_or_exp(@NotNull SerpentParser.In_or_expContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#add_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_expr(@NotNull SerpentParser.Add_exprContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#msg_datasize}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsg_datasize(@NotNull SerpentParser.Msg_datasizeContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#msg_sender}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsg_sender(@NotNull SerpentParser.Msg_senderContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#block_difficulty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock_difficulty(@NotNull SerpentParser.Block_difficultyContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#array_assign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_assign(@NotNull SerpentParser.Array_assignContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#array_retreive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_retreive(@NotNull SerpentParser.Array_retreiveContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#parse_init_code_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse_init_code_block(@NotNull SerpentParser.Parse_init_code_blockContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#tx_gasprice}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTx_gasprice(@NotNull SerpentParser.Tx_gaspriceContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#contract_storage_load}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContract_storage_load(@NotNull SerpentParser.Contract_storage_loadContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#ex_or_exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEx_or_exp(@NotNull SerpentParser.Ex_or_expContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#block_gaslimit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock_gaslimit(@NotNull SerpentParser.Block_gaslimitContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#rel_exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRel_exp(@NotNull SerpentParser.Rel_expContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#msg_func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsg_func(@NotNull SerpentParser.Msg_funcContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(@NotNull SerpentParser.ParseContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#suicide_func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuicide_func(@NotNull SerpentParser.Suicide_funcContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#hex_num}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHex_num(@NotNull SerpentParser.Hex_numContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#contract_balance}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContract_balance(@NotNull SerpentParser.Contract_balanceContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(@NotNull SerpentParser.ConditionContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#eq_exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEq_exp(@NotNull SerpentParser.Eq_expContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#stop_func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStop_func(@NotNull SerpentParser.Stop_funcContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#log_and_exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLog_and_exp(@NotNull SerpentParser.Log_and_expContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#block_timestamp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock_timestamp(@NotNull SerpentParser.Block_timestampContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#send_func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSend_func(@NotNull SerpentParser.Send_funcContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#arr_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArr_def(@NotNull SerpentParser.Arr_defContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#while_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile_stmt(@NotNull SerpentParser.While_stmtContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#special_func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecial_func(@NotNull SerpentParser.Special_funcContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#block_coinbase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock_coinbase(@NotNull SerpentParser.Block_coinbaseContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#log_or_exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLog_or_exp(@NotNull SerpentParser.Log_or_expContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#and_exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd_exp(@NotNull SerpentParser.And_expContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#block_prevhash}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock_prevhash(@NotNull SerpentParser.Block_prevhashContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#mul_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMul_expr(@NotNull SerpentParser.Mul_exprContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#int_val}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInt_val(@NotNull SerpentParser.Int_valContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#msg_data}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsg_data(@NotNull SerpentParser.Msg_dataContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#ret_func_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRet_func_2(@NotNull SerpentParser.Ret_func_2Context ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#msg_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsg_value(@NotNull SerpentParser.Msg_valueContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#asm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsm(@NotNull SerpentParser.AsmContext ctx);

	/**
	 * Visit a parse tree produced by {@link SerpentParser#ret_func_1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRet_func_1(@NotNull SerpentParser.Ret_func_1Context ctx);
}