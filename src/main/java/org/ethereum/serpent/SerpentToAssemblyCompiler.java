package org.ethereum.serpent;

import org.antlr.v4.runtime.misc.NotNull;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 05/05/14 13:41
 */
public class SerpentToAssemblyCompiler extends SerpentBaseVisitor<String> {

    int labelIndex = 0;
    private ArrayList<String> vars = new ArrayList<String>();



    @Override
    public String visitParse(@NotNull SerpentParser.ParseContext ctx) {

        StringBuffer result = new StringBuffer();

        result.append( super.visitParse(ctx) );

        // todo: calc the wrapping with memory usage data

        return result.toString();
    }


    @Override
    public String visitIf_elif_else_stmt(@NotNull SerpentParser.If_elif_else_stmtContext ctx) {

        //todo: when you find some error throw expectation exception
        StringBuffer retCode = new StringBuffer();

        int endOfStmtLabel = labelIndex++ ;

        // if body
        SerpentParser.BlockContext ifBlock = (SerpentParser.BlockContext) ctx.getChild(4);
        String blockCode = visitBlock(ifBlock);
        String ifCond = visitCondition(ctx.condition(0));

        int nextLabel = labelIndex++;


        // if_cond [NOT REF_X JUMPI] if_body [REF_Y JUMP LABEL_X] Y=total_end
        retCode.append(ifCond).
                append(" NOT REF_").append(nextLabel).append(" JUMPI ").
                append(blockCode).
                append("REF_").append(endOfStmtLabel).
                append(" JUMP LABEL_").append(nextLabel).append(" ");

        // traverse the children and find out all [elif] and [else] that exist
        int count = ctx.condition().size();

        // traverse all 'elif' statements
        int i = 1; // define i here for 'else' option
        for (; i < count; ++i){


            // if the condition much at the end jump out of the main if
            // append to general retCode
            // todo: [NOT REF_X JUMPI] elif_body [REF_Y JUMP LABEL_X] Y=total_end
            // todo extract the condition and the body
            nextLabel = labelIndex++;

            // elif condition
            String elifCond = visitCondition(ctx.condition(i));

            // elif body
            String elifBlockCode = visitBlock(ctx.block(i));

            retCode.append(elifCond).
                    append(" NOT REF_").append(nextLabel).append(" JUMPI ").
                    append(elifBlockCode).
                    append("REF_").append(endOfStmtLabel).append(" JUMP LABEL_").
                    append(nextLabel).append(" ");
        }


        // check if there is 'else'
        if (ctx.block(i) != null) {

            // body
            String elseBlockCode = visitBlock(ctx.block(i));

            // append to general retCode
            retCode.append(elseBlockCode);
        }

        // [LABEL_Y] Y = end of statement
        retCode.append("LABEL_").append(endOfStmtLabel).append(" ");

        return retCode.toString();
    }


    @Override
    public String visitWhile_stmt(@NotNull SerpentParser.While_stmtContext ctx) {

        int whileStartRef = labelIndex++;
        int whileEndRef = labelIndex++;

        // elif condition
        SerpentParser.ConditionContext whileCondition = (SerpentParser.ConditionContext)
                ctx.getChild(1);

        // elif body
        SerpentParser.BlockContext whileBlock = (SerpentParser.BlockContext)
                ctx.getChild(4);

        String retCode =
                String.format("LABEL_%d %s EQ NOT REF_%d JUMPI %s REF_%d JUMP LABEL_%d",
                            whileStartRef, visitCondition(whileCondition), whileEndRef, visitBlock(whileBlock), whileStartRef, whileEndRef);

        return retCode;
    }

    @Override
    public String visitBlock(@NotNull SerpentParser.BlockContext ctx) {

        String blockStmt = super.visitBlock(ctx);

        return blockStmt;
    }


    @Override
    public String visitCondition(@NotNull SerpentParser.ConditionContext ctx) {
        return super.visitCondition(ctx);
    }

    @Override
    public String visitGet_var(@NotNull SerpentParser.Get_varContext ctx) {

        String varName = ctx.VAR().toString();
        int addr;

        addr = vars.indexOf(varName);
        if (addr == -1){
            throw  new UnassignVarException(varName);
        }

        return String.format(" %d MLOAD ",  addr * 32);
    }


    @Override
    public String visitAssign(@NotNull SerpentParser.AssignContext ctx) {

        String varName = ctx.VAR().toString();
        int addr = 0;

        addr = vars.indexOf(varName);
        if (addr == -1){
           addr = vars.size();
           vars.add(varName);
        }

        String expression = visitExpression(ctx.expression());

        return String.format(" %s %d MSTORE ", expression, addr * 32);
    }


