// $ANTLR 3.5.2 E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g 2014-05-02 09:10:52


  /*  (!!!) Do not update this file manually ,
  *         It was auto generated from the Serpent.g
  *         grammar file.
  */
  package org.ethereum.serpent;
  import org.ethereum.util.Utils;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.stringtemplate.*;
import org.antlr.stringtemplate.language.*;
import java.util.HashMap;
/*******************************************************************************
 * Ethereum high level language grammar definition
 *******************************************************************************/
@SuppressWarnings("all")
public class SerpentParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "COMMENT", "DIGIT", "HEX_DIGIT", 
		"HEX_NUMBER", "IDENT", "INTEGER", "LETTER", "WS", "'!'", "'#%'", "'#/'", 
		"'%'", "')'", "'*'", "'+'", "'-'", "'/'", "':'", "'<'", "'<='", "'='", 
		"'=='", "'>'", "'>='", "']'", "'^'", "'contract.storage['", "'else'", 
		"'if'", "'msg.data['", "'msg.datasize'", "'msg.sender'", "'return('"
	};
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
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public SerpentParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public SerpentParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected StringTemplateGroup templateLib =
	  new StringTemplateGroup("SerpentParserTemplates", AngleBracketTemplateLexer.class);

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
	@Override public String[] getTokenNames() { return SerpentParser.tokenNames; }
	@Override public String getGrammarFileName() { return "E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g"; }


	    private ArrayList<String> globals = new ArrayList<String>();
	    private int labelIndex = 0;


	public static class program_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "program"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:39:1: program : gen_body -> concat(left=conVarsright=$gen_body.st);
	public final program_return program() throws RecognitionException {
		program_return retval = new program_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope gen_body1 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:40:2: ( gen_body -> concat(left=conVarsright=$gen_body.st))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:40:5: gen_body
			{
			pushFollow(FOLLOW_gen_body_in_program60);
			gen_body1=gen_body();
			state._fsp--;


			            String conVars = "";

			            if (globals.size() > 0){

			                conVars = "0 " + (globals.size() * 32 - 1) + " MSTORE8";
			            }
			       
			// TEMPLATE REWRITE
			// 49:8: -> concat(left=conVarsright=$gen_body.st)
			{
				retval.st = templateLib.getInstanceOf("concat",new STAttrMap().put("left", conVars).put("right", (gen_body1!=null?((StringTemplate)gen_body1.getTemplate()):null)));
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
	// $ANTLR end "program"


	public static class test_1_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "test_1"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:52:1: test_1 : ( set_var -> concat(left=$test_1.stright=$set_var.st)| get_var -> concat(left=$test_1.stright=$get_var.st))* ;
	public final test_1_return test_1() throws RecognitionException {
		test_1_return retval = new test_1_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope set_var2 =null;
		ParserRuleReturnScope get_var3 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:53:2: ( ( set_var -> concat(left=$test_1.stright=$set_var.st)| get_var -> concat(left=$test_1.stright=$get_var.st))* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:54:8: ( set_var -> concat(left=$test_1.stright=$set_var.st)| get_var -> concat(left=$test_1.stright=$get_var.st))*
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:54:8: ( set_var -> concat(left=$test_1.stright=$set_var.st)| get_var -> concat(left=$test_1.stright=$get_var.st))*
			loop1:
			while (true) {
				int alt1=3;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==IDENT) ) {
					int LA1_2 = input.LA(2);
					if ( (LA1_2==24) ) {
						alt1=1;
					}
					else if ( (LA1_2==EOF||LA1_2==IDENT) ) {
						alt1=2;
					}

				}

				switch (alt1) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:55:11: set_var
					{
					pushFollow(FOLLOW_set_var_in_test_1120);
					set_var2=set_var();
					state._fsp--;

					// TEMPLATE REWRITE
					// 55:19: -> concat(left=$test_1.stright=$set_var.st)
					{
						retval.st = templateLib.getInstanceOf("concat",new STAttrMap().put("left", retval.st).put("right", (set_var2!=null?((StringTemplate)set_var2.getTemplate()):null)));
					}



					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:56:11: get_var
					{
					pushFollow(FOLLOW_get_var_in_test_1146);
					get_var3=get_var();
					state._fsp--;

					// TEMPLATE REWRITE
					// 56:19: -> concat(left=$test_1.stright=$get_var.st)
					{
						retval.st = templateLib.getInstanceOf("concat",new STAttrMap().put("left", retval.st).put("right", (get_var3!=null?((StringTemplate)get_var3.getTemplate()):null)));
					}



					}
					break;

				default :
					break loop1;
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
	// $ANTLR end "test_1"


	public static class gen_body_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "gen_body"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:75:1: gen_body : ( set_var -> concat(left=$gen_body.stright=$set_var.st)| storage_save -> concat(left=$gen_body.stright=$storage_save.st)| return_stmt -> concat(left=$gen_body.stright=$return_stmt.st))* ( if_else_stmt -> concat(left=$gen_body.stright=$if_else_stmt.st))? ;
	public final gen_body_return gen_body() throws RecognitionException {
		gen_body_return retval = new gen_body_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope set_var4 =null;
		ParserRuleReturnScope storage_save5 =null;
		ParserRuleReturnScope return_stmt6 =null;
		ParserRuleReturnScope if_else_stmt7 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:76:5: ( ( set_var -> concat(left=$gen_body.stright=$set_var.st)| storage_save -> concat(left=$gen_body.stright=$storage_save.st)| return_stmt -> concat(left=$gen_body.stright=$return_stmt.st))* ( if_else_stmt -> concat(left=$gen_body.stright=$if_else_stmt.st))? )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:77:8: ( set_var -> concat(left=$gen_body.stright=$set_var.st)| storage_save -> concat(left=$gen_body.stright=$storage_save.st)| return_stmt -> concat(left=$gen_body.stright=$return_stmt.st))* ( if_else_stmt -> concat(left=$gen_body.stright=$if_else_stmt.st))?
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:77:8: ( set_var -> concat(left=$gen_body.stright=$set_var.st)| storage_save -> concat(left=$gen_body.stright=$storage_save.st)| return_stmt -> concat(left=$gen_body.stright=$return_stmt.st))*
			loop2:
			while (true) {
				int alt2=4;
				switch ( input.LA(1) ) {
				case IDENT:
					{
					alt2=1;
					}
					break;
				case 30:
					{
					alt2=2;
					}
					break;
				case 36:
					{
					alt2=3;
					}
					break;
				}
				switch (alt2) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:78:9: set_var
					{
					pushFollow(FOLLOW_set_var_in_gen_body201);
					set_var4=set_var();
					state._fsp--;

					// TEMPLATE REWRITE
					// 78:17: -> concat(left=$gen_body.stright=$set_var.st)
					{
						retval.st = templateLib.getInstanceOf("concat",new STAttrMap().put("left", retval.st).put("right", (set_var4!=null?((StringTemplate)set_var4.getTemplate()):null)));
					}



					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:79:11: storage_save
					{
					pushFollow(FOLLOW_storage_save_in_gen_body227);
					storage_save5=storage_save();
					state._fsp--;

					// TEMPLATE REWRITE
					// 79:24: -> concat(left=$gen_body.stright=$storage_save.st)
					{
						retval.st = templateLib.getInstanceOf("concat",new STAttrMap().put("left", retval.st).put("right", (storage_save5!=null?((StringTemplate)storage_save5.getTemplate()):null)));
					}



					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:80:11: return_stmt
					{
					pushFollow(FOLLOW_return_stmt_in_gen_body253);
					return_stmt6=return_stmt();
					state._fsp--;

					// TEMPLATE REWRITE
					// 80:23: -> concat(left=$gen_body.stright=$return_stmt.st)
					{
						retval.st = templateLib.getInstanceOf("concat",new STAttrMap().put("left", retval.st).put("right", (return_stmt6!=null?((StringTemplate)return_stmt6.getTemplate()):null)));
					}



					}
					break;

				default :
					break loop2;
				}
			}

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:81:11: ( if_else_stmt -> concat(left=$gen_body.stright=$if_else_stmt.st))?
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==32) ) {
				alt3=1;
			}
			switch (alt3) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:81:13: if_else_stmt
					{
					pushFollow(FOLLOW_if_else_stmt_in_gen_body281);
					if_else_stmt7=if_else_stmt();
					state._fsp--;

					// TEMPLATE REWRITE
					// 81:26: -> concat(left=$gen_body.stright=$if_else_stmt.st)
					{
						retval.st = templateLib.getInstanceOf("concat",new STAttrMap().put("left", retval.st).put("right", (if_else_stmt7!=null?((StringTemplate)if_else_stmt7.getTemplate()):null)));
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
	// $ANTLR end "gen_body"


	public static class if_stmt_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "if_stmt"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:90:1: if_stmt : 'if' unr_expr ':' gen_body -> ifStmt(cond=$unr_expr.stbody=$gen_body.stindex=labelIndex++);
	public final if_stmt_return if_stmt() throws RecognitionException {
		if_stmt_return retval = new if_stmt_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope unr_expr8 =null;
		ParserRuleReturnScope gen_body9 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:91:5: ( 'if' unr_expr ':' gen_body -> ifStmt(cond=$unr_expr.stbody=$gen_body.stindex=labelIndex++))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:92:9: 'if' unr_expr ':' gen_body
			{
			match(input,32,FOLLOW_32_in_if_stmt328); 
			pushFollow(FOLLOW_unr_expr_in_if_stmt330);
			unr_expr8=unr_expr();
			state._fsp--;

			match(input,21,FOLLOW_21_in_if_stmt332); 
			pushFollow(FOLLOW_gen_body_in_if_stmt334);
			gen_body9=gen_body();
			state._fsp--;

			// TEMPLATE REWRITE
			// 94:65: -> ifStmt(cond=$unr_expr.stbody=$gen_body.stindex=labelIndex++)
			{
				retval.st = templateLib.getInstanceOf("ifStmt",new STAttrMap().put("cond", (unr_expr8!=null?((StringTemplate)unr_expr8.getTemplate()):null)).put("body", (gen_body9!=null?((StringTemplate)gen_body9.getTemplate()):null)).put("index", labelIndex++));
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
	// $ANTLR end "if_stmt"


	public static class if_else_stmt_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "if_else_stmt"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:102:1: if_else_stmt : 'if' unr_expr ':' if_body= gen_body 'else' ':' else_body= gen_body -> ifElseStmt(cond=$unr_expr.stif_body=$if_body.stelse_body=$else_body.stif_index=labelIndex++else_index=labelIndex++);
	public final if_else_stmt_return if_else_stmt() throws RecognitionException {
		if_else_stmt_return retval = new if_else_stmt_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope if_body =null;
		ParserRuleReturnScope else_body =null;
		ParserRuleReturnScope unr_expr10 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:103:5: ( 'if' unr_expr ':' if_body= gen_body 'else' ':' else_body= gen_body -> ifElseStmt(cond=$unr_expr.stif_body=$if_body.stelse_body=$else_body.stif_index=labelIndex++else_index=labelIndex++))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:104:9: 'if' unr_expr ':' if_body= gen_body 'else' ':' else_body= gen_body
			{
			match(input,32,FOLLOW_32_in_if_else_stmt448); 
			pushFollow(FOLLOW_unr_expr_in_if_else_stmt450);
			unr_expr10=unr_expr();
			state._fsp--;

			match(input,21,FOLLOW_21_in_if_else_stmt452); 
			pushFollow(FOLLOW_gen_body_in_if_else_stmt456);
			if_body=gen_body();
			state._fsp--;

			match(input,31,FOLLOW_31_in_if_else_stmt467); 
			match(input,21,FOLLOW_21_in_if_else_stmt469); 
			pushFollow(FOLLOW_gen_body_in_if_else_stmt473);
			else_body=gen_body();
			state._fsp--;

			// TEMPLATE REWRITE
			// 106:42: -> ifElseStmt(cond=$unr_expr.stif_body=$if_body.stelse_body=$else_body.stif_index=labelIndex++else_index=labelIndex++)
			{
				retval.st = templateLib.getInstanceOf("ifElseStmt",new STAttrMap().put("cond", (unr_expr10!=null?((StringTemplate)unr_expr10.getTemplate()):null)).put("if_body", (if_body!=null?((StringTemplate)if_body.getTemplate()):null)).put("else_body", (else_body!=null?((StringTemplate)else_body.getTemplate()):null)).put("if_index", labelIndex++).put("else_index", labelIndex++));
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
	// $ANTLR end "if_else_stmt"


	public static class set_var_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "set_var"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:113:1: set_var : (a= var '=' b= bin_expr ) -> set_var(param_a=$b.stparam_b=32 * varIndex);
	public final set_var_return set_var() throws RecognitionException {
		set_var_return retval = new set_var_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope a =null;
		ParserRuleReturnScope b =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:114:5: ( (a= var '=' b= bin_expr ) -> set_var(param_a=$b.stparam_b=32 * varIndex))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:115:9: (a= var '=' b= bin_expr )
			{
			int varIndex = -1;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:116:9: (a= var '=' b= bin_expr )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:116:10: a= var '=' b= bin_expr
			{
			pushFollow(FOLLOW_var_in_set_var601);
			a=var();
			state._fsp--;

			  // TODO: change it from atom to something else

			            // check if that variable already defined
			            // if it didn't add it to list
			            // if it did use the index * 32 for memory address
			            varIndex = globals.indexOf((a!=null?((StringTemplate)a.getTemplate()):null).toString());
			            if (varIndex == -1 ) {globals.add((a!=null?((StringTemplate)a.getTemplate()):null).toString()); varIndex = globals.size() - 1; }
			        
			match(input,24,FOLLOW_24_in_set_var614); 
			pushFollow(FOLLOW_bin_expr_in_set_var618);
			b=bin_expr();
			state._fsp--;

			}

			// TEMPLATE REWRITE
			// 125:25: -> set_var(param_a=$b.stparam_b=32 * varIndex)
			{
				retval.st = templateLib.getInstanceOf("set_var",new STAttrMap().put("param_a", (b!=null?((StringTemplate)b.getTemplate()):null)).put("param_b", 32 * varIndex));
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
	// $ANTLR end "set_var"


	public static class get_var_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "get_var"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:128:2: get_var : (a= var -> get_var(varIndex=32 * varIndex)) ;
	public final get_var_return get_var() throws RecognitionException {
		get_var_return retval = new get_var_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope a =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:129:5: ( (a= var -> get_var(varIndex=32 * varIndex)) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:130:8: (a= var -> get_var(varIndex=32 * varIndex))
			{
			int varIndex = -1;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:131:8: (a= var -> get_var(varIndex=32 * varIndex))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:131:9: a= var
			{
			pushFollow(FOLLOW_var_in_get_var670);
			a=var();
			state._fsp--;



			             // If there is no such var throw exception
			            varIndex = globals.indexOf((a!=null?((StringTemplate)a.getTemplate()):null).toString());
			            if (varIndex == -1 ) {
			                Error err = new Error("var undefined: " + (a!=null?((StringTemplate)a.getTemplate()):null).toString());
			                throw err;
			            }
			        ;
			// TEMPLATE REWRITE
			// 141:7: -> get_var(varIndex=32 * varIndex)
			{
				retval.st = templateLib.getInstanceOf("get_var",new STAttrMap().put("varIndex", 32 * varIndex));
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
	// $ANTLR end "get_var"


	public static class unr_expr_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "unr_expr"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:146:1: unr_expr : ( '!' )* a= cond_expr -> not(param=negative? $a.st : $a.st + \" NOT\");
	public final unr_expr_return unr_expr() throws RecognitionException {
		unr_expr_return retval = new unr_expr_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope a =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:147:5: ( ( '!' )* a= cond_expr -> not(param=negative? $a.st : $a.st + \" NOT\"))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:148:9: ( '!' )* a= cond_expr
			{
			boolean negative = false;
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:149:9: ( '!' )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0==12) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:149:10: '!'
					{
					match(input,12,FOLLOW_12_in_unr_expr732); 
					negative = !negative;
					}
					break;

				default :
					break loop4;
				}
			}

			pushFollow(FOLLOW_cond_expr_in_unr_expr742);
			a=cond_expr();
			state._fsp--;

			// TEMPLATE REWRITE
			// 149:54: -> not(param=negative? $a.st : $a.st + \" NOT\")
			{
				retval.st = templateLib.getInstanceOf("not",new STAttrMap().put("param", negative? (a!=null?((StringTemplate)a.getTemplate()):null) : (a!=null?((StringTemplate)a.getTemplate()):null) + " NOT"));
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
	// $ANTLR end "unr_expr"


	public static class cond_expr_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "cond_expr"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:154:1: cond_expr : a= bin_expr ( '==' b= bin_expr -> equals(left=$a.stright=$b.st)| '<' b= bin_expr -> lessThan(left=$a.stright=$b.st)| '<=' b= bin_expr -> lessEqThan(left=$a.stright=$b.st)| '>=' b= bin_expr -> greatEqThan(left=$a.stright=$b.st)| '>' b= bin_expr -> greatThan(left=$a.stright=$b.st)| -> {$a.st}) ;
	public final cond_expr_return cond_expr() throws RecognitionException {
		cond_expr_return retval = new cond_expr_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope a =null;
		ParserRuleReturnScope b =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:155:5: (a= bin_expr ( '==' b= bin_expr -> equals(left=$a.stright=$b.st)| '<' b= bin_expr -> lessThan(left=$a.stright=$b.st)| '<=' b= bin_expr -> lessEqThan(left=$a.stright=$b.st)| '>=' b= bin_expr -> greatEqThan(left=$a.stright=$b.st)| '>' b= bin_expr -> greatThan(left=$a.stright=$b.st)| -> {$a.st}) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:155:9: a= bin_expr ( '==' b= bin_expr -> equals(left=$a.stright=$b.st)| '<' b= bin_expr -> lessThan(left=$a.stright=$b.st)| '<=' b= bin_expr -> lessEqThan(left=$a.stright=$b.st)| '>=' b= bin_expr -> greatEqThan(left=$a.stright=$b.st)| '>' b= bin_expr -> greatThan(left=$a.stright=$b.st)| -> {$a.st})
			{
			pushFollow(FOLLOW_bin_expr_in_cond_expr774);
			a=bin_expr();
			state._fsp--;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:156:9: ( '==' b= bin_expr -> equals(left=$a.stright=$b.st)| '<' b= bin_expr -> lessThan(left=$a.stright=$b.st)| '<=' b= bin_expr -> lessEqThan(left=$a.stright=$b.st)| '>=' b= bin_expr -> greatEqThan(left=$a.stright=$b.st)| '>' b= bin_expr -> greatThan(left=$a.stright=$b.st)| -> {$a.st})
			int alt5=6;
			switch ( input.LA(1) ) {
			case 25:
				{
				alt5=1;
				}
				break;
			case 22:
				{
				alt5=2;
				}
				break;
			case 23:
				{
				alt5=3;
				}
				break;
			case 27:
				{
				alt5=4;
				}
				break;
			case 26:
				{
				alt5=5;
				}
				break;
			case 21:
				{
				alt5=6;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}
			switch (alt5) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:157:18: '==' b= bin_expr
					{
					match(input,25,FOLLOW_25_in_cond_expr803); 
					pushFollow(FOLLOW_bin_expr_in_cond_expr807);
					b=bin_expr();
					state._fsp--;

					// TEMPLATE REWRITE
					// 157:34: -> equals(left=$a.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("equals",new STAttrMap().put("left", (a!=null?((StringTemplate)a.getTemplate()):null)).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:158:18: '<' b= bin_expr
					{
					match(input,22,FOLLOW_22_in_cond_expr843); 
					pushFollow(FOLLOW_bin_expr_in_cond_expr848);
					b=bin_expr();
					state._fsp--;

					// TEMPLATE REWRITE
					// 158:34: -> lessThan(left=$a.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("lessThan",new STAttrMap().put("left", (a!=null?((StringTemplate)a.getTemplate()):null)).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:159:18: '<=' b= bin_expr
					{
					match(input,23,FOLLOW_23_in_cond_expr882); 
					pushFollow(FOLLOW_bin_expr_in_cond_expr887);
					b=bin_expr();
					state._fsp--;

					// TEMPLATE REWRITE
					// 159:35: -> lessEqThan(left=$a.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("lessEqThan",new STAttrMap().put("left", (a!=null?((StringTemplate)a.getTemplate()):null)).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 4 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:160:18: '>=' b= bin_expr
					{
					match(input,27,FOLLOW_27_in_cond_expr921); 
					pushFollow(FOLLOW_bin_expr_in_cond_expr926);
					b=bin_expr();
					state._fsp--;

					// TEMPLATE REWRITE
					// 160:35: -> greatEqThan(left=$a.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("greatEqThan",new STAttrMap().put("left", (a!=null?((StringTemplate)a.getTemplate()):null)).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 5 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:161:18: '>' b= bin_expr
					{
					match(input,26,FOLLOW_26_in_cond_expr960); 
					pushFollow(FOLLOW_bin_expr_in_cond_expr965);
					b=bin_expr();
					state._fsp--;

					// TEMPLATE REWRITE
					// 161:34: -> greatThan(left=$a.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("greatThan",new STAttrMap().put("left", (a!=null?((StringTemplate)a.getTemplate()):null)).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 6 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:163:13: 
					{
					// TEMPLATE REWRITE
					// 163:13: -> {$a.st}
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
	// $ANTLR end "cond_expr"


	public static class storage_save_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "storage_save"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:167:1: storage_save : 'contract.storage[' index= bin_expr ']' '=' assignment= bin_expr -> ssave(index=$index.stdata=$assignment.st);
	public final storage_save_return storage_save() throws RecognitionException {
		storage_save_return retval = new storage_save_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope index =null;
		ParserRuleReturnScope assignment =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:168:7: ( 'contract.storage[' index= bin_expr ']' '=' assignment= bin_expr -> ssave(index=$index.stdata=$assignment.st))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:168:9: 'contract.storage[' index= bin_expr ']' '=' assignment= bin_expr
			{
			match(input,30,FOLLOW_30_in_storage_save1024); 
			pushFollow(FOLLOW_bin_expr_in_storage_save1027);
			index=bin_expr();
			state._fsp--;

			match(input,28,FOLLOW_28_in_storage_save1028); 
			match(input,24,FOLLOW_24_in_storage_save1030); 
			pushFollow(FOLLOW_bin_expr_in_storage_save1034);
			assignment=bin_expr();
			state._fsp--;

			// TEMPLATE REWRITE
			// 168:70: -> ssave(index=$index.stdata=$assignment.st)
			{
				retval.st = templateLib.getInstanceOf("ssave",new STAttrMap().put("index", (index!=null?((StringTemplate)index.getTemplate()):null)).put("data", (assignment!=null?((StringTemplate)assignment.getTemplate()):null)));
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
	// $ANTLR end "storage_save"


	public static class bin_expr_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "bin_expr"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:171:1: bin_expr : (a= atom -> {$a.st}) ( ( '+' b= atom -> add(left=$bin_expr.stright=$b.st)| '-' b= atom -> sub(left=$bin_expr.stright=$b.st)| '*' b= atom -> mul(left=$bin_expr.stright=$b.st)| '/' b= atom -> div(left=$bin_expr.stright=$b.st)| '^' b= atom -> exp(left=$bin_expr.stright=$b.st)| '%' b= atom -> mod(left=$bin_expr.stright=$b.st)| '#/' b= atom -> sdiv(left=$bin_expr.stright=$b.st)| '#%' b= atom -> smod(left=$bin_expr.stright=$b.st))* ) ;
	public final bin_expr_return bin_expr() throws RecognitionException {
		bin_expr_return retval = new bin_expr_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope a =null;
		ParserRuleReturnScope b =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:172:5: ( (a= atom -> {$a.st}) ( ( '+' b= atom -> add(left=$bin_expr.stright=$b.st)| '-' b= atom -> sub(left=$bin_expr.stright=$b.st)| '*' b= atom -> mul(left=$bin_expr.stright=$b.st)| '/' b= atom -> div(left=$bin_expr.stright=$b.st)| '^' b= atom -> exp(left=$bin_expr.stright=$b.st)| '%' b= atom -> mod(left=$bin_expr.stright=$b.st)| '#/' b= atom -> sdiv(left=$bin_expr.stright=$b.st)| '#%' b= atom -> smod(left=$bin_expr.stright=$b.st))* ) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:172:9: (a= atom -> {$a.st}) ( ( '+' b= atom -> add(left=$bin_expr.stright=$b.st)| '-' b= atom -> sub(left=$bin_expr.stright=$b.st)| '*' b= atom -> mul(left=$bin_expr.stright=$b.st)| '/' b= atom -> div(left=$bin_expr.stright=$b.st)| '^' b= atom -> exp(left=$bin_expr.stright=$b.st)| '%' b= atom -> mod(left=$bin_expr.stright=$b.st)| '#/' b= atom -> sdiv(left=$bin_expr.stright=$b.st)| '#%' b= atom -> smod(left=$bin_expr.stright=$b.st))* )
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:172:9: (a= atom -> {$a.st})
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:172:10: a= atom
			{
			pushFollow(FOLLOW_atom_in_bin_expr1072);
			a=atom();
			state._fsp--;

			// TEMPLATE REWRITE
			// 172:17: -> {$a.st}
			{
				retval.st = (a!=null?((StringTemplate)a.getTemplate()):null);
			}



			}

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:173:9: ( ( '+' b= atom -> add(left=$bin_expr.stright=$b.st)| '-' b= atom -> sub(left=$bin_expr.stright=$b.st)| '*' b= atom -> mul(left=$bin_expr.stright=$b.st)| '/' b= atom -> div(left=$bin_expr.stright=$b.st)| '^' b= atom -> exp(left=$bin_expr.stright=$b.st)| '%' b= atom -> mod(left=$bin_expr.stright=$b.st)| '#/' b= atom -> sdiv(left=$bin_expr.stright=$b.st)| '#%' b= atom -> smod(left=$bin_expr.stright=$b.st))* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:173:10: ( '+' b= atom -> add(left=$bin_expr.stright=$b.st)| '-' b= atom -> sub(left=$bin_expr.stright=$b.st)| '*' b= atom -> mul(left=$bin_expr.stright=$b.st)| '/' b= atom -> div(left=$bin_expr.stright=$b.st)| '^' b= atom -> exp(left=$bin_expr.stright=$b.st)| '%' b= atom -> mod(left=$bin_expr.stright=$b.st)| '#/' b= atom -> sdiv(left=$bin_expr.stright=$b.st)| '#%' b= atom -> smod(left=$bin_expr.stright=$b.st))*
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:173:10: ( '+' b= atom -> add(left=$bin_expr.stright=$b.st)| '-' b= atom -> sub(left=$bin_expr.stright=$b.st)| '*' b= atom -> mul(left=$bin_expr.stright=$b.st)| '/' b= atom -> div(left=$bin_expr.stright=$b.st)| '^' b= atom -> exp(left=$bin_expr.stright=$b.st)| '%' b= atom -> mod(left=$bin_expr.stright=$b.st)| '#/' b= atom -> sdiv(left=$bin_expr.stright=$b.st)| '#%' b= atom -> smod(left=$bin_expr.stright=$b.st))*
			loop6:
			while (true) {
				int alt6=9;
				switch ( input.LA(1) ) {
				case 18:
					{
					alt6=1;
					}
					break;
				case 19:
					{
					alt6=2;
					}
					break;
				case 17:
					{
					alt6=3;
					}
					break;
				case 20:
					{
					alt6=4;
					}
					break;
				case 29:
					{
					alt6=5;
					}
					break;
				case 15:
					{
					alt6=6;
					}
					break;
				case 14:
					{
					alt6=7;
					}
					break;
				case 13:
					{
					alt6=8;
					}
					break;
				}
				switch (alt6) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:173:12: '+' b= atom
					{
					match(input,18,FOLLOW_18_in_bin_expr1090); 
					pushFollow(FOLLOW_atom_in_bin_expr1095);
					b=atom();
					state._fsp--;

					// TEMPLATE REWRITE
					// 173:24: -> add(left=$bin_expr.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("add",new STAttrMap().put("left", retval.st).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:174:12: '-' b= atom
					{
					match(input,19,FOLLOW_19_in_bin_expr1126); 
					pushFollow(FOLLOW_atom_in_bin_expr1131);
					b=atom();
					state._fsp--;

					// TEMPLATE REWRITE
					// 174:24: -> sub(left=$bin_expr.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("sub",new STAttrMap().put("left", retval.st).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:175:12: '*' b= atom
					{
					match(input,17,FOLLOW_17_in_bin_expr1162); 
					pushFollow(FOLLOW_atom_in_bin_expr1167);
					b=atom();
					state._fsp--;

					// TEMPLATE REWRITE
					// 175:24: -> mul(left=$bin_expr.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("mul",new STAttrMap().put("left", retval.st).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 4 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:176:12: '/' b= atom
					{
					match(input,20,FOLLOW_20_in_bin_expr1198); 
					pushFollow(FOLLOW_atom_in_bin_expr1203);
					b=atom();
					state._fsp--;

					// TEMPLATE REWRITE
					// 176:24: -> div(left=$bin_expr.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("div",new STAttrMap().put("left", retval.st).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 5 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:177:12: '^' b= atom
					{
					match(input,29,FOLLOW_29_in_bin_expr1234); 
					pushFollow(FOLLOW_atom_in_bin_expr1239);
					b=atom();
					state._fsp--;

					// TEMPLATE REWRITE
					// 177:24: -> exp(left=$bin_expr.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("exp",new STAttrMap().put("left", retval.st).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 6 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:178:12: '%' b= atom
					{
					match(input,15,FOLLOW_15_in_bin_expr1270); 
					pushFollow(FOLLOW_atom_in_bin_expr1275);
					b=atom();
					state._fsp--;

					// TEMPLATE REWRITE
					// 178:24: -> mod(left=$bin_expr.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("mod",new STAttrMap().put("left", retval.st).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 7 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:179:12: '#/' b= atom
					{
					match(input,14,FOLLOW_14_in_bin_expr1306); 
					pushFollow(FOLLOW_atom_in_bin_expr1310);
					b=atom();
					state._fsp--;

					// TEMPLATE REWRITE
					// 179:24: -> sdiv(left=$bin_expr.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("sdiv",new STAttrMap().put("left", retval.st).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;
				case 8 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:180:12: '#%' b= atom
					{
					match(input,13,FOLLOW_13_in_bin_expr1340); 
					pushFollow(FOLLOW_atom_in_bin_expr1344);
					b=atom();
					state._fsp--;

					// TEMPLATE REWRITE
					// 180:24: -> smod(left=$bin_expr.stright=$b.st)
					{
						retval.st = templateLib.getInstanceOf("smod",new STAttrMap().put("left", retval.st).put("right", (b!=null?((StringTemplate)b.getTemplate()):null)));
					}



					}
					break;

				default :
					break loop6;
				}
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
	// $ANTLR end "bin_expr"


	public static class atom_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "atom"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:186:1: atom : ( storage_load -> iconst(value=$storage_load.st)| msg_sender -> iconst(value=$msg_sender.st)| msg_datasize -> iconst(value=$msg_datasize.st)| msg_load -> iconst(value=$msg_load.st)| get_var -> iconst(value=$get_var.st)| INTEGER -> iconst(value=$INTEGER.text)| hex_num -> iconst(value=$hex_num.st));
	public final atom_return atom() throws RecognitionException {
		atom_return retval = new atom_return();
		retval.start = input.LT(1);

		Token INTEGER16=null;
		ParserRuleReturnScope storage_load11 =null;
		ParserRuleReturnScope msg_sender12 =null;
		ParserRuleReturnScope msg_datasize13 =null;
		ParserRuleReturnScope msg_load14 =null;
		ParserRuleReturnScope get_var15 =null;
		ParserRuleReturnScope hex_num17 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:187:5: ( storage_load -> iconst(value=$storage_load.st)| msg_sender -> iconst(value=$msg_sender.st)| msg_datasize -> iconst(value=$msg_datasize.st)| msg_load -> iconst(value=$msg_load.st)| get_var -> iconst(value=$get_var.st)| INTEGER -> iconst(value=$INTEGER.text)| hex_num -> iconst(value=$hex_num.st))
			int alt7=7;
			switch ( input.LA(1) ) {
			case 30:
				{
				alt7=1;
				}
				break;
			case 35:
				{
				alt7=2;
				}
				break;
			case 34:
				{
				alt7=3;
				}
				break;
			case 33:
				{
				alt7=4;
				}
				break;
			case IDENT:
				{
				alt7=5;
				}
				break;
			case INTEGER:
				{
				alt7=6;
				}
				break;
			case HEX_NUMBER:
				{
				alt7=7;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}
			switch (alt7) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:188:7: storage_load
					{
					pushFollow(FOLLOW_storage_load_in_atom1414);
					storage_load11=storage_load();
					state._fsp--;

					// TEMPLATE REWRITE
					// 188:20: -> iconst(value=$storage_load.st)
					{
						retval.st = templateLib.getInstanceOf("iconst",new STAttrMap().put("value", (storage_load11!=null?((StringTemplate)storage_load11.getTemplate()):null)));
					}



					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:189:9: msg_sender
					{
					pushFollow(FOLLOW_msg_sender_in_atom1433);
					msg_sender12=msg_sender();
					state._fsp--;

					// TEMPLATE REWRITE
					// 189:20: -> iconst(value=$msg_sender.st)
					{
						retval.st = templateLib.getInstanceOf("iconst",new STAttrMap().put("value", (msg_sender12!=null?((StringTemplate)msg_sender12.getTemplate()):null)));
					}



					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:190:9: msg_datasize
					{
					pushFollow(FOLLOW_msg_datasize_in_atom1452);
					msg_datasize13=msg_datasize();
					state._fsp--;

					// TEMPLATE REWRITE
					// 190:22: -> iconst(value=$msg_datasize.st)
					{
						retval.st = templateLib.getInstanceOf("iconst",new STAttrMap().put("value", (msg_datasize13!=null?((StringTemplate)msg_datasize13.getTemplate()):null)));
					}



					}
					break;
				case 4 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:191:9: msg_load
					{
					pushFollow(FOLLOW_msg_load_in_atom1471);
					msg_load14=msg_load();
					state._fsp--;

					// TEMPLATE REWRITE
					// 191:18: -> iconst(value=$msg_load.st)
					{
						retval.st = templateLib.getInstanceOf("iconst",new STAttrMap().put("value", (msg_load14!=null?((StringTemplate)msg_load14.getTemplate()):null)));
					}



					}
					break;
				case 5 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:192:9: get_var
					{
					pushFollow(FOLLOW_get_var_in_atom1490);
					get_var15=get_var();
					state._fsp--;

					// TEMPLATE REWRITE
					// 192:18: -> iconst(value=$get_var.st)
					{
						retval.st = templateLib.getInstanceOf("iconst",new STAttrMap().put("value", (get_var15!=null?((StringTemplate)get_var15.getTemplate()):null)));
					}



					}
					break;
				case 6 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:193:9: INTEGER
					{
					INTEGER16=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_atom1510); 
					// TEMPLATE REWRITE
					// 193:17: -> iconst(value=$INTEGER.text)
					{
						retval.st = templateLib.getInstanceOf("iconst",new STAttrMap().put("value", (INTEGER16!=null?INTEGER16.getText():null)));
					}



					}
					break;
				case 7 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:194:9: hex_num
					{
					pushFollow(FOLLOW_hex_num_in_atom1529);
					hex_num17=hex_num();
					state._fsp--;

					// TEMPLATE REWRITE
					// 194:17: -> iconst(value=$hex_num.st)
					{
						retval.st = templateLib.getInstanceOf("iconst",new STAttrMap().put("value", (hex_num17!=null?((StringTemplate)hex_num17.getTemplate()):null)));
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


	public static class hex_num_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "hex_num"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:198:1: hex_num : HEX_NUMBER -> iconst(value=dec_num);
	public final hex_num_return hex_num() throws RecognitionException {
		hex_num_return retval = new hex_num_return();
		retval.start = input.LT(1);

		Token HEX_NUMBER18=null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:199:4: ( HEX_NUMBER -> iconst(value=dec_num))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:200:10: HEX_NUMBER
			{
			HEX_NUMBER18=(Token)match(input,HEX_NUMBER,FOLLOW_HEX_NUMBER_in_hex_num1564); 

			            String dec_num = Utils.hexStringToDecimalString((HEX_NUMBER18!=null?HEX_NUMBER18.getText():null));
			         
			// TEMPLATE REWRITE
			// 204:10: -> iconst(value=dec_num)
			{
				retval.st = templateLib.getInstanceOf("iconst",new STAttrMap().put("value", dec_num));
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
	// $ANTLR end "hex_num"


	public static class var_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "var"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:207:1: var : IDENT -> refVar(id=$IDENT.text);
	public final var_return var() throws RecognitionException {
		var_return retval = new var_return();
		retval.start = input.LT(1);

		Token IDENT19=null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:208:5: ( IDENT -> refVar(id=$IDENT.text))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:208:7: IDENT
			{
			IDENT19=(Token)match(input,IDENT,FOLLOW_IDENT_in_var1609); 
			// TEMPLATE REWRITE
			// 208:13: -> refVar(id=$IDENT.text)
			{
				retval.st = templateLib.getInstanceOf("refVar",new STAttrMap().put("id", (IDENT19!=null?IDENT19.getText():null)));
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
	// $ANTLR end "var"


	public static class storage_load_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "storage_load"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:211:1: storage_load : 'contract.storage[' bin_expr ']' -> sload(index=$bin_expr.st);
	public final storage_load_return storage_load() throws RecognitionException {
		storage_load_return retval = new storage_load_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope bin_expr20 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:212:7: ( 'contract.storage[' bin_expr ']' -> sload(index=$bin_expr.st))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:212:9: 'contract.storage[' bin_expr ']'
			{
			match(input,30,FOLLOW_30_in_storage_load1638); 
			pushFollow(FOLLOW_bin_expr_in_storage_load1639);
			bin_expr20=bin_expr();
			state._fsp--;

			match(input,28,FOLLOW_28_in_storage_load1640); 
			// TEMPLATE REWRITE
			// 212:40: -> sload(index=$bin_expr.st)
			{
				retval.st = templateLib.getInstanceOf("sload",new STAttrMap().put("index", (bin_expr20!=null?((StringTemplate)bin_expr20.getTemplate()):null)));
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
	// $ANTLR end "storage_load"


	public static class msg_load_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "msg_load"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:215:1: msg_load : 'msg.data[' bin_expr ']' -> calldataload(index=$bin_expr.st);
	public final msg_load_return msg_load() throws RecognitionException {
		msg_load_return retval = new msg_load_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope bin_expr21 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:216:7: ( 'msg.data[' bin_expr ']' -> calldataload(index=$bin_expr.st))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:216:9: 'msg.data[' bin_expr ']'
			{
			match(input,33,FOLLOW_33_in_msg_load1670); 
			pushFollow(FOLLOW_bin_expr_in_msg_load1671);
			bin_expr21=bin_expr();
			state._fsp--;

			match(input,28,FOLLOW_28_in_msg_load1672); 
			// TEMPLATE REWRITE
			// 216:33: -> calldataload(index=$bin_expr.st)
			{
				retval.st = templateLib.getInstanceOf("calldataload",new STAttrMap().put("index", (bin_expr21!=null?((StringTemplate)bin_expr21.getTemplate()):null)));
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
	// $ANTLR end "msg_load"


	public static class msg_sender_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "msg_sender"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:219:1: msg_sender : 'msg.sender' -> msdSender(;
	public final msg_sender_return msg_sender() throws RecognitionException {
		msg_sender_return retval = new msg_sender_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:220:7: ( 'msg.sender' -> msdSender()
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:220:9: 'msg.sender'
			{
			match(input,35,FOLLOW_35_in_msg_sender1703); 
			// TEMPLATE REWRITE
			// 220:22: -> msdSender(
			{
				retval.st = templateLib.getInstanceOf("msdSender");
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
	// $ANTLR end "msg_sender"


	public static class msg_datasize_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "msg_datasize"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:223:1: msg_datasize : 'msg.datasize' -> msgDatasize(;
	public final msg_datasize_return msg_datasize() throws RecognitionException {
		msg_datasize_return retval = new msg_datasize_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:224:7: ( 'msg.datasize' -> msgDatasize()
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:224:9: 'msg.datasize'
			{
			match(input,34,FOLLOW_34_in_msg_datasize1730); 
			// TEMPLATE REWRITE
			// 224:24: -> msgDatasize(
			{
				retval.st = templateLib.getInstanceOf("msgDatasize");
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
	// $ANTLR end "msg_datasize"


	public static class return_stmt_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "return_stmt"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:227:1: return_stmt : 'return(' bin_expr ')' -> returnStmt(index=$bin_expr.st);
	public final return_stmt_return return_stmt() throws RecognitionException {
		return_stmt_return retval = new return_stmt_return();
		retval.start = input.LT(1);

		ParserRuleReturnScope bin_expr22 =null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:228:7: ( 'return(' bin_expr ')' -> returnStmt(index=$bin_expr.st))
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\org\\ethereum\\serpent\\Serpent.g:228:9: 'return(' bin_expr ')'
			{
			match(input,36,FOLLOW_36_in_return_stmt1757); 
			pushFollow(FOLLOW_bin_expr_in_return_stmt1758);
			bin_expr22=bin_expr();
			state._fsp--;

			match(input,16,FOLLOW_16_in_return_stmt1759); 
			// TEMPLATE REWRITE
			// 228:31: -> returnStmt(index=$bin_expr.st)
			{
				retval.st = templateLib.getInstanceOf("returnStmt",new STAttrMap().put("index", (bin_expr22!=null?((StringTemplate)bin_expr22.getTemplate()):null)));
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
	// $ANTLR end "return_stmt"

	// Delegated rules



	public static final BitSet FOLLOW_gen_body_in_program60 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_var_in_test_1120 = new BitSet(new long[]{0x0000000000000102L});
	public static final BitSet FOLLOW_get_var_in_test_1146 = new BitSet(new long[]{0x0000000000000102L});
	public static final BitSet FOLLOW_set_var_in_gen_body201 = new BitSet(new long[]{0x0000001140000102L});
	public static final BitSet FOLLOW_storage_save_in_gen_body227 = new BitSet(new long[]{0x0000001140000102L});
	public static final BitSet FOLLOW_return_stmt_in_gen_body253 = new BitSet(new long[]{0x0000001140000102L});
	public static final BitSet FOLLOW_if_else_stmt_in_gen_body281 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_32_in_if_stmt328 = new BitSet(new long[]{0x0000000E40001380L});
	public static final BitSet FOLLOW_unr_expr_in_if_stmt330 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_if_stmt332 = new BitSet(new long[]{0x0000001140000100L});
	public static final BitSet FOLLOW_gen_body_in_if_stmt334 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_32_in_if_else_stmt448 = new BitSet(new long[]{0x0000000E40001380L});
	public static final BitSet FOLLOW_unr_expr_in_if_else_stmt450 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_if_else_stmt452 = new BitSet(new long[]{0x00000011C0000100L});
	public static final BitSet FOLLOW_gen_body_in_if_else_stmt456 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_31_in_if_else_stmt467 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_21_in_if_else_stmt469 = new BitSet(new long[]{0x0000001140000100L});
	public static final BitSet FOLLOW_gen_body_in_if_else_stmt473 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_var_in_set_var601 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_24_in_set_var614 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_set_var618 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_var_in_get_var670 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_12_in_unr_expr732 = new BitSet(new long[]{0x0000000E40001380L});
	public static final BitSet FOLLOW_cond_expr_in_unr_expr742 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_bin_expr_in_cond_expr774 = new BitSet(new long[]{0x000000000EC00002L});
	public static final BitSet FOLLOW_25_in_cond_expr803 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_cond_expr807 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_22_in_cond_expr843 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_cond_expr848 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_23_in_cond_expr882 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_cond_expr887 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_27_in_cond_expr921 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_cond_expr926 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_26_in_cond_expr960 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_cond_expr965 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_30_in_storage_save1024 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_storage_save1027 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_28_in_storage_save1028 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_24_in_storage_save1030 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_storage_save1034 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_atom_in_bin_expr1072 = new BitSet(new long[]{0x00000000201EE002L});
	public static final BitSet FOLLOW_18_in_bin_expr1090 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_atom_in_bin_expr1095 = new BitSet(new long[]{0x00000000201EE002L});
	public static final BitSet FOLLOW_19_in_bin_expr1126 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_atom_in_bin_expr1131 = new BitSet(new long[]{0x00000000201EE002L});
	public static final BitSet FOLLOW_17_in_bin_expr1162 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_atom_in_bin_expr1167 = new BitSet(new long[]{0x00000000201EE002L});
	public static final BitSet FOLLOW_20_in_bin_expr1198 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_atom_in_bin_expr1203 = new BitSet(new long[]{0x00000000201EE002L});
	public static final BitSet FOLLOW_29_in_bin_expr1234 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_atom_in_bin_expr1239 = new BitSet(new long[]{0x00000000201EE002L});
	public static final BitSet FOLLOW_15_in_bin_expr1270 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_atom_in_bin_expr1275 = new BitSet(new long[]{0x00000000201EE002L});
	public static final BitSet FOLLOW_14_in_bin_expr1306 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_atom_in_bin_expr1310 = new BitSet(new long[]{0x00000000201EE002L});
	public static final BitSet FOLLOW_13_in_bin_expr1340 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_atom_in_bin_expr1344 = new BitSet(new long[]{0x00000000201EE002L});
	public static final BitSet FOLLOW_storage_load_in_atom1414 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_msg_sender_in_atom1433 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_msg_datasize_in_atom1452 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_msg_load_in_atom1471 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_get_var_in_atom1490 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_atom1510 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_hex_num_in_atom1529 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_HEX_NUMBER_in_hex_num1564 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_var1609 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_30_in_storage_load1638 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_storage_load1639 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_28_in_storage_load1640 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_33_in_msg_load1670 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_msg_load1671 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_28_in_msg_load1672 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_35_in_msg_sender1703 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_34_in_msg_datasize1730 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_36_in_return_stmt1757 = new BitSet(new long[]{0x0000000E40000380L});
	public static final BitSet FOLLOW_bin_expr_in_return_stmt1758 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_16_in_return_stmt1759 = new BitSet(new long[]{0x0000000000000002L});
}
