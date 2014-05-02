// $ANTLR 3.5.2 E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g 2014-05-02 09:10:52


  /*  (!!!) Do not update this file manually ,
  *         It was auto generated from the Serpent.g
  *         grammar file.
  */
  package org.ethereum.serpent;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class SerpentLexer extends Lexer {
	public static final int EOF=-1;
	public static final int T__12=12;
	public static final int T__13=13;
	public static final int T__14=14;
	public static final int T__15=15;
	public static final int T__16=16;
	public static final int T__17=17;
	public static final int T__18=18;
	public static final int T__19=19;
	public static final int T__20=20;
	public static final int T__21=21;
	public static final int T__22=22;
	public static final int T__23=23;
	public static final int T__24=24;
	public static final int T__25=25;
	public static final int T__26=26;
	public static final int T__27=27;
	public static final int T__28=28;
	public static final int T__29=29;
	public static final int T__30=30;
	public static final int T__31=31;
	public static final int T__32=32;
	public static final int T__33=33;
	public static final int T__34=34;
	public static final int T__35=35;
	public static final int T__36=36;
	public static final int COMMENT=4;
	public static final int DIGIT=5;
	public static final int HEX_DIGIT=6;
	public static final int HEX_NUMBER=7;
	public static final int IDENT=8;
	public static final int INTEGER=9;
	public static final int LETTER=10;
	public static final int WS=11;

	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public SerpentLexer() {} 
	public SerpentLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public SerpentLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g"; }

	// $ANTLR start "T__12"
	public final void mT__12() throws RecognitionException {
		try {
			int _type = T__12;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:11:7: ( '!' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:11:9: '!'
			{
			match('!'); 
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:12:7: ( '#%' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:12:9: '#%'
			{
			match("#%"); 

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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:13:7: ( '#/' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:13:9: '#/'
			{
			match("#/"); 

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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:14:7: ( '%' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:14:9: '%'
			{
			match('%'); 
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:15:7: ( ')' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:15:9: ')'
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
	// $ANTLR end "T__16"

	// $ANTLR start "T__17"
	public final void mT__17() throws RecognitionException {
		try {
			int _type = T__17;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:16:7: ( '*' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:16:9: '*'
			{
			match('*'); 
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:17:7: ( '+' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:17:9: '+'
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
	// $ANTLR end "T__18"

	// $ANTLR start "T__19"
	public final void mT__19() throws RecognitionException {
		try {
			int _type = T__19;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:18:7: ( '-' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:18:9: '-'
			{
			match('-'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__19"

	// $ANTLR start "T__20"
	public final void mT__20() throws RecognitionException {
		try {
			int _type = T__20;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:19:7: ( '/' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:19:9: '/'
			{
			match('/'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__20"

	// $ANTLR start "T__21"
	public final void mT__21() throws RecognitionException {
		try {
			int _type = T__21;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:20:7: ( ':' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:20:9: ':'
			{
			match(':'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__21"

	// $ANTLR start "T__22"
	public final void mT__22() throws RecognitionException {
		try {
			int _type = T__22;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:21:7: ( '<' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:21:9: '<'
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
	// $ANTLR end "T__22"

	// $ANTLR start "T__23"
	public final void mT__23() throws RecognitionException {
		try {
			int _type = T__23;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:22:7: ( '<=' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:22:9: '<='
			{
			match("<="); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__23"

	// $ANTLR start "T__24"
	public final void mT__24() throws RecognitionException {
		try {
			int _type = T__24;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:23:7: ( '=' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:23:9: '='
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
	// $ANTLR end "T__24"

	// $ANTLR start "T__25"
	public final void mT__25() throws RecognitionException {
		try {
			int _type = T__25;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:24:7: ( '==' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:24:9: '=='
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
	// $ANTLR end "T__25"

	// $ANTLR start "T__26"
	public final void mT__26() throws RecognitionException {
		try {
			int _type = T__26;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:25:7: ( '>' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:25:9: '>'
			{
			match('>'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__26"

	// $ANTLR start "T__27"
	public final void mT__27() throws RecognitionException {
		try {
			int _type = T__27;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:26:7: ( '>=' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:26:9: '>='
			{
			match(">="); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__27"

	// $ANTLR start "T__28"
	public final void mT__28() throws RecognitionException {
		try {
			int _type = T__28;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:27:7: ( ']' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:27:9: ']'
			{
			match(']'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__28"

	// $ANTLR start "T__29"
	public final void mT__29() throws RecognitionException {
		try {
			int _type = T__29;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:28:7: ( '^' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:28:9: '^'
			{
			match('^'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__29"

	// $ANTLR start "T__30"
	public final void mT__30() throws RecognitionException {
		try {
			int _type = T__30;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:29:7: ( 'contract.storage[' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:29:9: 'contract.storage['
			{
			match("contract.storage["); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__30"

	// $ANTLR start "T__31"
	public final void mT__31() throws RecognitionException {
		try {
			int _type = T__31;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:30:7: ( 'else' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:30:9: 'else'
			{
			match("else"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__31"

	// $ANTLR start "T__32"
	public final void mT__32() throws RecognitionException {
		try {
			int _type = T__32;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:31:7: ( 'if' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:31:9: 'if'
			{
			match("if"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__32"

	// $ANTLR start "T__33"
	public final void mT__33() throws RecognitionException {
		try {
			int _type = T__33;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:32:7: ( 'msg.data[' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:32:9: 'msg.data['
			{
			match("msg.data["); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__33"

	// $ANTLR start "T__34"
	public final void mT__34() throws RecognitionException {
		try {
			int _type = T__34;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:33:7: ( 'msg.datasize' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:33:9: 'msg.datasize'
			{
			match("msg.datasize"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__34"

	// $ANTLR start "T__35"
	public final void mT__35() throws RecognitionException {
		try {
			int _type = T__35;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:34:7: ( 'msg.sender' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:34:9: 'msg.sender'
			{
			match("msg.sender"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__35"

	// $ANTLR start "T__36"
	public final void mT__36() throws RecognitionException {
		try {
			int _type = T__36;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:35:7: ( 'return(' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:35:9: 'return('
			{
			match("return("); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__36"

	// $ANTLR start "LETTER"
	public final void mLETTER() throws RecognitionException {
		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:232:17: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:
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

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LETTER"

	// $ANTLR start "DIGIT"
	public final void mDIGIT() throws RecognitionException {
		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:233:16: ( '0' .. '9' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:234:9: ( ( DIGIT )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:234:11: ( DIGIT )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:234:11: ( DIGIT )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= '0' && LA1_0 <= '9')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:
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
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
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

	// $ANTLR start "IDENT"
	public final void mIDENT() throws RecognitionException {
		try {
			int _type = IDENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:235:7: ( LETTER ( LETTER | DIGIT )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:235:9: LETTER ( LETTER | DIGIT )*
			{
			mLETTER(); 

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:235:16: ( LETTER | DIGIT )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( ((LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'Z')||(LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
					break loop2;
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
	// $ANTLR end "IDENT"

	// $ANTLR start "HEX_DIGIT"
	public final void mHEX_DIGIT() throws RecognitionException {
		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:237:20: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
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
	// $ANTLR end "HEX_DIGIT"

	// $ANTLR start "HEX_NUMBER"
	public final void mHEX_NUMBER() throws RecognitionException {
		try {
			int _type = HEX_NUMBER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:238:12: ( ( '0x' | '0X' ) ( HEX_DIGIT )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:238:14: ( '0x' | '0X' ) ( HEX_DIGIT )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:238:14: ( '0x' | '0X' )
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0=='0') ) {
				int LA3_1 = input.LA(2);
				if ( (LA3_1=='x') ) {
					alt3=1;
				}
				else if ( (LA3_1=='X') ) {
					alt3=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 3, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}

			switch (alt3) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:238:15: '0x'
					{
					match("0x"); 

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:238:22: '0X'
					{
					match("0X"); 

					}
					break;

			}

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:238:28: ( HEX_DIGIT )+
			int cnt4=0;
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( ((LA4_0 >= '0' && LA4_0 <= '9')||(LA4_0 >= 'A' && LA4_0 <= 'F')||(LA4_0 >= 'a' && LA4_0 <= 'f')) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
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
					if ( cnt4 >= 1 ) break loop4;
					EarlyExitException eee = new EarlyExitException(4, input);
					throw eee;
				}
				cnt4++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HEX_NUMBER"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:240:4: ( ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:240:6: ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:240:6: ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+
			int cnt5=0;
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( ((LA5_0 >= '\t' && LA5_0 <= '\n')||(LA5_0 >= '\f' && LA5_0 <= '\r')||LA5_0==' ') ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
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

			_channel = HIDDEN;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:241:9: ( '//' ( . )* ( '\\n' | '\\r' ) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:241:11: '//' ( . )* ( '\\n' | '\\r' )
			{
			match("//"); 

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:241:16: ( . )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( (LA6_0=='\n'||LA6_0=='\r') ) {
					alt6=2;
				}
				else if ( ((LA6_0 >= '\u0000' && LA6_0 <= '\t')||(LA6_0 >= '\u000B' && LA6_0 <= '\f')||(LA6_0 >= '\u000E' && LA6_0 <= '\uFFFF')) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:241:16: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop6;
				}
			}

			if ( input.LA(1)=='\n'||input.LA(1)=='\r' ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			_channel = HIDDEN;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT"

	@Override
	public void mTokens() throws RecognitionException {
		// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:8: ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | INTEGER | IDENT | HEX_NUMBER | WS | COMMENT )
		int alt7=30;
		alt7 = dfa7.predict(input);
		switch (alt7) {
			case 1 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:10: T__12
				{
				mT__12(); 

				}
				break;
			case 2 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:16: T__13
				{
				mT__13(); 

				}
				break;
			case 3 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:22: T__14
				{
				mT__14(); 

				}
				break;
			case 4 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:28: T__15
				{
				mT__15(); 

				}
				break;
			case 5 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:34: T__16
				{
				mT__16(); 

				}
				break;
			case 6 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:40: T__17
				{
				mT__17(); 

				}
				break;
			case 7 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:46: T__18
				{
				mT__18(); 

				}
				break;
			case 8 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:52: T__19
				{
				mT__19(); 

				}
				break;
			case 9 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:58: T__20
				{
				mT__20(); 

				}
				break;
			case 10 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:64: T__21
				{
				mT__21(); 

				}
				break;
			case 11 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:70: T__22
				{
				mT__22(); 

				}
				break;
			case 12 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:76: T__23
				{
				mT__23(); 

				}
				break;
			case 13 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:82: T__24
				{
				mT__24(); 

				}
				break;
			case 14 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:88: T__25
				{
				mT__25(); 

				}
				break;
			case 15 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:94: T__26
				{
				mT__26(); 

				}
				break;
			case 16 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:100: T__27
				{
				mT__27(); 

				}
				break;
			case 17 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:106: T__28
				{
				mT__28(); 

				}
				break;
			case 18 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:112: T__29
				{
				mT__29(); 

				}
				break;
			case 19 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:118: T__30
				{
				mT__30(); 

				}
				break;
			case 20 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:124: T__31
				{
				mT__31(); 

				}
				break;
			case 21 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:130: T__32
				{
				mT__32(); 

				}
				break;
			case 22 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:136: T__33
				{
				mT__33(); 

				}
				break;
			case 23 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:142: T__34
				{
				mT__34(); 

				}
				break;
			case 24 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:148: T__35
				{
				mT__35(); 

				}
				break;
			case 25 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:154: T__36
				{
				mT__36(); 

				}
				break;
			case 26 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:160: INTEGER
				{
				mINTEGER(); 

				}
				break;
			case 27 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:168: IDENT
				{
				mIDENT(); 

				}
				break;
			case 28 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:174: HEX_NUMBER
				{
				mHEX_NUMBER(); 

				}
				break;
			case 29 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:185: WS
				{
				mWS(); 

				}
				break;
			case 30 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:1:188: COMMENT
				{
				mCOMMENT(); 

				}
				break;

		}
	}


	protected DFA7 dfa7 = new DFA7(this);
	static final String DFA7_eotS =
		"\10\uffff\1\33\1\uffff\1\35\1\37\1\41\2\uffff\5\25\1\26\15\uffff\2\25"+
		"\1\52\2\25\1\uffff\2\25\1\uffff\3\25\1\62\1\uffff\2\25\3\uffff\2\25\1"+
		"\uffff\2\25\2\uffff\1\25\4\uffff";
	static final String DFA7_eofS =
		"\101\uffff";
	static final String DFA7_minS =
		"\1\11\1\uffff\1\45\5\uffff\1\57\1\uffff\3\75\2\uffff\1\157\1\154\1\146"+
		"\1\163\1\145\1\130\15\uffff\1\156\1\163\1\60\1\147\1\164\1\uffff\1\164"+
		"\1\145\1\uffff\1\56\1\165\1\162\1\60\1\144\1\162\1\141\1\uffff\1\141\1"+
		"\uffff\1\156\1\143\1\164\1\50\1\164\1\141\1\uffff\1\56\1\133\3\uffff";
	static final String DFA7_maxS =
		"\1\172\1\uffff\1\57\5\uffff\1\57\1\uffff\3\75\2\uffff\1\157\1\154\1\146"+
		"\1\163\1\145\1\170\15\uffff\1\156\1\163\1\172\1\147\1\164\1\uffff\1\164"+
		"\1\145\1\uffff\1\56\1\165\1\162\1\172\1\163\1\162\1\141\1\uffff\1\141"+
		"\1\uffff\1\156\1\143\1\164\1\50\1\164\1\141\1\uffff\1\56\1\163\3\uffff";
	static final String DFA7_acceptS =
		"\1\uffff\1\1\1\uffff\1\4\1\5\1\6\1\7\1\10\1\uffff\1\12\3\uffff\1\21\1"+
		"\22\6\uffff\1\33\1\32\1\35\1\2\1\3\1\36\1\11\1\14\1\13\1\16\1\15\1\20"+
		"\1\17\5\uffff\1\34\2\uffff\1\25\7\uffff\1\24\1\uffff\1\30\6\uffff\1\31"+
		"\2\uffff\1\23\1\26\1\27";
	static final String DFA7_specialS =
		"\101\uffff}>";
	static final String[] DFA7_transitionS = {
			"\2\27\1\uffff\2\27\22\uffff\1\27\1\1\1\uffff\1\2\1\uffff\1\3\3\uffff"+
			"\1\4\1\5\1\6\1\uffff\1\7\1\uffff\1\10\1\24\11\26\1\11\1\uffff\1\12\1"+
			"\13\1\14\2\uffff\32\25\2\uffff\1\15\1\16\2\uffff\2\25\1\17\1\25\1\20"+
			"\3\25\1\21\3\25\1\22\4\25\1\23\10\25",
			"",
			"\1\30\11\uffff\1\31",
			"",
			"",
			"",
			"",
			"",
			"\1\32",
			"",
			"\1\34",
			"\1\36",
			"\1\40",
			"",
			"",
			"\1\42",
			"\1\43",
			"\1\44",
			"\1\45",
			"\1\46",
			"\1\47\37\uffff\1\47",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\50",
			"\1\51",
			"\12\25\7\uffff\32\25\6\uffff\32\25",
			"\1\53",
			"\1\54",
			"",
			"\1\55",
			"\1\56",
			"",
			"\1\57",
			"\1\60",
			"\1\61",
			"\12\25\7\uffff\32\25\6\uffff\32\25",
			"\1\63\16\uffff\1\64",
			"\1\65",
			"\1\66",
			"",
			"\1\67",
			"",
			"\1\70",
			"\1\71",
			"\1\72",
			"\1\73",
			"\1\74",
			"\1\75",
			"",
			"\1\76",
			"\1\77\27\uffff\1\100",
			"",
			"",
			""
	};

	static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
	static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
	static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
	static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
	static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
	static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
	static final short[][] DFA7_transition;

	static {
		int numStates = DFA7_transitionS.length;
		DFA7_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
		}
	}

	protected class DFA7 extends DFA {

		public DFA7(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 7;
			this.eot = DFA7_eot;
			this.eof = DFA7_eof;
			this.min = DFA7_min;
			this.max = DFA7_max;
			this.accept = DFA7_accept;
			this.special = DFA7_special;
			this.transition = DFA7_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( T__12 | T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | INTEGER | IDENT | HEX_NUMBER | WS | COMMENT );";
		}
	}

}
