package org.ethereum.serpent;

import org.antlr.v4.runtime.misc.NotNull;
import org.ethereum.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 05/05/14 13:41
 */
public class SerpentToAssemblyCompiler extends SerpentBaseVisitor<String> {

    private int labelIndex = 0;
    private List<String> vars   = new ArrayList<>();

    private Map<String, Integer> arraysSize = new HashMap<>();
    private List<String> arraysIndex = new ArrayList<>();

    @Override
    public String visitParse(@NotNull SerpentParser.ParseContext ctx) {

        String codeBlock = visit(ctx.block());
        int memSize = vars.size() * 32 - ( vars.size() > 0 ? 1 : 0);

        String initMemCodeBlock = "";
        if ( ! arraysSize.isEmpty() && vars.size() > 0)
            initMemCodeBlock = String.format(" 0 %d MSTORE8 ", memSize);

        if (memSize == 0)
            codeBlock= codeBlock.replace("@vars_table@", "0");
        else
            codeBlock= codeBlock.replace("@vars_table@", memSize + 1 + "");

        return  initMemCodeBlock + codeBlock;
    }

    @Override
    public String visitParse_init_code_block(@NotNull SerpentParser.Parse_init_code_blockContext ctx) {

        String initBlock = visit(ctx.block(0));
        int memSize = vars.size() * 32 - ( vars.size() > 0 ? 1 : 0);

        String initMemInitBlock = "";
        if ( ! arraysSize.isEmpty() && vars.size() > 0)
            initMemInitBlock = String.format(" 0 %d MSTORE8 ", memSize);

        if (memSize == 0)
            initBlock= initBlock.replace("@vars_table@", "0");
        else
            initBlock= initBlock.replace("@vars_table@", memSize + 1 + "");

        vars.clear();
        String codeBlock = visit(ctx.block(1));
        memSize = vars.size() * 32 - ( vars.size() > 0 ? 1 : 0);

        if (memSize == 0)
            codeBlock= codeBlock.replace("@vars_table@", "0");
        else
            codeBlock= codeBlock.replace("@vars_table@", memSize + 1 + "");


        String initMemCodeBlock = "";
        if ( ! arraysSize.isEmpty() && vars.size() > 0)
            initMemCodeBlock = String.format(" 0 %d MSTORE8 ", memSize);

        return String.format(" [init %s %s init] [code %s %s code] ", initMemInitBlock, initBlock,
                initMemCodeBlock,  codeBlock);
    }

