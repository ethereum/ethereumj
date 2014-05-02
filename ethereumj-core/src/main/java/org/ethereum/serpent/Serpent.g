/*******************************************************************************
 * Ethereum high level language grammar definition
 *******************************************************************************/
grammar Serpent;

options {
  //language = Java;
  //output   = AST;
  output   = template;
}

@header {

  /*  (!!!) Do not update this file manually ,
  *         It was auto generated from the Serpent.g
  *         grammar file.
  */
  package org.ethereum.serpent;
  import org.ethereum.util.Utils;
}

@lexer::header {

  /*  (!!!) Do not update this file manually ,
  *         It was auto generated from the Serpent.g
  *         grammar file.
  */
  package org.ethereum.serpent;
}


@members {
    private java.util.ArrayList<String> globals = new java.util.ArrayList<String>();
    private int labelIndex = 0;
}



program
	:  gen_body
       {
            String conVars = "";

            if (globals.size() > 0){

                conVars = "0 " + (globals.size() * 32 - 1) + " MSTORE8";
            }
       }
       -> concat(left={conVars}, right={$gen_body.st})
	;

test_1
	:
       (
          set_var -> concat(left={$test_1.st}, right={$set_var.st})
        | get_var -> concat(left={$test_1.st}, right={$get_var.st})   )*
	;

/*
test_2
	:
       (
          set_var -> concat(left={$test_2.st}, right={$set_var.st})
       )*

       (
          if_stmt       -> concat(left={$test_2.st}, right={$if_stmt.st})
        | if_else_stmt  -> concat(left={$test_2.st}, right={$if_else_stmt.st})
        )
	;
*/



gen_body
    :
       (
        set_var -> concat(left={$gen_body.st}, right={$set_var.st})
        | storage_save -> concat(left={$gen_body.st}, right={$storage_save.st})
        | return_stmt -> concat(left={$gen_body.st}, right={$return_stmt.st})
       )* ( if_else_stmt -> concat(left={$gen_body.st}, right={$if_else_stmt.st}))?

    ;




// [if a==10:\n b=20 \n]
// [10, 0, 'MLOAD', 'EQ', 'NOT', 'REF_0', 'JUMPI', 20, 32, 'MSTORE', 'LABEL_0']
if_stmt
    :
        'if' unr_expr ':' gen_body
// (!!!)RECURSION ON if_stmt  ( 'elif' cond_expr ':' elif_body=set_var )*
                                                                -> ifStmt(cond={$unr_expr.st}, body={$gen_body.st}, index={labelIndex++})
    ;


// [if a==10:\n b=20 \nelse: \n b=30]
// [10, 0, 'MLOAD', 'EQ', 'NOT', 'REF_1', 'JUMPI', 20, 32, 'MSTORE', 'REF_0', 'JUMP', 'LABEL_1', 30, 32, 'MSTORE', 'LABEL_0']
//  a 10             EQ NOT REF_01 JUMPI            30 0 MSTORE      REF_0     JUMP    LABEL_01  20  0    MSTORE     LABEL_0

if_else_stmt
    :
        'if' unr_expr ':' if_body=gen_body
// (!!!)RECURSION ON if_stmt     ( 'elif' unr_expr ':' elif_body=set_var )*
        'else' ':' else_body=gen_body    -> ifElseStmt(cond={$unr_expr.st}, if_body={$if_body.st},
                                                          else_body={$else_body.st}, if_index={labelIndex++}, else_index={labelIndex++}) ;




// [multi_set_var] (a=set_var->compile(left={$program.st}, right={$a.st}))*
set_var
    :
        {int varIndex = -1;}
        (a=var {  // TODO: change it from atom to something else

            // check if that variable already defined
            // if it didn't add it to list
            // if it did use the index * 32 for memory address
            varIndex = globals.indexOf($a.st.toString());
            if (varIndex == -1 ) {globals.add($a.st.toString()); varIndex = globals.size() - 1; }
        }

        '=' b=bin_expr) -> set_var(param_a={$b.st}, param_b={32 * varIndex})
    ;

 get_var
    :
       {int varIndex = -1;}
       (a=var  {

             // If there is no such var throw exception
            varIndex = globals.indexOf($a.st.toString());
            if (varIndex == -1 ) {
                Error err = new Error("var undefined: " + $a.st.toString());
                throw err;
            }
        ;}

      -> get_var(varIndex={32 * varIndex})
    )
    ;