    @Override
    public String visitInt_val(@NotNull SerpentParser.Int_valContext ctx) {

        if (ctx.OP_NOT() != null)
            return visitExpression(ctx.expression()) + " NOT";

        if (ctx.expression() != null)
            return visitExpression(ctx.expression());

        if (ctx.get_var() != null)
            return visitGet_var(ctx.get_var());

        if (ctx.hex_num() != null)
            return hexStringToDecimalString(ctx.hex_num().getText());

        if (ctx.special_func() != null)
            return visitSpecial_func(ctx.special_func());

        return ctx.INT().toString();
    }


    @Override
    public String visitMul_expr(@NotNull SerpentParser.Mul_exprContext ctx) {
        if (ctx.mul_expr() == null) return visit(ctx.int_val());

        String operand0 = visit(ctx.int_val());
        String operand1 = visit(ctx.mul_expr());

        switch (ctx.OP_MUL().getText().toLowerCase()) {
            case "*": return operand0 + " " + operand1 + " MUL";
            case "/": return operand0 + " " + operand1 + " DIV";
            case "^": return operand0 + " " + operand1 + " EXP";
            case "%": return operand0 + " " + operand1 + " MOD";
            default: throw new UnknownOperandException(ctx.OP_MUL().getText());
        }
    }


    @Override
    public String visitAdd_expr(@NotNull SerpentParser.Add_exprContext ctx) {

        if (ctx.add_expr() == null) return visit(ctx.mul_expr());

        String operand0 = visit(ctx.mul_expr());
        String operand1 = visit(ctx.add_expr());

        switch (ctx.OP_ADD().getText().toLowerCase()) {
            case "+": return operand0 + " " + operand1 + " ADD";
            case "-": return operand0 + " " + operand1 + " SUB";
            default: throw new UnknownOperandException(ctx.OP_ADD().getText());
            }
    }

    @Override
    public String visitRel_exp(@NotNull SerpentParser.Rel_expContext ctx) {

        if (ctx.rel_exp() == null) return visit(ctx.add_expr());

        String operand0 = visit(ctx.rel_exp());
        String operand1 = visit(ctx.add_expr());

        switch (ctx.OP_REL().getText().toLowerCase()) {
            case "<":  return operand1 + " " + operand0 + " LT";
            case ">":  return operand1 + " " + operand0 + " GT";
            case ">=": return operand1 + " " + operand0 + " LT NOT";
            case "<=": return operand1 + " " + operand0 + " GT NOT";
            default: throw new UnknownOperandException(ctx.OP_REL().getText());
        }
    }

    @Override
    public String visitEq_exp(@NotNull SerpentParser.Eq_expContext ctx) {

        if (ctx.eq_exp() == null) return visit(ctx.rel_exp());

        String operand0 = visit(ctx.rel_exp());
        String operand1 = visit(ctx.eq_exp());

        switch (ctx.OP_EQ().getText().toLowerCase()) {
            case "==":  return operand0 + " " + operand1 + " EQ";
            case "!=":  return operand0 + " " + operand1 + " EQ NOT";
            default: throw new UnknownOperandException(ctx.OP_EQ().getText());
        }
    }

    @Override
    public String visitAnd_exp(@NotNull SerpentParser.And_expContext ctx) {

        if (ctx.and_exp() == null) return visit(ctx.eq_exp());

        String operand0 = visit(ctx.eq_exp());
        String operand1 = visit(ctx.and_exp());

        switch (ctx.OP_AND().getText().toLowerCase()) {
            case "&":  return operand0 + " " + operand1 + " AND";
            default: throw new UnknownOperandException(ctx.OP_AND().getText());
        }
    }

    @Override
    public String visitEx_or_exp(@NotNull SerpentParser.Ex_or_expContext ctx) {

        if (ctx.ex_or_exp() == null) return visit(ctx.and_exp());

        String operand0 = visit(ctx.and_exp());
        String operand1 = visit(ctx.ex_or_exp());

        switch (ctx.OP_EX_OR().getText().toLowerCase()) {
            case "xor":  return operand0 + " " + operand1 + " XOR";
            default: throw new UnknownOperandException(ctx.OP_EX_OR().getText());
        }
    }

