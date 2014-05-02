grammar PyEsque;

options {
  language = Java;
  output = AST;
  //output=template;
}

tokens {
  BLOCK;
}

@header {
  package samples.antlr;
}

@lexer::header {
  package samples.antlr;
}



@lexer::members {

  private int previousIndents = -1;
  private int indentLevel = 0;
  java.util.Queue<Token> tokens = new java.util.LinkedList<Token>();

  @Override
  public void emit(Token t) {
    state.token = t;
    tokens.offer(t);
  }

  @Override
  public Token nextToken() {
    super.nextToken();
    return tokens.isEmpty() ? getEOFToken() : tokens.poll();
  }

  private void jump(int ttype) {
    indentLevel += (ttype == Dedent ? -1 : 1);
    emit(new CommonToken(ttype, "level=" + indentLevel));
  }
}


parse
 : block EOF -> block
 ;

block
 : Indent block_atoms Dedent -> ^(BLOCK block_atoms)
 ;

block_atoms
 :  (Id | block)+
 ;

NewLine
 : NL SP?
   {
     int n = $SP.text == null ? 0 : $SP.text.length();
     if(n > previousIndents) {
       jump(Indent);
       previousIndents = n;
     }
     else if(n < previousIndents) {
       jump(Dedent);
       previousIndents = n;
     }
     else if(input.LA(1) == EOF) {
       while(indentLevel > 0) {
         jump(Dedent);
       }
     }
     else {
       skip();
     }
   }
 ;


Id
 : ('a'..'z' | 'A'..'Z')+
 ;

SpaceChars
 : SP {skip();}
 ;

fragment NL     : '\r'? '\n' | '\r';
fragment SP     : (' ' | '\t')+;
fragment Indent : ;
fragment Dedent : ;


expression
	:	INTEGER*
	;



fragment DIGIT : '0'..'9';
INTEGER : DIGIT+ ;
