// $ANTLR 3.5.2 E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g 2014-04-27 13:24:16

package samples.stg;
import org.antlr.stringtemplate.*;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.stringtemplate.*;
import org.antlr.stringtemplate.language.*;
import java.util.HashMap;
@SuppressWarnings("all")
public class CMinusParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ID", "INT", "WS", "'('", "')'", 
		"'+'", "','", "';'", "'<'", "'='", "'=='", "'char'", "'for'", "'int'", 
		"'{'", "'}'"
	};
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
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators

	protected static class slist_scope {
		List locals;
		List stats;
	}
	protected Stack<slist_scope> slist_stack = new Stack<slist_scope>();


	public CMinusParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public CMinusParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected StringTemplateGroup templateLib =
	  new StringTemplateGroup("CMinusParserTemplates", AngleBracketTemplateLexer.class);

	public void setTemplateLib(StringTemplateGroup templateLib) {
	  this.templateLib = templateLib;
	}
	public StringTemplateGroup getTemplateLib() {
	  return templateLib;
	}
	/** allows convenient multi-value initialization:
	 *  "new STAttrMap().put(...).put(...)"
	 */
	@SuppressWarnings("serial")
	public static class STAttrMap extends HashMap<String, Object> {
		public STAttrMap put(String attrName, Object value) {
			super.put(attrName, value);
			return this;
		}
	}
	@Override public String[] getTokenNames() { return CMinusParser.tokenNames; }
	@Override public String getGrammarFileName() { return "E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g"; }


	protected static class program_scope {
		List globals;
		List functions;
	}
	protected Stack<program_scope> program_stack = new Stack<program_scope>();

	public static class program_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "program"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:26:1: program : ( declaration )+ -> program(globals=$program::globalsfunctions=$program::functions);
	public final program_return program() throws RecognitionException {
		program_stack.push(new program_scope());
		program_return retval = new program_return();
		retval.start = input.LT(1);


		  program_stack.peek().globals = new ArrayList();
		  program_stack.peek().functions = new ArrayList();

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:35:5: ( ( declaration )+ -> program(globals=$program::globalsfunctions=$program::functions))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:35:9: ( declaration )+
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:35:9: ( declaration )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==ID||LA1_0==15||LA1_0==17) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:35:9: declaration
					{
					pushFollow(FOLLOW_declaration_in_program58);
					declaration();
					state._fsp--;

					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			// TEMPLATE REWRITE
			// 36:9: -> program(globals=$program::globalsfunctions=$program::functions)
			{
				retval.st = templateLib.getInstanceOf("program",new STAttrMap().put("globals", program_stack.peek().globals).put("functions", program_stack.peek().functions));
			}



			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			program_stack.pop();
		}
		return retval;
	}
	// $ANTLR end "program"


	public static class declaration_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "declaration"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:39:1: declaration : ( variable |f= function );
	public final declaration_return declaration() throws RecognitionException {
		declaration_return retval = new declaration_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope f =null;
		ParserRuleReturnScope variable1 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:40:5: ( variable |f= function )
			int alt2=2;
			switch ( input.LA(1) ) {
			case 17:
				{
				int LA2_1 = input.LA(2);
				if ( (LA2_1==ID) ) {
					int LA2_4 = input.LA(3);
					if ( (LA2_4==11) ) {
						alt2=1;
					}
					else if ( (LA2_4==7) ) {
						alt2=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 2, 4, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 2, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 15:
				{
				int LA2_2 = input.LA(2);
				if ( (LA2_2==ID) ) {
					int LA2_4 = input.LA(3);
					if ( (LA2_4==11) ) {
						alt2=1;
					}
					else if ( (LA2_4==7) ) {
						alt2=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 2, 4, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 2, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ID:
				{
				int LA2_3 = input.LA(2);
				if ( (LA2_3==ID) ) {
					int LA2_4 = input.LA(3);
					if ( (LA2_4==11) ) {
						alt2=1;
					}
					else if ( (LA2_4==7) ) {
						alt2=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 2, 4, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 2, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}
			switch (alt2) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:40:9: variable
					{
					pushFollow(FOLLOW_variable_in_declaration99);
					variable1=variable();
					state._fsp--;

					program_stack.peek().globals.add((variable1!=null?((StringTemplate)variable1.getTemplate()):null));
					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:41:9: f= function
					{
					pushFollow(FOLLOW_function_in_declaration115);
					f=function();
					state._fsp--;

					program_stack.peek().functions.add((f!=null?((StringTemplate)f.getTemplate()):null));
					}
					break;

			}
			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "declaration"


	public static class variable_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "variable"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:47:1: variable : type declarator ';' -> {$function.size()>0 && $function::name==null}? globalVariable(type=$type.stname=$declarator.st) -> variable(type=$type.stname=$declarator.st);
	public final variable_return variable() throws RecognitionException {
		variable_return retval = new variable_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope type2 =null;
		ParserRuleReturnScope declarator3 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:48:5: ( type declarator ';' -> {$function.size()>0 && $function::name==null}? globalVariable(type=$type.stname=$declarator.st) -> variable(type=$type.stname=$declarator.st))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:48:9: type declarator ';'
			{
			pushFollow(FOLLOW_type_in_variable139);
			type2=type();
			state._fsp--;

			pushFollow(FOLLOW_declarator_in_variable141);
			declarator3=declarator();
			state._fsp--;

			match(input,11,FOLLOW_11_in_variable143); 
			// TEMPLATE REWRITE
			// 49:9: -> {$function.size()>0 && $function::name==null}? globalVariable(type=$type.stname=$declarator.st)
			if (function_stack.size()>0 && function_stack.peek().name==null) {
				retval.st = templateLib.getInstanceOf("globalVariable",new STAttrMap().put("type", (type2!=null?((StringTemplate)type2.getTemplate()):null)).put("name", (declarator3!=null?((StringTemplate)declarator3.getTemplate()):null)));
			}

			else // 51:9: -> variable(type=$type.stname=$declarator.st)
			{
				retval.st = templateLib.getInstanceOf("variable",new STAttrMap().put("type", (type2!=null?((StringTemplate)type2.getTemplate()):null)).put("name", (declarator3!=null?((StringTemplate)declarator3.getTemplate()):null)));
			}



			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "variable"


	public static class declarator_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "declarator"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:54:1: declarator : ID -> {new StringTemplate($ID.text)};
	public final declarator_return declarator() throws RecognitionException {
		declarator_return retval = new declarator_return();
		retval.start = input.LT(1);

		Token ID4=null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:55:5: ( ID -> {new StringTemplate($ID.text)})
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:55:9: ID
			{
			ID4=(Token)match(input,ID,FOLLOW_ID_in_declarator217); 
			// TEMPLATE REWRITE
			// 55:12: -> {new StringTemplate($ID.text)}
			{
				retval.st = new StringTemplate((ID4!=null?ID4.getText():null));
			}



			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "declarator"


	protected static class function_scope {
		String name;
	}
	protected Stack<function_scope> function_stack = new Stack<function_scope>();

	public static class function_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "function"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:58:1: function : type ID '(' (p+= formalParameter ( ',' p+= formalParameter )* )? ')' block -> function(type=$type.stname=$function::namelocals=$slist::localsstats=$slist::statsargs=$p);
	public final function_return function() throws RecognitionException {
		slist_stack.push(new slist_scope());
		function_stack.push(new function_scope());
		function_return retval = new function_return();
		retval.start = input.LT(1);

		Token ID5=null;
		List<Object> list_p=null;
		ParserRuleReturnScope type6 =null;
		RuleReturnScope p = null;

		  slist_stack.peek().locals = new ArrayList();
		  slist_stack.peek().stats = new ArrayList();

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:67:5: ( type ID '(' (p+= formalParameter ( ',' p+= formalParameter )* )? ')' block -> function(type=$type.stname=$function::namelocals=$slist::localsstats=$slist::statsargs=$p))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:67:9: type ID '(' (p+= formalParameter ( ',' p+= formalParameter )* )? ')' block
			{
			pushFollow(FOLLOW_type_in_function254);
			type6=type();
			state._fsp--;

			ID5=(Token)match(input,ID,FOLLOW_ID_in_function256); 
			function_stack.peek().name =(ID5!=null?ID5.getText():null);
			match(input,7,FOLLOW_7_in_function268); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:68:13: (p+= formalParameter ( ',' p+= formalParameter )* )?
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==ID||LA4_0==15||LA4_0==17) ) {
				alt4=1;
			}
			switch (alt4) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:68:15: p+= formalParameter ( ',' p+= formalParameter )*
					{
					pushFollow(FOLLOW_formalParameter_in_function274);
					p=formalParameter();
					state._fsp--;

					if (list_p==null) list_p=new ArrayList<Object>();
					list_p.add(p.getTemplate());
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:68:34: ( ',' p+= formalParameter )*
					loop3:
					while (true) {
						int alt3=2;
						int LA3_0 = input.LA(1);
						if ( (LA3_0==10) ) {
							alt3=1;
						}

						switch (alt3) {
						case 1 :
							// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:68:36: ',' p+= formalParameter
							{
							match(input,10,FOLLOW_10_in_function278); 
							pushFollow(FOLLOW_formalParameter_in_function282);
							p=formalParameter();
							state._fsp--;

							if (list_p==null) list_p=new ArrayList<Object>();
							list_p.add(p.getTemplate());
							}
							break;

						default :
							break loop3;
						}
					}

					}
					break;

			}

			match(input,8,FOLLOW_8_in_function290); 
			pushFollow(FOLLOW_block_in_function300);
			block();
			state._fsp--;

			// TEMPLATE REWRITE
			// 70:9: -> function(type=$type.stname=$function::namelocals=$slist::localsstats=$slist::statsargs=$p)
			{
				retval.st = templateLib.getInstanceOf("function",new STAttrMap().put("type", (type6!=null?((StringTemplate)type6.getTemplate()):null)).put("name", function_stack.peek().name).put("locals", slist_stack.peek().locals).put("stats", slist_stack.peek().stats).put("args", list_p));
			}



			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			slist_stack.pop();
			function_stack.pop();
		}
		return retval;
	}
	// $ANTLR end "function"


	public static class formalParameter_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "formalParameter"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:76:1: formalParameter : type declarator -> parameter(type=$type.stname=$declarator.st);
	public final formalParameter_return formalParameter() throws RecognitionException {
		formalParameter_return retval = new formalParameter_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope type7 =null;
		ParserRuleReturnScope declarator8 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:77:5: ( type declarator -> parameter(type=$type.stname=$declarator.st))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:77:9: type declarator
			{
			pushFollow(FOLLOW_type_in_formalParameter416);
			type7=type();
			state._fsp--;

			pushFollow(FOLLOW_declarator_in_formalParameter418);
			declarator8=declarator();
			state._fsp--;

			// TEMPLATE REWRITE
			// 78:9: -> parameter(type=$type.stname=$declarator.st)
			{
				retval.st = templateLib.getInstanceOf("parameter",new STAttrMap().put("type", (type7!=null?((StringTemplate)type7.getTemplate()):null)).put("name", (declarator8!=null?((StringTemplate)declarator8.getTemplate()):null)));
			}



			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "formalParameter"


	public static class type_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "type"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:81:1: type : ( 'int' -> type_int(| 'char' -> type_char(| ID -> type_user_object(name=$ID.text));
	public final type_return type() throws RecognitionException {
		type_return retval = new type_return();
		retval.start = input.LT(1);

		Token ID9=null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:82:5: ( 'int' -> type_int(| 'char' -> type_char(| ID -> type_user_object(name=$ID.text))
			int alt5=3;
			switch ( input.LA(1) ) {
			case 17:
				{
				alt5=1;
				}
				break;
			case 15:
				{
				alt5=2;
				}
				break;
			case ID:
				{
				alt5=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}
			switch (alt5) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:82:9: 'int'
					{
					match(input,17,FOLLOW_17_in_type459); 
					// TEMPLATE REWRITE
					// 82:16: -> type_int(
					{
						retval.st = templateLib.getInstanceOf("type_int");
					}



					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:83:9: 'char'
					{
					match(input,15,FOLLOW_15_in_type476); 
					// TEMPLATE REWRITE
					// 83:16: -> type_char(
					{
						retval.st = templateLib.getInstanceOf("type_char");
					}



					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:84:9: ID
					{
					ID9=(Token)match(input,ID,FOLLOW_ID_in_type492); 
					// TEMPLATE REWRITE
					// 84:16: -> type_user_object(name=$ID.text)
					{
						retval.st = templateLib.getInstanceOf("type_user_object",new STAttrMap().put("name", (ID9!=null?ID9.getText():null)));
					}



					}
					break;

			}
			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "type"


	public static class block_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "block"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:87:1: block : '{' ( variable )* ( stat )* '}' ;
	public final block_return block() throws RecognitionException {
		block_return retval = new block_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope variable10 =null;
		ParserRuleReturnScope stat11 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:88:5: ( '{' ( variable )* ( stat )* '}' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:88:8: '{' ( variable )* ( stat )* '}'
			{
			match(input,18,FOLLOW_18_in_block523); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:89:8: ( variable )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( (LA6_0==ID) ) {
					int LA6_2 = input.LA(2);
					if ( (LA6_2==ID) ) {
						alt6=1;
					}

				}
				else if ( (LA6_0==15||LA6_0==17) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:89:10: variable
					{
					pushFollow(FOLLOW_variable_in_block534);
					variable10=variable();
					state._fsp--;

					slist_stack.peek().locals.add((variable10!=null?((StringTemplate)variable10.getTemplate()):null));
					}
					break;

				default :
					break loop6;
				}
			}

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:90:8: ( stat )*
			loop7:
			while (true) {
				int alt7=2;
				int LA7_0 = input.LA(1);
				if ( ((LA7_0 >= ID && LA7_0 <= INT)||LA7_0==7||LA7_0==11||LA7_0==16||LA7_0==18) ) {
					alt7=1;
				}

				switch (alt7) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:90:10: stat
					{
					pushFollow(FOLLOW_stat_in_block550);
					stat11=stat();
					state._fsp--;

					slist_stack.peek().stats.add((stat11!=null?((StringTemplate)stat11.getTemplate()):null));
					}
					break;

				default :
					break loop7;
				}
			}

			match(input,19,FOLLOW_19_in_block563); 
			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "block"


	public static class stat_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "stat"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:94:1: stat : ( forStat -> {$forStat.st}| expr ';' -> statement(expr=$expr.st)| block -> statementList(locals=$slist::localsstats=$slist::stats)| assignStat ';' -> {$assignStat.st}| ';' -> {new StringTemplate(\";\")});
	public final stat_return stat() throws RecognitionException {
		slist_stack.push(new slist_scope());

		stat_return retval = new stat_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope forStat12 =null;
		ParserRuleReturnScope expr13 =null;
		ParserRuleReturnScope assignStat14 =null;


		  slist_stack.peek().locals = new ArrayList();
		  slist_stack.peek().stats = new ArrayList();

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:100:5: ( forStat -> {$forStat.st}| expr ';' -> statement(expr=$expr.st)| block -> statementList(locals=$slist::localsstats=$slist::stats)| assignStat ';' -> {$assignStat.st}| ';' -> {new StringTemplate(\";\")})
			int alt8=5;
			switch ( input.LA(1) ) {
			case 16:
				{
				alt8=1;
				}
				break;
			case ID:
				{
				int LA8_2 = input.LA(2);
				if ( (LA8_2==13) ) {
					alt8=4;
				}
				else if ( (LA8_2==9||(LA8_2 >= 11 && LA8_2 <= 12)||LA8_2==14) ) {
					alt8=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 8, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case INT:
			case 7:
				{
				alt8=2;
				}
				break;
			case 18:
				{
				alt8=3;
				}
				break;
			case 11:
				{
				alt8=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}
			switch (alt8) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:100:7: forStat
					{
					pushFollow(FOLLOW_forStat_in_stat590);
					forStat12=forStat();
					state._fsp--;

					// TEMPLATE REWRITE
					// 100:15: -> {$forStat.st}
					{
						retval.st = (forStat12!=null?((StringTemplate)forStat12.getTemplate()):null);
					}



					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:101:7: expr ';'
					{
					pushFollow(FOLLOW_expr_in_stat602);
					expr13=expr();
					state._fsp--;

					match(input,11,FOLLOW_11_in_stat604); 
					// TEMPLATE REWRITE
					// 101:16: -> statement(expr=$expr.st)
					{
						retval.st = templateLib.getInstanceOf("statement",new STAttrMap().put("expr", (expr13!=null?((StringTemplate)expr13.getTemplate()):null)));
					}



					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:102:7: block
					{
					pushFollow(FOLLOW_block_in_stat621);
					block();
					state._fsp--;

					// TEMPLATE REWRITE
					// 102:13: -> statementList(locals=$slist::localsstats=$slist::stats)
					{
						retval.st = templateLib.getInstanceOf("statementList",new STAttrMap().put("locals", slist_stack.peek().locals).put("stats", slist_stack.peek().stats));
					}



					}
					break;
				case 4 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:103:7: assignStat ';'
					{
					pushFollow(FOLLOW_assignStat_in_stat643);
					assignStat14=assignStat();
					state._fsp--;

					match(input,11,FOLLOW_11_in_stat645); 
					// TEMPLATE REWRITE
					// 103:22: -> {$assignStat.st}
					{
						retval.st = (assignStat14!=null?((StringTemplate)assignStat14.getTemplate()):null);
					}



					}
					break;
				case 5 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:104:7: ';'
					{
					match(input,11,FOLLOW_11_in_stat657); 
					// TEMPLATE REWRITE
					// 104:11: -> {new StringTemplate(\";\")}
					{
						retval.st = new StringTemplate(";");
					}



					}
					break;

			}
			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			slist_stack.pop();

		}
		return retval;
	}
	// $ANTLR end "stat"


	public static class forStat_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "forStat"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:107:1: forStat : 'for' '(' e1= assignStat ';' e2= expr ';' e3= assignStat ')' block -> forLoop(e1=$e1.ste2=$e2.ste3=$e3.stlocals=$slist::localsstats=$slist::stats);
	public final forStat_return forStat() throws RecognitionException {
		slist_stack.push(new slist_scope());

		forStat_return retval = new forStat_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope e1 =null;
		ParserRuleReturnScope e2 =null;
		ParserRuleReturnScope e3 =null;


		  slist_stack.peek().locals = new ArrayList();
		  slist_stack.peek().stats = new ArrayList();

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:113:5: ( 'for' '(' e1= assignStat ';' e2= expr ';' e3= assignStat ')' block -> forLoop(e1=$e1.ste2=$e2.ste3=$e3.stlocals=$slist::localsstats=$slist::stats))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:113:9: 'for' '(' e1= assignStat ';' e2= expr ';' e3= assignStat ')' block
			{
			match(input,16,FOLLOW_16_in_forStat690); 
			match(input,7,FOLLOW_7_in_forStat692); 
			pushFollow(FOLLOW_assignStat_in_forStat696);
			e1=assignStat();
			state._fsp--;

			match(input,11,FOLLOW_11_in_forStat698); 
			pushFollow(FOLLOW_expr_in_forStat702);
			e2=expr();
			state._fsp--;

			match(input,11,FOLLOW_11_in_forStat704); 
			pushFollow(FOLLOW_assignStat_in_forStat708);
			e3=assignStat();
			state._fsp--;

			match(input,8,FOLLOW_8_in_forStat710); 
			pushFollow(FOLLOW_block_in_forStat712);
			block();
			state._fsp--;

			// TEMPLATE REWRITE
			// 114:9: -> forLoop(e1=$e1.ste2=$e2.ste3=$e3.stlocals=$slist::localsstats=$slist::stats)
			{
				retval.st = templateLib.getInstanceOf("forLoop",new STAttrMap().put("e1", (e1!=null?((StringTemplate)e1.getTemplate()):null)).put("e2", (e2!=null?((StringTemplate)e2.getTemplate()):null)).put("e3", (e3!=null?((StringTemplate)e3.getTemplate()):null)).put("locals", slist_stack.peek().locals).put("stats", slist_stack.peek().stats));
			}



			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			slist_stack.pop();

		}
		return retval;
	}
	// $ANTLR end "forStat"


	public static class assignStat_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "assignStat"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:118:1: assignStat : ID '=' expr -> assign(lhs=$ID.textrhs=$expr.st);
	public final assignStat_return assignStat() throws RecognitionException {
		assignStat_return retval = new assignStat_return();
		retval.start = input.LT(1);

		Token ID15=null;
		ParserRuleReturnScope expr16 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:119:5: ( ID '=' expr -> assign(lhs=$ID.textrhs=$expr.st))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:119:9: ID '=' expr
			{
			ID15=(Token)match(input,ID,FOLLOW_ID_in_assignStat785); 
			match(input,13,FOLLOW_13_in_assignStat787); 
			pushFollow(FOLLOW_expr_in_assignStat789);
			expr16=expr();
			state._fsp--;

			// TEMPLATE REWRITE
			// 119:21: -> assign(lhs=$ID.textrhs=$expr.st)
			{
				retval.st = templateLib.getInstanceOf("assign",new STAttrMap().put("lhs", (ID15!=null?ID15.getText():null)).put("rhs", (expr16!=null?((StringTemplate)expr16.getTemplate()):null)));
			}



			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "assignStat"


	public static class expr_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "expr"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:122:1: expr : condExpr -> {$condExpr.st};
	public final expr_return expr() throws RecognitionException {
		expr_return retval = new expr_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope condExpr17 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:122:5: ( condExpr -> {$condExpr.st})
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:122:9: condExpr
			{
			pushFollow(FOLLOW_condExpr_in_expr817);
			condExpr17=condExpr();
			state._fsp--;

			// TEMPLATE REWRITE
			// 122:18: -> {$condExpr.st}
			{
				retval.st = (condExpr17!=null?((StringTemplate)condExpr17.getTemplate()):null);
			}



			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "expr"


	public static class condExpr_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "condExpr"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:125:1: condExpr : a= aexpr ( ( '==' b= aexpr -> equals(left=$a.stright=$b.st)| '<' b= aexpr -> lessThan(left=$a.stright=$b.st)) | -> {$a.st}) ;
	public final condExpr_return condExpr() throws RecognitionException {
		condExpr_return retval = new condExpr_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope a =null;
		ParserRuleReturnScope b =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:126:5: (a= aexpr ( ( '==' b= aexpr -> equals(left=$a.stright=$b.st)| '<' b= aexpr -> lessThan(left=$a.stright=$b.st)) | -> {$a.st}) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:126:9: a= aexpr ( ( '==' b= aexpr -> equals(left=$a.stright=$b.st)| '<' b= aexpr -> lessThan(left=$a.stright=$b.st)) | -> {$a.st})
			{
			pushFollow(FOLLOW_aexpr_in_condExpr842);
			a=aexpr();
			state._fsp--;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:127:9: ( ( '==' b= aexpr -> equals(left=$a.stright=$b.st)| '<' b= aexpr -> lessThan(left=$a.stright=$b.st)) | -> {$a.st})
			int alt10=2;
			int LA10_0 = input.LA(1);
			if ( (LA10_0==12||LA10_0==14) ) {
				alt10=1;
			}
			else if ( (LA10_0==8||LA10_0==11) ) {
				alt10=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}

			switch (alt10) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:127:13: ( '==' b= aexpr -> equals(left=$a.stright=$b.st)| '<' b= aexpr -> lessThan(left=$a.stright=$b.st))
					{
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:127:13: ( '==' b= aexpr -> equals(left=$a.stright=$b.st)| '<' b= aexpr -> lessThan(left=$a.stright=$b.st))
					int alt9=2;
					int LA9_0 = input.LA(1);
					if ( (LA9_0==14) ) {
						alt9=1;
					}
					else if ( (LA9_0==12) ) {
						alt9=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 9, 0, input);
						throw nvae;
					}

					switch (alt9) {
						case 1 :
							// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:127:16: '==' b= aexpr
							{
							match(input,14,FOLLOW_14_in_condExpr859); 
							pushFollow(FOLLOW_aexpr_in_condExpr863);
							b=aexpr();
							state._fsp--;

							// TEMPLATE REWRITE
							// 127:29: -> equals(left=$a.stright=$b.st)
							{
								retval.st = templateLib.getInstanceOf("equals",new STAttrMap().put("left", (a!=null?((StringTemplate)a.getTemplate()):null)).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
							}



							}
							break;
						case 2 :
							// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:128:16: '<' b= aexpr
							{
							match(input,12,FOLLOW_12_in_condExpr893); 
							pushFollow(FOLLOW_aexpr_in_condExpr897);
							b=aexpr();
							state._fsp--;

							// TEMPLATE REWRITE
							// 128:30: -> lessThan(left=$a.stright=$b.st)
							{
								retval.st = templateLib.getInstanceOf("lessThan",new STAttrMap().put("left", (a!=null?((StringTemplate)a.getTemplate()):null)).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
							}



							}
							break;

					}

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:130:13: 
					{
					// TEMPLATE REWRITE
					// 130:13: -> {$a.st}
					{
						retval.st = (a!=null?((StringTemplate)a.getTemplate()):null);
					}



					}
					break;

			}

			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "condExpr"


	public static class aexpr_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "aexpr"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:134:1: aexpr : (a= atom -> {$a.st}) ( '+' b= atom -> add(left=$aexpr.stright=$b.st))* ;
	public final aexpr_return aexpr() throws RecognitionException {
		aexpr_return retval = new aexpr_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope a =null;
		ParserRuleReturnScope b =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:135:5: ( (a= atom -> {$a.st}) ( '+' b= atom -> add(left=$aexpr.stright=$b.st))* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:135:9: (a= atom -> {$a.st}) ( '+' b= atom -> add(left=$aexpr.stright=$b.st))*
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:135:9: (a= atom -> {$a.st})
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:135:10: a= atom
			{
			pushFollow(FOLLOW_atom_in_aexpr975);
			a=atom();
			state._fsp--;

			// TEMPLATE REWRITE
			// 135:17: -> {$a.st}
			{
				retval.st = (a!=null?((StringTemplate)a.getTemplate()):null);
			}



			}

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:136:9: ( '+' b= atom -> add(left=$aexpr.stright=$b.st))*
			loop11:
			while (true) {
				int alt11=2;
				int LA11_0 = input.LA(1);
				if ( (LA11_0==9) ) {
					alt11=1;
				}

				switch (alt11) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:136:11: '+' b= atom
					{
					match(input,9,FOLLOW_9_in_aexpr992); 
					pushFollow(FOLLOW_atom_in_aexpr996);
					b=atom();
					state._fsp--;

					// TEMPLATE REWRITE
					// 136:22: -> add(left=$aexpr.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("add",new STAttrMap().put("left", retval.st).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;

				default :
					break loop11;
				}
			}

			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "aexpr"


	public static class atom_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "atom"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:139:1: atom : ( ID -> refVar(id=$ID.text)| INT -> iconst(value=$INT.text)| '(' expr ')' -> {$expr.st});
	public final atom_return atom() throws RecognitionException {
		atom_return retval = new atom_return();
		retval.start = input.LT(1);

		Token ID18=null;
		Token INT19=null;
		ParserRuleReturnScope expr20 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:140:5: ( ID -> refVar(id=$ID.text)| INT -> iconst(value=$INT.text)| '(' expr ')' -> {$expr.st})
			int alt12=3;
			switch ( input.LA(1) ) {
			case ID:
				{
				alt12=1;
				}
				break;
			case INT:
				{
				alt12=2;
				}
				break;
			case 7:
				{
				alt12=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 12, 0, input);
				throw nvae;
			}
			switch (alt12) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:140:7: ID
					{
					ID18=(Token)match(input,ID,FOLLOW_ID_in_atom1030); 
					// TEMPLATE REWRITE
					// 140:10: -> refVar(id=$ID.text)
					{
						retval.st = templateLib.getInstanceOf("refVar",new STAttrMap().put("id", (ID18!=null?ID18.getText():null)));
					}



					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:141:7: INT
					{
					INT19=(Token)match(input,INT,FOLLOW_INT_in_atom1047); 
					// TEMPLATE REWRITE
					// 141:11: -> iconst(value=$INT.text)
					{
						retval.st = templateLib.getInstanceOf("iconst",new STAttrMap().put("value", (INT19!=null?INT19.getText():null)));
					}



					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\stg\\CMinus.g:142:7: '(' expr ')'
					{
					match(input,7,FOLLOW_7_in_atom1064); 
					pushFollow(FOLLOW_expr_in_atom1066);
					expr20=expr();
					state._fsp--;

					match(input,8,FOLLOW_8_in_atom1068); 
					// TEMPLATE REWRITE
					// 142:20: -> {$expr.st}
					{
						retval.st = (expr20!=null?((StringTemplate)expr20.getTemplate()):null);
					}



					}
					break;

			}
			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "atom"

	// Delegated rules



	public static final BitSet FOLLOW_declaration_in_program58 = new BitSet(new long[]{0x0000000000028012L});
	public static final BitSet FOLLOW_variable_in_declaration99 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_in_declaration115 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_variable139 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_declarator_in_variable141 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_11_in_variable143 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_declarator217 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_function254 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_ID_in_function256 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_7_in_function268 = new BitSet(new long[]{0x0000000000028110L});
	public static final BitSet FOLLOW_formalParameter_in_function274 = new BitSet(new long[]{0x0000000000000500L});
	public static final BitSet FOLLOW_10_in_function278 = new BitSet(new long[]{0x0000000000028010L});
	public static final BitSet FOLLOW_formalParameter_in_function282 = new BitSet(new long[]{0x0000000000000500L});
	public static final BitSet FOLLOW_8_in_function290 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_block_in_function300 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_formalParameter416 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_declarator_in_formalParameter418 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_17_in_type459 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_15_in_type476 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_type492 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_18_in_block523 = new BitSet(new long[]{0x00000000000F88B0L});
	public static final BitSet FOLLOW_variable_in_block534 = new BitSet(new long[]{0x00000000000F88B0L});
	public static final BitSet FOLLOW_stat_in_block550 = new BitSet(new long[]{0x00000000000D08B0L});
	public static final BitSet FOLLOW_19_in_block563 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_forStat_in_stat590 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expr_in_stat602 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_11_in_stat604 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_stat621 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_assignStat_in_stat643 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_11_in_stat645 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_11_in_stat657 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_16_in_forStat690 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_7_in_forStat692 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_assignStat_in_forStat696 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_11_in_forStat698 = new BitSet(new long[]{0x00000000000000B0L});
	public static final BitSet FOLLOW_expr_in_forStat702 = new BitSet(new long[]{0x0000000000000800L});
	public static final BitSet FOLLOW_11_in_forStat704 = new BitSet(new long[]{0x0000000000000010L});
	public static final BitSet FOLLOW_assignStat_in_forStat708 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_8_in_forStat710 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_block_in_forStat712 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_in_assignStat785 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_13_in_assignStat787 = new BitSet(new long[]{0x00000000000000B0L});
	public static final BitSet FOLLOW_expr_in_assignStat789 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_condExpr_in_expr817 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_aexpr_in_condExpr842 = new BitSet(new long[]{0x0000000000005002L});
	public static final BitSet FOLLOW_14_in_condExpr859 = new BitSet(new long[]{0x00000000000000B0L});
	public static final BitSet FOLLOW_aexpr_in_condExpr863 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_12_in_condExpr893 = new BitSet(new long[]{0x00000000000000B0L});
	public static final BitSet FOLLOW_aexpr_in_condExpr897 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_atom_in_aexpr975 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_9_in_aexpr992 = new BitSet(new long[]{0x00000000000000B0L});
	public static final BitSet FOLLOW_atom_in_aexpr996 = new BitSet(new long[]{0x0000000000000202L});
	public static final BitSet FOLLOW_ID_in_atom1030 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INT_in_atom1047 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_7_in_atom1064 = new BitSet(new long[]{0x00000000000000B0L});
	public static final BitSet FOLLOW_expr_in_atom1066 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_8_in_atom1068 = new BitSet(new long[]{0x0000000000000002L});
}