unr_expr
    :
        {boolean negative = false;}
        ('!' {negative = !negative;} )*  a=cond_expr -> not(param={negative? $a.st : $a.st + " NOT"})

    ;


cond_expr
    :   a=bin_expr
        (
                 '==' b=bin_expr -> equals(left={$a.st},right={$b.st})   |
                 '<'  b=bin_expr -> lessThan(left={$a.st},right={$b.st}) |
                 '<='  b=bin_expr -> lessEqThan(left={$a.st},right={$b.st}) |
                 '>='  b=bin_expr -> greatEqThan(left={$a.st},right={$b.st}) |
                 '>'  b=bin_expr -> greatThan(left={$a.st},right={$b.st})

        |   -> {$a.st}
        )
    ;

storage_save
      : 'contract.storage['index=bin_expr']' '=' assignment=bin_expr -> ssave(index={$index.st}, data={$assignment.st})
      ;

bin_expr
    :   (a=atom -> {$a.st})
        (( '+'  b=atom -> add(left={$bin_expr.st}, right={$b.st})   |
           '-'  b=atom -> sub(left={$bin_expr.st}, right={$b.st})   |
           '*'  b=atom -> mul(left={$bin_expr.st}, right={$b.st})   |
           '/'  b=atom -> div(left={$bin_expr.st}, right={$b.st})   |
           '^'  b=atom -> exp(left={$bin_expr.st}, right={$b.st})   |
           '%'  b=atom -> mod(left={$bin_expr.st}, right={$b.st})   |
           '#/' b=atom -> sdiv(left={$bin_expr.st}, right={$b.st})  |
           '#%' b=atom -> smod(left={$bin_expr.st}, right={$b.st})
           )*)
    ;

                // "if !a==10:\n b=20 \nelse: \n b=30"

atom
    :
      storage_load -> iconst(value={$storage_load.st})
      | msg_sender -> iconst(value={$msg_sender.st})
      | msg_datasize -> iconst(value={$msg_datasize.st})
      | msg_load -> iconst(value={$msg_load.st})
      | get_var  -> iconst(value={$get_var.st})
      | INTEGER -> iconst(value={$INTEGER.text})
      | hex_num -> iconst(value={$hex_num.st})

    ;

hex_num
   :
         HEX_NUMBER
         {
            String dec_num = Utils.hexStringToDecimalString($HEX_NUMBER.text);
         }
         -> iconst(value={dec_num})
   ;

var
    : IDENT -> refVar(id={$IDENT.text} )
    ;

storage_load
      : 'contract.storage['bin_expr']' -> sload(index={$bin_expr.st})
      ;

msg_load
      : 'msg.data['bin_expr']'  -> calldataload(index={$bin_expr.st})
      ;

msg_sender
      : 'msg.sender' -> msdSender()
      ;

msg_datasize
      : 'msg.datasize' -> msgDatasize()
      ;

return_stmt
      : 'return('bin_expr')'  -> returnStmt(index={$bin_expr.st})
      ;


fragment LETTER : ('a'..'z' | 'A'..'Z') ;
fragment DIGIT : '0'..'9';
INTEGER : DIGIT+ ;
IDENT : LETTER (LETTER | DIGIT)*;

fragment HEX_DIGIT : ('0'..'9' | 'a'..'f' | 'A'..'F');
HEX_NUMBER : ('0x' | '0X' )HEX_DIGIT+;

WS : (' ' | '\t' | '\n' | '\r' | '\f')+ {$channel = HIDDEN;};
COMMENT : '//' .* ('\n'|'\r') {$channel = HIDDEN;};







