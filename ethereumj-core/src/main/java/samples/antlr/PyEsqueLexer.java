// $ANTLR 3.5.2 E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g 2014-05-01 16:36:17

  package samples.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PyEsqueLexer extends Lexer {
	public static final int EOF=-1;
	public static final int BLOCK=4;
	public static final int DIGIT=5;
	public static final int Dedent=6;
	public static final int INTEGER=7;
	public static final int Id=8;
	public static final int Indent=9;
	public static final int NL=10;
	public static final int NewLine=11;
	public static final int SP=12;
	public static final int SpaceChars=13;


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


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public PyEsqueLexer() {} 
	public PyEsqueLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public PyEsqueLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g"; }

	// $ANTLR start "NewLine"
	public final void mNewLine() throws RecognitionException {
		try {
			int _type = NewLine;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			CommonToken SP1=null;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:61:2: ( NL ( SP )? )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:61:4: NL ( SP )?
			{
			mNL(); 

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:61:7: ( SP )?
			int alt1=2;
			int LA1_0 = input.LA(1);
			if ( (LA1_0=='\t'||LA1_0==' ') ) {
				alt1=1;
			}
			switch (alt1) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:61:7: SP
					{
					int SP1Start39 = getCharIndex();
					int SP1StartLine39 = getLine();
					int SP1StartCharPos39 = getCharPositionInLine();
					mSP(); 
					SP1 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, SP1Start39, getCharIndex()-1);
					SP1.setLine(SP1StartLine39);
					SP1.setCharPositionInLine(SP1StartCharPos39);

					}
					break;

			}


			     int n = (SP1!=null?SP1.getText():null) == null ? 0 : (SP1!=null?SP1.getText():null).length();
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

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NewLine"

	// $ANTLR start "Id"
	public final void mId() throws RecognitionException {
		try {
			int _type = Id;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:85:2: ( ( 'a' .. 'z' | 'A' .. 'Z' )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:85:4: ( 'a' .. 'z' | 'A' .. 'Z' )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:85:4: ( 'a' .. 'z' | 'A' .. 'Z' )+
			int cnt2=0;
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( ((LA2_0 >= 'A' && LA2_0 <= 'Z')||(LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:
					{
					if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt2 >= 1 ) break loop2;
					EarlyExitException eee = new EarlyExitException(2, input);
					throw eee;
				}
				cnt2++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Id"

	// $ANTLR start "SpaceChars"
	public final void mSpaceChars() throws RecognitionException {
		try {
			int _type = SpaceChars;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:89:2: ( SP )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:89:4: SP
			{
			mSP(); 

			skip();
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SpaceChars"

	// $ANTLR start "NL"
	public final void mNL() throws RecognitionException {
		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:92:17: ( ( '\\r' )? '\\n' | '\\r' )
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0=='\r') ) {
				int LA4_1 = input.LA(2);
				if ( (LA4_1=='\n') ) {
					alt4=1;
				}

				else {
					alt4=2;
				}

			}
			else if ( (LA4_0=='\n') ) {
				alt4=1;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}

			switch (alt4) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:92:19: ( '\\r' )? '\\n'
					{
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:92:19: ( '\\r' )?
					int alt3=2;
					int LA3_0 = input.LA(1);
					if ( (LA3_0=='\r') ) {
						alt3=1;
					}
					switch (alt3) {
						case 1 :
							// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:92:19: '\\r'
							{
							match('\r'); 
							}
							break;

					}

					match('\n'); 
					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:92:32: '\\r'
					{
					match('\r'); 
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NL"

	// $ANTLR start "SP"
	public final void mSP() throws RecognitionException {
		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:93:17: ( ( ' ' | '\\t' )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:93:19: ( ' ' | '\\t' )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:93:19: ( ' ' | '\\t' )+
			int cnt5=0;
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( (LA5_0=='\t'||LA5_0==' ') ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:
					{
					if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt5 >= 1 ) break loop5;
					EarlyExitException eee = new EarlyExitException(5, input);
					throw eee;
				}
				cnt5++;
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SP"

	// $ANTLR start "Indent"
	public final void mIndent() throws RecognitionException {
		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:94:17: ()
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:94:19: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Indent"

	// $ANTLR start "Dedent"
	public final void mDedent() throws RecognitionException {
		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:95:17: ()
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:95:19: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Dedent"

	// $ANTLR start "DIGIT"
	public final void mDIGIT() throws RecognitionException {
		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:104:16: ( '0' .. '9' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DIGIT"

	// $ANTLR start "INTEGER"
	public final void mINTEGER() throws RecognitionException {
		try {
			int _type = INTEGER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:105:9: ( ( DIGIT )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:105:11: ( DIGIT )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:105:11: ( DIGIT )+
			int cnt6=0;
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( ((LA6_0 >= '0' && LA6_0 <= '9')) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt6 >= 1 ) break loop6;
					EarlyExitException eee = new EarlyExitException(6, input);
					throw eee;
				}
				cnt6++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INTEGER"

	@Override
	public void mTokens() throws RecognitionException {
		// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:1:8: ( NewLine | Id | SpaceChars | INTEGER )
		int alt7=4;
		switch ( input.LA(1) ) {
		case '\n':
		case '\r':
			{
			alt7=1;
			}
			break;
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
		case 'G':
		case 'H':
		case 'I':
		case 'J':
		case 'K':
		case 'L':
		case 'M':
		case 'N':
		case 'O':
		case 'P':
		case 'Q':
		case 'R':
		case 'S':
		case 'T':
		case 'U':
		case 'V':
		case 'W':
		case 'X':
		case 'Y':
		case 'Z':
		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'f':
		case 'g':
		case 'h':
		case 'i':
		case 'j':
		case 'k':
		case 'l':
		case 'm':
		case 'n':
		case 'o':
		case 'p':
		case 'q':
		case 'r':
		case 's':
		case 't':
		case 'u':
		case 'v':
		case 'w':
		case 'x':
		case 'y':
		case 'z':
			{
			alt7=2;
			}
			break;
		case '\t':
		case ' ':
			{
			alt7=3;
			}
			break;
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			{
			alt7=4;
			}
			break;
		default:
			NoViableAltException nvae =
				new NoViableAltException("", 7, 0, input);
			throw nvae;
		}
		switch (alt7) {
			case 1 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:1:10: NewLine
				{
				mNewLine(); 

				}
				break;
			case 2 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:1:18: Id
				{
				mId(); 

				}
				break;
			case 3 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:1:21: SpaceChars
				{
				mSpaceChars(); 

				}
				break;
			case 4 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:1:32: INTEGER
				{
				mINTEGER(); 

				}
				break;

		}
	}



}
