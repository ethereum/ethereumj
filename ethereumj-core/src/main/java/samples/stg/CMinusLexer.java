// $ANTLR 3.5.2 E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g 2014-04-27 13:24:16

package samples.stg;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class CMinusLexer extends Lexer {
	public static final int EOF=-1;
	public static final int T__7=7;
	public static final int T__8=8;
	public static final int T__9=9;
	public static final int T__10=10;
	public static final int T__11=11;
	public static final int T__12=12;
	public static final int T__13=13;
	public static final int T__14=14;
	public static final int T__15=15;
	public static final int T__16=16;
	public static final int T__17=17;
	public static final int T__18=18;
	public static final int T__19=19;
	public static final int ID=4;
	public static final int INT=5;
	public static final int WS=6;

	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public CMinusLexer() {} 
	public CMinusLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public CMinusLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g"; }

	// $ANTLR start "T__7"
	public final void mT__7() throws RecognitionException {
		try {
			int _type = T__7;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:6:6: ( '(' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:6:8: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__7"

	// $ANTLR start "T__8"
	public final void mT__8() throws RecognitionException {
		try {
			int _type = T__8;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:7:6: ( ')' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:7:8: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__8"

	// $ANTLR start "T__9"
	public final void mT__9() throws RecognitionException {
		try {
			int _type = T__9;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:8:6: ( '+' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:8:8: '+'
			{
			match('+'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__9"

	// $ANTLR start "T__10"
	public final void mT__10() throws RecognitionException {
		try {
			int _type = T__10;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:9:7: ( ',' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:9:9: ','
			{
			match(','); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__10"

	// $ANTLR start "T__11"
	public final void mT__11() throws RecognitionException {
		try {
			int _type = T__11;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:10:7: ( ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:10:9: ';'
			{
			match(';'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__11"

	// $ANTLR start "T__12"
	public final void mT__12() throws RecognitionException {
		try {
			int _type = T__12;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:11:7: ( '<' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:11:9: '<'
			{
			match('<'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__12"

	// $ANTLR start "T__13"
	public final void mT__13() throws RecognitionException {
		try {
			int _type = T__13;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:12:7: ( '=' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:12:9: '='
			{
			match('='); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__13"

	// $ANTLR start "T__14"
	public final void mT__14() throws RecognitionException {
		try {
			int _type = T__14;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:13:7: ( '==' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:13:9: '=='
			{
			match("=="); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__14"

	// $ANTLR start "T__15"
	public final void mT__15() throws RecognitionException {
		try {
			int _type = T__15;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:14:7: ( 'char' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:14:9: 'char'
			{
			match("char"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__15"

	// $ANTLR start "T__16"
	public final void mT__16() throws RecognitionException {
		try {
			int _type = T__16;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:15:7: ( 'for' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:15:9: 'for'
			{
			match("for"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__16"

	// $ANTLR start "T__17"
	public final void mT__17() throws RecognitionException {
		try {
			int _type = T__17;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:16:7: ( 'int' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:16:9: 'int'
			{
			match("int"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__17"

	// $ANTLR start "T__18"
	public final void mT__18() throws RecognitionException {
		try {
			int _type = T__18;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:17:7: ( '{' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:17:9: '{'
			{
			match('{'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__18"

	// $ANTLR start "T__19"
	public final void mT__19() throws RecognitionException {
		try {
			int _type = T__19;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:18:7: ( '}' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:18:9: '}'
			{
			match('}'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__19"

	// $ANTLR start "ID"
	public final void mID() throws RecognitionException {
		try {
			int _type = ID;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:145:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:145:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:145:33: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||LA1_0=='_'||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
					break loop1;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ID"

	// $ANTLR start "INT"
	public final void mINT() throws RecognitionException {
		try {
			int _type = INT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:148:5: ( ( '0' .. '9' )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:148:7: ( '0' .. '9' )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:148:7: ( '0' .. '9' )+
			int cnt2=0;
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( ((LA2_0 >= '0' && LA2_0 <= '9')) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:
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
	// $ANTLR end "INT"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:151:5: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:151:9: ( ' ' | '\\t' | '\\r' | '\\n' )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:151:9: ( ' ' | '\\t' | '\\r' | '\\n' )+
			int cnt3=0;
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( ((LA3_0 >= '\t' && LA3_0 <= '\n')||LA3_0=='\r'||LA3_0==' ') ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
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
					if ( cnt3 >= 1 ) break loop3;
					EarlyExitException eee = new EarlyExitException(3, input);
					throw eee;
				}
				cnt3++;
			}

			_channel=HIDDEN;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	@Override
	public void mTokens() throws RecognitionException {
		// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:8: ( T__7 | T__8 | T__9 | T__10 | T__11 | T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | ID | INT | WS )
		int alt4=16;
		switch ( input.LA(1) ) {
		case '(':
			{
			alt4=1;
			}
			break;
		case ')':
			{
			alt4=2;
			}
			break;
		case '+':
			{
			alt4=3;
			}
			break;
		case ',':
			{
			alt4=4;
			}
			break;
		case ';':
			{
			alt4=5;
			}
			break;
		case '<':
			{
			alt4=6;
			}
			break;
		case '=':
			{
			int LA4_7 = input.LA(2);
			if ( (LA4_7=='=') ) {
				alt4=8;
			}

			else {
				alt4=7;
			}

			}
			break;
		case 'c':
			{
			int LA4_8 = input.LA(2);
			if ( (LA4_8=='h') ) {
				int LA4_18 = input.LA(3);
				if ( (LA4_18=='a') ) {
					int LA4_21 = input.LA(4);
					if ( (LA4_21=='r') ) {
						int LA4_24 = input.LA(5);
						if ( ((LA4_24 >= '0' && LA4_24 <= '9')||(LA4_24 >= 'A' && LA4_24 <= 'Z')||LA4_24=='_'||(LA4_24 >= 'a' && LA4_24 <= 'z')) ) {
							alt4=14;
						}

						else {
							alt4=9;
						}

					}

					else {
						alt4=14;
					}

				}

				else {
					alt4=14;
				}

			}

			else {
				alt4=14;
			}

			}
			break;
		case 'f':
			{
			int LA4_9 = input.LA(2);
			if ( (LA4_9=='o') ) {
				int LA4_19 = input.LA(3);
				if ( (LA4_19=='r') ) {
					int LA4_22 = input.LA(4);
					if ( ((LA4_22 >= '0' && LA4_22 <= '9')||(LA4_22 >= 'A' && LA4_22 <= 'Z')||LA4_22=='_'||(LA4_22 >= 'a' && LA4_22 <= 'z')) ) {
						alt4=14;
					}

					else {
						alt4=10;
					}

				}

				else {
					alt4=14;
				}

			}

			else {
				alt4=14;
			}

			}
			break;
		case 'i':
			{
			int LA4_10 = input.LA(2);
			if ( (LA4_10=='n') ) {
				int LA4_20 = input.LA(3);
				if ( (LA4_20=='t') ) {
					int LA4_23 = input.LA(4);
					if ( ((LA4_23 >= '0' && LA4_23 <= '9')||(LA4_23 >= 'A' && LA4_23 <= 'Z')||LA4_23=='_'||(LA4_23 >= 'a' && LA4_23 <= 'z')) ) {
						alt4=14;
					}

					else {
						alt4=11;
					}

				}

				else {
					alt4=14;
				}

			}

			else {
				alt4=14;
			}

			}
			break;
		case '{':
			{
			alt4=12;
			}
			break;
		case '}':
			{
			alt4=13;
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
		case '_':
		case 'a':
		case 'b':
		case 'd':
		case 'e':
		case 'g':
		case 'h':
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
			alt4=14;
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
			alt4=15;
			}
			break;
		case '\t':
		case '\n':
		case '\r':
		case ' ':
			{
			alt4=16;
			}
			break;
		default:
			NoViableAltException nvae =
				new NoViableAltException("", 4, 0, input);
			throw nvae;
		}
		switch (alt4) {
			case 1 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:10: T__7
				{
				mT__7(); 

				}
				break;
			case 2 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:15: T__8
				{
				mT__8(); 

				}
				break;
			case 3 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:20: T__9
				{
				mT__9(); 

				}
				break;
			case 4 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:25: T__10
				{
				mT__10(); 

				}
				break;
			case 5 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:31: T__11
				{
				mT__11(); 

				}
				break;
			case 6 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:37: T__12
				{
				mT__12(); 

				}
				break;
			case 7 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:43: T__13
				{
				mT__13(); 

				}
				break;
			case 8 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:49: T__14
				{
				mT__14(); 

				}
				break;
			case 9 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:55: T__15
				{
				mT__15(); 

				}
				break;
			case 10 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:61: T__16
				{
				mT__16(); 

				}
				break;
			case 11 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:67: T__17
				{
				mT__17(); 

				}
				break;
			case 12 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:73: T__18
				{
				mT__18(); 

				}
				break;
			case 13 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:79: T__19
				{
				mT__19(); 

				}
				break;
			case 14 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:85: ID
				{
				mID(); 

				}
				break;
			case 15 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:88: INT
				{
				mINT(); 

				}
				break;
			case 16 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:1:92: WS
				{
				mWS(); 

				}
				break;

		}
	}



}
