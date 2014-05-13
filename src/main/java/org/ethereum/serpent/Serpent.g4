grammar Serpent;

tokens {
INDENT, DEDENT }
@lexer::header {
  import com.yuvalshavit.antlr4.DenterHelper;
}


@lexer::members {
  private final DenterHelper denter = new DenterHelper(NL, SerpentParser.INDENT, SerpentParser.DEDENT) {
    @Override
    public Token pullToken() {
      return SerpentLexer.super.nextToken(); // must be to super.nextToken, or we'll recurse forever!
    }


  };

  @Override
  public Token nextToken() {
    return denter.nextToken();
  }
}


parse: block EOF
    ;


block:  (assign | special_func | if_elif_else_stmt | while_stmt | ret_func)* ;


if_elif_else_stmt:  'if'   condition ':' INDENT block DEDENT
                   ('elif' condition ':' INDENT block DEDENT)*
                   ('else:' INDENT block DEDENT)?
       ;

while_stmt: 'while' condition ':' INDENT block DEDENT;


/* special functions */

special_func :
  msg_datasize |
  msg_sender |
  msg_value |
  tx_gasprice |
  tx_origin |
  tx_gas |
  contract_balance |
  block_prevhash |
  block_coinbase |
  block_timestamp |
  block_number |
  block_difficulty |
  block_gaslimit
  ;

msg_datasize
      : 'msg.datasize' ;

msg_sender
      : 'msg.sender' ;

msg_value
      : 'msg.value' ;

tx_gasprice
      : 'tx.gasprice' ;

tx_origin
      : 'tx.origin' ;

tx_gas
      : 'tx.gas' ;

contract_balance
      : 'contract.balance' ;

block_prevhash
      : 'block.prevhash' ;

block_coinbase
      : 'block.coinbase' ;

block_timestamp
      : 'block.timestamp' ;

block_number
      : 'block.number' ;

block_difficulty
      : 'block.difficulty' ;

block_gaslimit
      : 'block.gaslimit' ;



assign:  VAR EQ_OP expression NL;


mul_expr
    :   int_val
    |   mul_expr OP_MUL int_val
    ;

add_expr
    :   mul_expr
    |   add_expr OP_ADD mul_expr
    ;

rel_exp
    :   add_expr
    |   rel_exp OP_REL add_expr
    ;

eq_exp
    :   rel_exp
    |   eq_exp OP_EQ rel_exp
    ;

and_exp
    :   eq_exp
    |   and_exp OP_AND eq_exp
    ;

ex_or_exp
    :   and_exp
    |   ex_or_exp OP_EX_OR and_exp
    ;

in_or_exp
    :   ex_or_exp
    |   in_or_exp OP_IN_OR ex_or_exp
    ;

log_and_exp
    :   in_or_exp
    |   log_and_exp OP_LOG_AND in_or_exp
    ;

log_or_exp
    :   log_and_exp
    |   log_or_exp OP_LOG_OR log_and_exp
    ;


expression : log_or_exp ;

condition: expression ;


int_val : INT | hex_num | get_var | special_func | '(' expression ')' | OP_NOT '(' expression ')' ;
// todo:  here the val should include also retrieve a variable

hex_num
   : HEX_NUMBER
   ;

ret_func:  'return' '(' INT ')' NL;

get_var: VAR;


/*  'xor', 'and', 'or', 'not' operands should be defined
    first among lexer rules because it
    can be mismatched as a var lexa   */
OP_EX_OR: 'xor';
OP_LOG_AND: '&&' | 'and';
OP_LOG_OR: '||' | 'or';
OP_NOT: '!' | 'not';


EQ_OP: '='  ;

NL: ('\r'? '\n' ' '*); // note the ' '*;
WS: [ \t]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;

INT: [0-9]+;
VAR:  [a-zA-Z][a-zA-Z0-9]* ;


OP_ADD : '+' | '-';
OP_MUL : '*' | '/' | '^' | '%' ;

OP_REL : '<' | '>' | '<=' | '>=';
OP_EQ : '==' | '!=';
OP_AND: '&';
OP_IN_OR: '|';


HEX_DIGIT : [0-9a-fA-F];
HEX_NUMBER : ('0x' | '0X' )HEX_DIGIT+;
