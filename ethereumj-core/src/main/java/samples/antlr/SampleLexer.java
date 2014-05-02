// $ANTLR 3.5.2 E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g 2014-04-27 11:25:21

  package samples.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class SampleLexer extends Lexer {
	public static final int EOF=-1;
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
	public static final int T__37=37;
	public static final int T__38=38;
	public static final int T__39=39;
	public static final int T__40=40;
	public static final int T__41=41;
	public static final int T__42=42;
	public static final int T__43=43;
	public static final int T__44=44;
	public static final int T__45=45;
	public static final int T__46=46;
	public static final int T__47=47;
	public static final int T__48=48;
	public static final int T__49=49;
	public static final int T__50=50;
	public static final int T__51=51;
	public static final int T__52=52;
	public static final int T__53=53;
	public static final int T__54=54;
	public static final int T__55=55;
	public static final int T__56=56;
	public static final int T__57=57;
	public static final int T__58=58;
	public static final int T__59=59;
	public static final int T__60=60;
	public static final int CHAR_LITERAL=4;
	public static final int COMMENT=5;
	public static final int DIGIT=6;
	public static final int IDENT=7;
	public static final int INTEGER=8;
	public static final int LETTER=9;
	public static final int MULTILINE_COMMENT=10;
	public static final int STRING_LITERAL=11;
	public static final int WS=12;

	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public SampleLexer() {} 
	public SampleLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public SampleLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g"; }

	// $ANTLR start "T__13"
	public final void mT__13() throws RecognitionException {
		try {
			int _type = T__13;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:11:7: ( '(' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:11:9: '('
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
	// $ANTLR end "T__13"

	// $ANTLR start "T__14"
	public final void mT__14() throws RecognitionException {
		try {
			int _type = T__14;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:12:7: ( ')' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:12:9: ')'
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
	// $ANTLR end "T__14"

	// $ANTLR start "T__15"
	public final void mT__15() throws RecognitionException {
		try {
			int _type = T__15;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:13:7: ( '*' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:13:9: '*'
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
	// $ANTLR end "T__15"

	// $ANTLR start "T__16"
	public final void mT__16() throws RecognitionException {
		try {
			int _type = T__16;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:14:7: ( '+' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:14:9: '+'
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
	// $ANTLR end "T__16"

	// $ANTLR start "T__17"
	public final void mT__17() throws RecognitionException {
		try {
			int _type = T__17;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:15:7: ( ',' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:15:9: ','
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
	// $ANTLR end "T__17"

	// $ANTLR start "T__18"
	public final void mT__18() throws RecognitionException {
		try {
			int _type = T__18;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:16:7: ( '-' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:16:9: '-'
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
	// $ANTLR end "T__18"

	// $ANTLR start "T__19"
	public final void mT__19() throws RecognitionException {
		try {
			int _type = T__19;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:17:7: ( '.' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:17:9: '.'
			{
			match('.'); 
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:18:7: ( '..' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:18:9: '..'
			{
			match(".."); 

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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:19:7: ( '/' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:19:9: '/'
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
	// $ANTLR end "T__21"

	// $ANTLR start "T__22"
	public final void mT__22() throws RecognitionException {
		try {
			int _type = T__22;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:20:7: ( '/=' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:20:9: '/='
			{
			match("/="); 

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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:21:7: ( ':' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:21:9: ':'
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
	// $ANTLR end "T__23"

	// $ANTLR start "T__24"
	public final void mT__24() throws RecognitionException {
		try {
			int _type = T__24;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:22:7: ( ':=' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:22:9: ':='
			{
			match(":="); 

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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:23:7: ( ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:23:9: ';'
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
	// $ANTLR end "T__25"

	// $ANTLR start "T__26"
	public final void mT__26() throws RecognitionException {
		try {
			int _type = T__26;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:24:7: ( '<' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:24:9: '<'
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
	// $ANTLR end "T__26"

	// $ANTLR start "T__27"
	public final void mT__27() throws RecognitionException {
		try {
			int _type = T__27;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:25:7: ( '<=' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:25:9: '<='
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
	// $ANTLR end "T__27"

	// $ANTLR start "T__28"
	public final void mT__28() throws RecognitionException {
		try {
			int _type = T__28;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:26:7: ( '=' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:26:9: '='
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
	// $ANTLR end "T__28"

	// $ANTLR start "T__29"
	public final void mT__29() throws RecognitionException {
		try {
			int _type = T__29;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:27:7: ( '>' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:27:9: '>'
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
	// $ANTLR end "T__29"

	// $ANTLR start "T__30"
	public final void mT__30() throws RecognitionException {
		try {
			int _type = T__30;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:28:7: ( '>=' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:28:9: '>='
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
	// $ANTLR end "T__30"

	// $ANTLR start "T__31"
	public final void mT__31() throws RecognitionException {
		try {
			int _type = T__31;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:29:7: ( 'Boolean' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:29:9: 'Boolean'
			{
			match("Boolean"); 

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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:30:7: ( 'Char' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:30:9: 'Char'
			{
			match("Char"); 

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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:31:7: ( 'Integer' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:31:9: 'Integer'
			{
			match("Integer"); 

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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:32:7: ( 'String' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:32:9: 'String'
			{
			match("String"); 

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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:33:7: ( '[' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:33:9: '['
			{
			match('['); 
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:34:7: ( ']' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:34:9: ']'
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
	// $ANTLR end "T__36"

	// $ANTLR start "T__37"
	public final void mT__37() throws RecognitionException {
		try {
			int _type = T__37;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:35:7: ( 'and' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:35:9: 'and'
			{
			match("and"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__37"

	// $ANTLR start "T__38"
	public final void mT__38() throws RecognitionException {
		try {
			int _type = T__38;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:36:7: ( 'array' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:36:9: 'array'
			{
			match("array"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__38"

	// $ANTLR start "T__39"
	public final void mT__39() throws RecognitionException {
		try {
			int _type = T__39;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:37:7: ( 'begin' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:37:9: 'begin'
			{
			match("begin"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__39"

	// $ANTLR start "T__40"
	public final void mT__40() throws RecognitionException {
		try {
			int _type = T__40;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:38:7: ( 'constant' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:38:9: 'constant'
			{
			match("constant"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__40"

	// $ANTLR start "T__41"
	public final void mT__41() throws RecognitionException {
		try {
			int _type = T__41;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:39:7: ( 'else' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:39:9: 'else'
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
	// $ANTLR end "T__41"

	// $ANTLR start "T__42"
	public final void mT__42() throws RecognitionException {
		try {
			int _type = T__42;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:40:7: ( 'elsif' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:40:9: 'elsif'
			{
			match("elsif"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__42"

	// $ANTLR start "T__43"
	public final void mT__43() throws RecognitionException {
		try {
			int _type = T__43;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:41:7: ( 'end' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:41:9: 'end'
			{
			match("end"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__43"

	// $ANTLR start "T__44"
	public final void mT__44() throws RecognitionException {
		try {
			int _type = T__44;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:42:7: ( 'exit' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:42:9: 'exit'
			{
			match("exit"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__44"

	// $ANTLR start "T__45"
	public final void mT__45() throws RecognitionException {
		try {
			int _type = T__45;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:43:7: ( 'function' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:43:9: 'function'
			{
			match("function"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__45"

	// $ANTLR start "T__46"
	public final void mT__46() throws RecognitionException {
		try {
			int _type = T__46;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:44:7: ( 'if' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:44:9: 'if'
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
	// $ANTLR end "T__46"

	// $ANTLR start "T__47"
	public final void mT__47() throws RecognitionException {
		try {
			int _type = T__47;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:45:7: ( 'loop' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:45:9: 'loop'
			{
			match("loop"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__47"

	// $ANTLR start "T__48"
	public final void mT__48() throws RecognitionException {
		try {
			int _type = T__48;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:46:7: ( 'mod' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:46:9: 'mod'
			{
			match("mod"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__48"

	// $ANTLR start "T__49"
	public final void mT__49() throws RecognitionException {
		try {
			int _type = T__49;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:47:7: ( 'not' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:47:9: 'not'
			{
			match("not"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__49"

	// $ANTLR start "T__50"
	public final void mT__50() throws RecognitionException {
		try {
			int _type = T__50;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:48:7: ( 'of' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:48:9: 'of'
			{
			match("of"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__50"

	// $ANTLR start "T__51"
	public final void mT__51() throws RecognitionException {
		try {
			int _type = T__51;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:49:7: ( 'or' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:49:9: 'or'
			{
			match("or"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__51"

	// $ANTLR start "T__52"
	public final void mT__52() throws RecognitionException {
		try {
			int _type = T__52;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:50:7: ( 'procedure' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:50:9: 'procedure'
			{
			match("procedure"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__52"

	// $ANTLR start "T__53"
	public final void mT__53() throws RecognitionException {
		try {
			int _type = T__53;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:51:7: ( 'program' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:51:9: 'program'
			{
			match("program"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__53"

	// $ANTLR start "T__54"
	public final void mT__54() throws RecognitionException {
		try {
			int _type = T__54;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:52:7: ( 'record' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:52:9: 'record'
			{
			match("record"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__54"

	// $ANTLR start "T__55"
	public final void mT__55() throws RecognitionException {
		try {
			int _type = T__55;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:53:7: ( 'return' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:53:9: 'return'
			{
			match("return"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__55"

	// $ANTLR start "T__56"
	public final void mT__56() throws RecognitionException {
		try {
			int _type = T__56;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:54:7: ( 'then' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:54:9: 'then'
			{
			match("then"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__56"

	// $ANTLR start "T__57"
	public final void mT__57() throws RecognitionException {
		try {
			int _type = T__57;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:55:7: ( 'type' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:55:9: 'type'
			{
			match("type"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__57"

	// $ANTLR start "T__58"
	public final void mT__58() throws RecognitionException {
		try {
			int _type = T__58;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:56:7: ( 'var' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:56:9: 'var'
			{
			match("var"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__58"

	// $ANTLR start "T__59"
	public final void mT__59() throws RecognitionException {
		try {
			int _type = T__59;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:57:7: ( 'when' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:57:9: 'when'
			{
			match("when"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__59"

	// $ANTLR start "T__60"
	public final void mT__60() throws RecognitionException {
		try {
			int _type = T__60;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:58:7: ( 'while' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:58:9: 'while'
			{
			match("while"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__60"

	// $ANTLR start "MULTILINE_COMMENT"
	public final void mMULTILINE_COMMENT() throws RecognitionException {
		try {
			int _type = MULTILINE_COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:180:19: ( '/*' ( . )* '*/' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:180:21: '/*' ( . )* '*/'
			{
			match("/*"); 

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:180:26: ( . )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0=='*') ) {
					int LA1_1 = input.LA(2);
					if ( (LA1_1=='/') ) {
						alt1=2;
					}
					else if ( ((LA1_1 >= '\u0000' && LA1_1 <= '.')||(LA1_1 >= '0' && LA1_1 <= '\uFFFF')) ) {
						alt1=1;
					}

				}
				else if ( ((LA1_0 >= '\u0000' && LA1_0 <= ')')||(LA1_0 >= '+' && LA1_0 <= '\uFFFF')) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:180:26: .
					{
					matchAny(); 
					}
					break;

				default :
					break loop1;
				}
			}

			match("*/"); 

			_channel = HIDDEN;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MULTILINE_COMMENT"

	// $ANTLR start "STRING_LITERAL"
	public final void mSTRING_LITERAL() throws RecognitionException {
		try {
			int _type = STRING_LITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			int c;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:183:2: ( '\"' ( '\"' '\"' |c=~ ( '\"' | '\\r' | '\\n' ) )* '\"' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:183:4: '\"' ( '\"' '\"' |c=~ ( '\"' | '\\r' | '\\n' ) )* '\"'
			{
			match('\"'); 
			 StringBuilder b = new StringBuilder(); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:185:3: ( '\"' '\"' |c=~ ( '\"' | '\\r' | '\\n' ) )*
			loop2:
			while (true) {
				int alt2=3;
				int LA2_0 = input.LA(1);
				if ( (LA2_0=='\"') ) {
					int LA2_1 = input.LA(2);
					if ( (LA2_1=='\"') ) {
						alt2=1;
					}

				}
				else if ( ((LA2_0 >= '\u0000' && LA2_0 <= '\t')||(LA2_0 >= '\u000B' && LA2_0 <= '\f')||(LA2_0 >= '\u000E' && LA2_0 <= '!')||(LA2_0 >= '#' && LA2_0 <= '\uFFFF')) ) {
					alt2=2;
				}

				switch (alt2) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:185:5: '\"' '\"'
					{
					match('\"'); 
					match('\"'); 
					 b.appendCodePoint('"');
					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:186:5: c=~ ( '\"' | '\\r' | '\\n' )
					{
					c= input.LA(1);
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					 b.appendCodePoint(c);
					}
					break;

				default :
					break loop2;
				}
			}

			match('\"'); 
			 setText(b.toString()); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING_LITERAL"

	// $ANTLR start "CHAR_LITERAL"
	public final void mCHAR_LITERAL() throws RecognitionException {
		try {
			int _type = CHAR_LITERAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:193:2: ( '\\'' . '\\'' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:193:4: '\\'' . '\\''
			{
			match('\''); 
			matchAny(); 
			match('\''); 
			setText(getText().substring(1,2));
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CHAR_LITERAL"

	// $ANTLR start "LETTER"
	public final void mLETTER() throws RecognitionException {
		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:196:17: ( ( 'a' .. 'z' | 'A' .. 'Z' ) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:197:16: ( '0' .. '9' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:198:9: ( ( DIGIT )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:198:11: ( DIGIT )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:198:11: ( DIGIT )+
			int cnt3=0;
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:
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
					if ( cnt3 >= 1 ) break loop3;
					EarlyExitException eee = new EarlyExitException(3, input);
					throw eee;
				}
				cnt3++;
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:199:7: ( LETTER ( LETTER | DIGIT )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:199:9: LETTER ( LETTER | DIGIT )*
			{
			mLETTER(); 

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:199:16: ( LETTER | DIGIT )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( ((LA4_0 >= '0' && LA4_0 <= '9')||(LA4_0 >= 'A' && LA4_0 <= 'Z')||(LA4_0 >= 'a' && LA4_0 <= 'z')) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:
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
					break loop4;
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

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:200:4: ( ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:200:6: ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:200:6: ( ' ' | '\\t' | '\\n' | '\\r' | '\\f' )+
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
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:
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
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:201:9: ( '//' ( . )* ( '\\n' | '\\r' ) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:201:11: '//' ( . )* ( '\\n' | '\\r' )
			{
			match("//"); 

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:201:16: ( . )*
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
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:201:16: .
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
		// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:8: ( T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | MULTILINE_COMMENT | STRING_LITERAL | CHAR_LITERAL | INTEGER | IDENT | WS | COMMENT )
		int alt7=55;
		alt7 = dfa7.predict(input);
		switch (alt7) {
			case 1 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:10: T__13
				{
				mT__13(); 

				}
				break;
			case 2 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:16: T__14
				{
				mT__14(); 

				}
				break;
			case 3 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:22: T__15
				{
				mT__15(); 

				}
				break;
			case 4 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:28: T__16
				{
				mT__16(); 

				}
				break;
			case 5 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:34: T__17
				{
				mT__17(); 

				}
				break;
			case 6 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:40: T__18
				{
				mT__18(); 

				}
				break;
			case 7 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:46: T__19
				{
				mT__19(); 

				}
				break;
			case 8 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:52: T__20
				{
				mT__20(); 

				}
				break;
			case 9 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:58: T__21
				{
				mT__21(); 

				}
				break;
			case 10 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:64: T__22
				{
				mT__22(); 

				}
				break;
			case 11 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:70: T__23
				{
				mT__23(); 

				}
				break;
			case 12 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:76: T__24
				{
				mT__24(); 

				}
				break;
			case 13 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:82: T__25
				{
				mT__25(); 

				}
				break;
			case 14 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:88: T__26
				{
				mT__26(); 

				}
				break;
			case 15 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:94: T__27
				{
				mT__27(); 

				}
				break;
			case 16 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:100: T__28
				{
				mT__28(); 

				}
				break;
			case 17 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:106: T__29
				{
				mT__29(); 

				}
				break;
			case 18 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:112: T__30
				{
				mT__30(); 

				}
				break;
			case 19 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:118: T__31
				{
				mT__31(); 

				}
				break;
			case 20 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:124: T__32
				{
				mT__32(); 

				}
				break;
			case 21 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:130: T__33
				{
				mT__33(); 

				}
				break;
			case 22 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:136: T__34
				{
				mT__34(); 

				}
				break;
			case 23 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:142: T__35
				{
				mT__35(); 

				}
				break;
			case 24 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:148: T__36
				{
				mT__36(); 

				}
				break;
			case 25 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:154: T__37
				{
				mT__37(); 

				}
				break;
			case 26 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:160: T__38
				{
				mT__38(); 

				}
				break;
			case 27 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:166: T__39
				{
				mT__39(); 

				}
				break;
			case 28 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:172: T__40
				{
				mT__40(); 

				}
				break;
			case 29 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:178: T__41
				{
				mT__41(); 

				}
				break;
			case 30 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:184: T__42
				{
				mT__42(); 

				}
				break;
			case 31 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:190: T__43
				{
				mT__43(); 

				}
				break;
			case 32 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:196: T__44
				{
				mT__44(); 

				}
				break;
			case 33 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:202: T__45
				{
				mT__45(); 

				}
				break;
			case 34 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:208: T__46
				{
				mT__46(); 

				}
				break;
			case 35 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:214: T__47
				{
				mT__47(); 

				}
				break;
			case 36 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:220: T__48
				{
				mT__48(); 

				}
				break;
			case 37 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:226: T__49
				{
				mT__49(); 

				}
				break;
			case 38 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:232: T__50
				{
				mT__50(); 

				}
				break;
			case 39 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:238: T__51
				{
				mT__51(); 

				}
				break;
			case 40 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:244: T__52
				{
				mT__52(); 

				}
				break;
			case 41 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:250: T__53
				{
				mT__53(); 

				}
				break;
			case 42 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:256: T__54
				{
				mT__54(); 

				}
				break;
			case 43 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:262: T__55
				{
				mT__55(); 

				}
				break;
			case 44 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:268: T__56
				{
				mT__56(); 

				}
				break;
			case 45 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:274: T__57
				{
				mT__57(); 

				}
				break;
			case 46 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:280: T__58
				{
				mT__58(); 

				}
				break;
			case 47 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:286: T__59
				{
				mT__59(); 

				}
				break;
			case 48 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:292: T__60
				{
				mT__60(); 

				}
				break;
			case 49 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:298: MULTILINE_COMMENT
				{
				mMULTILINE_COMMENT(); 

				}
				break;
			case 50 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:316: STRING_LITERAL
				{
				mSTRING_LITERAL(); 

				}
				break;
			case 51 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:331: CHAR_LITERAL
				{
				mCHAR_LITERAL(); 

				}
				break;
			case 52 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:344: INTEGER
				{
				mINTEGER(); 

				}
				break;
			case 53 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:352: IDENT
				{
				mIDENT(); 

				}
				break;
			case 54 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:358: WS
				{
				mWS(); 

				}
				break;
			case 55 :
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:1:361: COMMENT
				{
				mCOMMENT(); 

				}
				break;

		}
	}


	protected DFA7 dfa7 = new DFA7(this);
	static final String DFA7_eotS =
		"\7\uffff\1\51\1\55\1\57\1\uffff\1\61\1\uffff\1\63\4\46\2\uffff\17\46\21"+
		"\uffff\14\46\1\130\3\46\1\134\1\135\12\46\1\152\4\46\1\160\2\46\1\uffff"+
		"\1\46\1\164\1\165\2\uffff\5\46\1\174\3\46\1\u0080\2\46\1\uffff\3\46\1"+
		"\u0086\1\46\1\uffff\1\u0088\1\46\1\u008a\2\uffff\4\46\1\u008f\1\u0090"+
		"\1\uffff\1\u0091\2\46\1\uffff\2\46\1\u0096\1\u0097\1\46\1\uffff\1\u0099"+
		"\1\uffff\1\46\1\uffff\4\46\3\uffff\1\u009f\2\46\1\u00a2\2\uffff\1\46\1"+
		"\uffff\3\46\1\u00a7\1\u00a8\1\uffff\1\u00a9\1\u00aa\1\uffff\3\46\1\u00ae"+
		"\4\uffff\1\u00af\1\u00b0\1\46\3\uffff\1\u00b2\1\uffff";
	static final String DFA7_eofS =
		"\u00b3\uffff";
	static final String DFA7_minS =
		"\1\11\6\uffff\1\56\1\52\1\75\1\uffff\1\75\1\uffff\1\75\1\157\1\150\1\156"+
		"\1\164\2\uffff\1\156\1\145\1\157\1\154\1\165\1\146\3\157\1\146\1\162\1"+
		"\145\1\150\1\141\1\150\21\uffff\1\157\1\141\1\164\1\162\1\144\1\162\1"+
		"\147\1\156\1\163\1\144\1\151\1\156\1\60\1\157\1\144\1\164\2\60\1\157\1"+
		"\143\1\145\1\160\1\162\1\145\1\154\1\162\1\145\1\151\1\60\1\141\1\151"+
		"\1\163\1\145\1\60\1\164\1\143\1\uffff\1\160\2\60\2\uffff\1\143\1\157\1"+
		"\165\1\156\1\145\1\60\1\156\1\154\1\145\1\60\1\147\1\156\1\uffff\1\171"+
		"\1\156\1\164\1\60\1\146\1\uffff\1\60\1\164\1\60\2\uffff\1\145\3\162\2"+
		"\60\1\uffff\1\60\1\145\1\141\1\uffff\1\145\1\147\2\60\1\141\1\uffff\1"+
		"\60\1\uffff\1\151\1\uffff\1\144\1\141\1\144\1\156\3\uffff\1\60\1\156\1"+
		"\162\1\60\2\uffff\1\156\1\uffff\1\157\1\165\1\155\2\60\1\uffff\2\60\1"+
		"\uffff\1\164\1\156\1\162\1\60\4\uffff\2\60\1\145\3\uffff\1\60\1\uffff";
	static final String DFA7_maxS =
		"\1\172\6\uffff\1\56\2\75\1\uffff\1\75\1\uffff\1\75\1\157\1\150\1\156\1"+
		"\164\2\uffff\1\162\1\145\1\157\1\170\1\165\1\146\3\157\2\162\1\145\1\171"+
		"\1\141\1\150\21\uffff\1\157\1\141\1\164\1\162\1\144\1\162\1\147\1\156"+
		"\1\163\1\144\1\151\1\156\1\172\1\157\1\144\1\164\2\172\1\157\1\164\1\145"+
		"\1\160\1\162\1\151\1\154\1\162\1\145\1\151\1\172\1\141\1\151\1\163\1\151"+
		"\1\172\1\164\1\143\1\uffff\1\160\2\172\2\uffff\1\147\1\157\1\165\1\156"+
		"\1\145\1\172\1\156\1\154\1\145\1\172\1\147\1\156\1\uffff\1\171\1\156\1"+
		"\164\1\172\1\146\1\uffff\1\172\1\164\1\172\2\uffff\1\145\3\162\2\172\1"+
		"\uffff\1\172\1\145\1\141\1\uffff\1\145\1\147\2\172\1\141\1\uffff\1\172"+
		"\1\uffff\1\151\1\uffff\1\144\1\141\1\144\1\156\3\uffff\1\172\1\156\1\162"+
		"\1\172\2\uffff\1\156\1\uffff\1\157\1\165\1\155\2\172\1\uffff\2\172\1\uffff"+
		"\1\164\1\156\1\162\1\172\4\uffff\2\172\1\145\3\uffff\1\172\1\uffff";
	static final String DFA7_acceptS =
		"\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\3\uffff\1\15\1\uffff\1\20\5\uffff\1\27"+
		"\1\30\17\uffff\1\62\1\63\1\64\1\65\1\66\1\10\1\7\1\12\1\61\1\67\1\11\1"+
		"\14\1\13\1\17\1\16\1\22\1\21\44\uffff\1\42\3\uffff\1\46\1\47\14\uffff"+
		"\1\31\5\uffff\1\37\3\uffff\1\44\1\45\6\uffff\1\56\3\uffff\1\24\5\uffff"+
		"\1\35\1\uffff\1\40\1\uffff\1\43\4\uffff\1\54\1\55\1\57\4\uffff\1\32\1"+
		"\33\1\uffff\1\36\5\uffff\1\60\2\uffff\1\26\4\uffff\1\52\1\53\1\23\1\25"+
		"\3\uffff\1\51\1\34\1\41\1\uffff\1\50";
	static final String DFA7_specialS =
		"\u00b3\uffff}>";
	static final String[] DFA7_transitionS = {
			"\2\47\1\uffff\2\47\22\uffff\1\47\1\uffff\1\43\4\uffff\1\44\1\1\1\2\1"+
			"\3\1\4\1\5\1\6\1\7\1\10\12\45\1\11\1\12\1\13\1\14\1\15\2\uffff\1\46\1"+
			"\16\1\17\5\46\1\20\11\46\1\21\7\46\1\22\1\uffff\1\23\3\uffff\1\24\1\25"+
			"\1\26\1\46\1\27\1\30\2\46\1\31\2\46\1\32\1\33\1\34\1\35\1\36\1\46\1\37"+
			"\1\46\1\40\1\46\1\41\1\42\3\46",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\50",
			"\1\53\4\uffff\1\54\15\uffff\1\52",
			"\1\56",
			"",
			"\1\60",
			"",
			"\1\62",
			"\1\64",
			"\1\65",
			"\1\66",
			"\1\67",
			"",
			"",
			"\1\70\3\uffff\1\71",
			"\1\72",
			"\1\73",
			"\1\74\1\uffff\1\75\11\uffff\1\76",
			"\1\77",
			"\1\100",
			"\1\101",
			"\1\102",
			"\1\103",
			"\1\104\13\uffff\1\105",
			"\1\106",
			"\1\107",
			"\1\110\20\uffff\1\111",
			"\1\112",
			"\1\113",
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
			"",
			"",
			"",
			"",
			"\1\114",
			"\1\115",
			"\1\116",
			"\1\117",
			"\1\120",
			"\1\121",
			"\1\122",
			"\1\123",
			"\1\124",
			"\1\125",
			"\1\126",
			"\1\127",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\131",
			"\1\132",
			"\1\133",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\136",
			"\1\137\20\uffff\1\140",
			"\1\141",
			"\1\142",
			"\1\143",
			"\1\144\3\uffff\1\145",
			"\1\146",
			"\1\147",
			"\1\150",
			"\1\151",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\153",
			"\1\154",
			"\1\155",
			"\1\156\3\uffff\1\157",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\161",
			"\1\162",
			"",
			"\1\163",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"",
			"",
			"\1\166\3\uffff\1\167",
			"\1\170",
			"\1\171",
			"\1\172",
			"\1\173",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\175",
			"\1\176",
			"\1\177",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\u0081",
			"\1\u0082",
			"",
			"\1\u0083",
			"\1\u0084",
			"\1\u0085",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\u0087",
			"",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\u0089",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"",
			"",
			"\1\u008b",
			"\1\u008c",
			"\1\u008d",
			"\1\u008e",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\u0092",
			"\1\u0093",
			"",
			"\1\u0094",
			"\1\u0095",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\u0098",
			"",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"",
			"\1\u009a",
			"",
			"\1\u009b",
			"\1\u009c",
			"\1\u009d",
			"\1\u009e",
			"",
			"",
			"",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\u00a0",
			"\1\u00a1",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"",
			"",
			"\1\u00a3",
			"",
			"\1\u00a4",
			"\1\u00a5",
			"\1\u00a6",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"",
			"\1\u00ab",
			"\1\u00ac",
			"\1\u00ad",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"",
			"",
			"",
			"",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
			"\1\u00b1",
			"",
			"",
			"",
			"\12\46\7\uffff\32\46\6\uffff\32\46",
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
			return "1:1: Tokens : ( T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | MULTILINE_COMMENT | STRING_LITERAL | CHAR_LITERAL | INTEGER | IDENT | WS | COMMENT );";
		}
	}

}