    @Override
    public String visitIn_or_exp(@NotNull SerpentParser.In_or_expContext ctx) {

        if (ctx.in_or_exp() == null) return visit(ctx.ex_or_exp());

        String operand0 = visit(ctx.ex_or_exp());
        String operand1 = visit(ctx.in_or_exp());

        switch (ctx.OP_IN_OR().getText().toLowerCase()) {
            case "|":  return operand0 + " " + operand1 + " OR";
            default: throw new UnknownOperandException(ctx.OP_IN_OR().getText());
        }
    }

    @Override
    public String visitLog_and_exp(@NotNull SerpentParser.Log_and_expContext ctx) {

        if (ctx.log_and_exp() == null) return visit(ctx.in_or_exp());

        String operand0 = visit(ctx.in_or_exp());
        String operand1 = visit(ctx.log_and_exp());

        switch (ctx.OP_LOG_AND().getText().toLowerCase()) {
            case "and":  return operand0 + " " + operand1 + " NOT NOT MUL";
            case "&&":   return operand0 + " " + operand1 + " NOT NOT MUL";
            default: throw new UnknownOperandException(ctx.OP_LOG_AND().getText());
        }
    }

    @Override
    public String visitLog_or_exp(@NotNull SerpentParser.Log_or_expContext ctx) {

        if (ctx.log_or_exp() == null) return visit(ctx.log_and_exp());

        String operand0 = visit(ctx.log_and_exp());
        String operand1 = visit(ctx.log_or_exp());

        switch (ctx.OP_LOG_OR().getText().toLowerCase()) {
            case "||":  return operand0 + " " + operand1 + " DUP 4 PC ADD JUMPI POP SWAP POP";
            case "or":   return operand0 + " " + operand1 + " DUP 4 PC ADD JUMPI POP SWAP POP";
            default: throw new UnknownOperandException(ctx.OP_LOG_OR().getText());
        }
    }



    @Override
    public String visitMsg_sender(@NotNull SerpentParser.Msg_senderContext ctx) {
        return "CALLER";
    }

    @Override
    public String visitMsg_datasize(@NotNull SerpentParser.Msg_datasizeContext ctx) {
        return "32 CALLDATASIZE DIV ";
    }


    @Override
    public String visitMsg_value(@NotNull SerpentParser.Msg_valueContext ctx) {
        return " CALLVALUE ";
    }

    @Override
    public String visitContract_balance(@NotNull SerpentParser.Contract_balanceContext ctx) {
        return " BALANCE ";
    }

    @Override
    public String visitTx_origin(@NotNull SerpentParser.Tx_originContext ctx) {
        return " ORIGIN ";
    }

    @Override
    public String visitBlock_timestamp(@NotNull SerpentParser.Block_timestampContext ctx) {
        return " TIMESTAMP ";
    }

    @Override
    public String visitBlock_number(@NotNull SerpentParser.Block_numberContext ctx) {
        return " NUMBER ";
    }

    @Override
    public String visitTx_gas(@NotNull SerpentParser.Tx_gasContext ctx) {
        return " GAS ";
    }

    @Override
    public String visitBlock_difficulty(@NotNull SerpentParser.Block_difficultyContext ctx) {
        return " DIFFICULTY ";
    }

    @Override
    public String visitBlock_coinbase(@NotNull SerpentParser.Block_coinbaseContext ctx) {
        return " COINBASE ";
    }

    @Override
    public String visitTx_gasprice(@NotNull SerpentParser.Tx_gaspriceContext ctx) {
        return " GASPRICE ";
    }

    @Override
    public String visitBlock_prevhash(@NotNull SerpentParser.Block_prevhashContext ctx) {
        return " PREVHASH ";
    }

    @Override
    public String visitBlock_gaslimit(@NotNull SerpentParser.Block_gaslimitContext ctx) {
        return " GASLIMIT ";
    }



    @Override
    protected String aggregateResult(String aggregate, String nextResult) {
        return aggregate + nextResult;
    }

    @Override
    protected String defaultResult() {
        return "";
    }


    /**
     * @param hexNum should be in form '0x34fabd34....'
     * @return
     */
    private String hexStringToDecimalString(String hexNum){
        String digits = hexNum.substring(2);
        if (digits.length() % 2 != 0) digits = "0" + digits;
        byte[] numberBytes = Hex.decode(digits);
        return (new BigInteger(1, numberBytes)).toString();
    }



    public static class UnknownOperandException extends RuntimeException {
        public UnknownOperandException(String name) {
            super("unknown operand: " + name);
        }
    }

    public static class UnassignVarException extends RuntimeException {
        public UnassignVarException(String name) {
            super("attempt to access not assigned variable: " + name);
        }
    }

}