    @Override
    public String visitIf_elif_else_stmt(@NotNull SerpentParser.If_elif_else_stmtContext ctx) {

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
        for (; i < count; ++i) {

            // if the condition much at the end jump out of the main if
            // append to general retCode
            // TODO: [NOT REF_X JUMPI] elif_body [REF_Y JUMP LABEL_X] Y=total_end
            // TODO extract the condition and the body
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
                String.format(" LABEL_%d %s NOT REF_%d JUMPI %s REF_%d JUMP LABEL_%d ",
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
        if (addr == -1) {
            throw  new UnassignVarException(varName);
        }
        return String.format(" %d MLOAD ",  addr * 32);
    }

    @Override
    public String visitAssign(@NotNull SerpentParser.AssignContext ctx) {

        String varName = ctx.VAR().toString();
        int addr = 0;

        // msg assigned has two arrays to calc
        if (ctx.msg_func() != null) {

            String msgCode = visitMsg_func(ctx.msg_func(), varName);
            return msgCode;
        } else if (ctx.arr_def() != null) {
            // if it's an array the all management is different
            String arrayCode = visitArr_def(ctx.arr_def());

            // calc the pointer addr
            int pos = getArraySize(arrayCode);
            arraysSize.put(varName, pos);
            arraysIndex.add(varName);

            return arrayCode;
        } else {
            String expression = visitExpression(ctx.expression());
            addr = vars.indexOf(varName);
            if (addr == -1) {
                addr = vars.size();
                vars.add(varName);
            }
            return String.format(" %s %d MSTORE ", expression, addr * 32);
        }
    }

    @Override
    public String visitContract_storage_load(@NotNull SerpentParser.Contract_storage_loadContext ctx) {

        String operand0 = visitExpression(ctx.expression());

        return String.format(" %s SLOAD ", operand0);
    }

    @Override
    public String visitContract_storage_assign(@NotNull SerpentParser.Contract_storage_assignContext ctx) {

        String operand0 = visitExpression(ctx.expression(0));
        String operand1 = visitExpression(ctx.expression(1));

        return String.format(" %s %s SSTORE ", operand1, operand0);
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

        if (ctx.msg_data() != null)
            return visitMsg_data(ctx.msg_data());

        if (ctx.contract_storage_load() != null)
            return visitContract_storage_load(ctx.contract_storage_load());

        if (ctx.send_func() != null)
            return visitSend_func(ctx.send_func());

        if (ctx.array_retreive() != null)
            return visitArray_retreive(ctx.array_retreive());

        return ctx.INT().toString();
    }

    @Override
    public String visitArr_def(@NotNull SerpentParser.Arr_defContext ctx) {

        List<SerpentParser.Int_valContext> numElements = ctx.int_val();
        int arraySize = numElements.size() * 32 + 32;

        StringBuffer arrayInit = new StringBuffer();
        int i = 32;
        for (SerpentParser.Int_valContext int_val : ctx.int_val()) {

            arrayInit.append(String.format(" DUP %d ADD %s SWAP MSTORE ", i, visit(int_val)));
            i += 32;
        }

       return String.format(" MSIZE 32 ADD MSIZE %s %d SWAP MSTORE ", arrayInit, arraySize);
    }

    @Override
    public String visitArray_assign(@NotNull SerpentParser.Array_assignContext ctx) {

        int order = this.arraysIndex.indexOf( ctx.VAR().toString());
        if (order == -1) {
            throw new Error("array with that name was not defined");
        }

        //calcAllocatedBefore();
        int allocSize = 0;
        for (int i = 0; i < order; ++i ) {
            String var = arraysIndex.get(i);
            allocSize += arraysSize.get(var);
        }

        String index = visit(ctx.int_val());
        String assignValue = visit(ctx.expression());

        return String.format(" %s 32 %s MUL 32 ADD %d ADD @vars_table@ ADD MSTORE ", assignValue, index, allocSize);
    }

    @Override
    public String visitArray_retreive(@NotNull SerpentParser.Array_retreiveContext ctx) {

        int order = this.arraysIndex.indexOf( ctx.VAR().toString());
        if (order == -1) {
            throw new Error("array with that name was not defined");
        }

        int allocSize = 32;
        for (int i = 0; i < order; ++i ) {
            String var = arraysIndex.get(i);
            allocSize += arraysSize.get(var);
        }

        String index = visit(ctx.int_val());

        return String.format(" 32 %s MUL %d ADD @vars_table@ ADD MLOAD ", index, allocSize );
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
            case "#/": return operand0 + " " + operand1 + " SDIV";
            case "#%": return operand0 + " " + operand1 + " SMOD";
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
    public String visitContract_address(@NotNull SerpentParser.Contract_addressContext ctx) {
        return " ADDRESS ";
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
    public String visitRet_func_1(@NotNull SerpentParser.Ret_func_1Context ctx) {

        String operand0 = visit(ctx.expression());

        return  String.format(" %s MSIZE SWAP MSIZE MSTORE 32 SWAP RETURN ", operand0);
    }

    @Override
    public String visitRet_func_2(@NotNull SerpentParser.Ret_func_2Context ctx) {

        String operand0 = visit(ctx.expression(0));
        String operand1 = visit(ctx.expression(1));

        return String.format(" %s 32 MUL %s RETURN ", operand1, operand0);
    }

    @Override
    public String visitSuicide_func(@NotNull SerpentParser.Suicide_funcContext ctx) {

        String operand0 = visit(ctx.expression());

        return String.format(" %s SUICIDE ", operand0);
    }

    @Override
    public String visitStop_func(@NotNull SerpentParser.Stop_funcContext ctx) {
        return " STOP ";
    }

    @Override
    public String visitMsg_data(@NotNull SerpentParser.Msg_dataContext ctx) {

        String operand0 = visit(ctx.expression());

        return String.format("%s 32 MUL CALLDATALOAD ",  operand0);
    }

    public String visitMsg_func(@NotNull SerpentParser.Msg_funcContext ctx, String varName) {

//        msg_func: 'msg' '(' int_val ',' int_val ',' int_val ',' arr_def ',' int_val  ',' int_val')' ;
//        msg_func: 'msg' '(' [gas] ',' [to] ',' [value] ',' arr_def ',' [in_len]  ',' [out_len]')' ;

        String operand0   = visit(ctx.int_val(0));
        String operand1   = visit(ctx.int_val(1));
        String operand2   = visit(ctx.int_val(2));

        String loadInData = visit(ctx.arr_def());

        String inSizeCallParam   = visit(ctx.int_val(3));
        String outSizeCallParam   = visit(ctx.int_val(4));

        // TODO: 1. allocate out_memory and push ptr
        // TODO: 2. push ptr for in_memory allocated

        String randomArrayName = new String(HashUtil.randomPeerId());

        int inSize = Integer.parseInt( inSizeCallParam );
        int outSize = Integer.parseInt( outSizeCallParam );

        arraysSize.put(randomArrayName, inSize * 32 + 32);
        arraysIndex.add(randomArrayName);

        int outSizeVal = outSize * 32 + 32;
        arraysSize.put(varName, outSize * 32 + 32);
        arraysIndex.add(varName);

//        [OUTDATASIZE] [OUTDATASTART] [INDATASIZE] [INDATASTART] [VALUE] [TO] [GAS] CALL
//        [OUTDATASIZE] [OUTDATASTART] [INDATASIZE] [INDATASTART] ***ARR_IN_SET*** [VALUE] [TO] [GAS] CALL
        //X_X = [ 32 + 128 + 6 * 32 ] = [ var_table_size + in_arr_size + out_arr_size ]

        // this code allocates the memory block for the out data,
        // and saves the size in typical array format [size, element_1, element_2, ...]
        String outArrSet = String.format( " %d MSIZE MSTORE   0 %d MSIZE ADD MSTORE8 ", outSizeVal, outSizeVal - 32 );

        return  String.format("%d MSIZE %s %d %s %s %s %s CALL ",
                outSizeVal, outArrSet ,inSize * 32, loadInData, operand2, operand1, operand0);
    }

    @Override
    public String visitSend_func(@NotNull SerpentParser.Send_funcContext ctx) {

        String operand0 = visit(ctx.int_val(0));
        String operand1 = visit(ctx.int_val(1));
        String operand2 = visit(ctx.int_val(2));

//        OUTDATASIZE OUTDATASTART INDATASIZE INDATASTART VALUE TO GAS CALL
        return  String.format("0 0 0 0 %s %s %s CALL ", operand2, operand1, operand0);
    }

    @Override
    public String visitCreate_func(@NotNull SerpentParser.Create_funcContext ctx) {
        String operand0 = visit(ctx.int_val(0));
        String operand1 = visit(ctx.int_val(1));
        String operand2 = visit(ctx.int_val(2));

//        MEM_SIZE MEM_START GAS CREATE
        return  String.format(" %s %s %s CREATE ", operand2, operand1, operand0);
    }

    @Override
    public String visitAsm(@NotNull SerpentParser.AsmContext ctx) {

        int size = ctx.asm_symbol().getChildCount();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < size ; ++i) {

            String symbol = ctx.asm_symbol().children.get(i).toString();
            symbol = symbol.trim();

            // exclude all that is not an assembly code
            if (symbol.equals("[asm") || symbol.equals("asm]") || symbol.equals("\n")) continue;

            boolean match = Pattern.matches("[0-9]+", symbol);
            if (match) {

                int byteVal = Integer.parseInt(symbol);
                if (byteVal > 255 || byteVal < 0) throw new Error("In the [asm asm] block should be placed only byte range numbers");
            }

            match = Pattern.matches("0[xX][0-9a-fA-F]+", symbol);
            if (match) {

                int byteVal = Integer.parseInt(symbol.substring(2), 16);
                if (byteVal > 255 || byteVal < 0) throw new Error("In the [asm asm] block should be placed only byte range numbers");
                symbol = byteVal + "";
            }

            sb.append(symbol).append(" ");
        }

        return "[asm " + sb.toString() + " asm]";
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
    private String hexStringToDecimalString(String hexNum) {
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

    private Integer getMsgOutputArraySize(String code) {

        String result = "0";
        Pattern pattern = Pattern.compile("<out_size ([0-9])* out_size>");
        Matcher matcher = pattern.matcher(code.trim());
        if (matcher.find()) {

            String group = matcher.group(0);
            result = group.replaceAll("<out_size ([0-9]*) out_size>", "$1").trim();
        }
        return Integer.parseInt(result);
    }

    private Integer getMsgInputArraySize(String code) {

        String result = "0";
        Pattern pattern = Pattern.compile("<in_size ([0-9])* in_size>");
        Matcher matcher = pattern.matcher(code.trim());
        if (matcher.find()) {

            String group = matcher.group(0);
            result = group.replaceAll("<in_size ([0-9]*) in_size>", "$1").trim();
        }
        return Integer.parseInt(result);
    }

    private String cleanMsgString (String code) {

        String result = "";

        Pattern pattern = Pattern.compile("<(.*?)>");
        result= code.replaceAll("<(.*?)>", "");

        return result;
    }


    /**
     * After the array deff code is set
     * extract the size out of code string
     */
    private Integer getArraySize(String code) {

        String result = "0";
        Pattern pattern = Pattern.compile(" [0-9]* SWAP MSTORE$");
        Matcher matcher = pattern.matcher(code.trim());
        if (matcher.find()) {

            String group = matcher.group(0);
            result = group.replace("SWAP MSTORE", "").trim();
        }
        return Integer.parseInt(result);
    }

}
