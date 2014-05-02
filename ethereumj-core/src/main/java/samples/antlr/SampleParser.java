// $ANTLR 3.5.2 E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g 2014-04-27 11:25:21

  package samples.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.stringtemplate.*;
import org.antlr.stringtemplate.language.*;
import java.util.HashMap;
/*******************************************************************************
 * Copyright (c) 2009 Scott Stanchfield
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
@SuppressWarnings("all")
public class SampleParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "CHAR_LITERAL", "COMMENT", "DIGIT", 
		"IDENT", "INTEGER", "LETTER", "MULTILINE_COMMENT", "STRING_LITERAL", "WS", 
		"'('", "')'", "'*'", "'+'", "','", "'-'", "'.'", "'..'", "'/'", "'/='", 
		"':'", "':='", "';'", "'<'", "'<='", "'='", "'>'", "'>='", "'Boolean'", 
		"'Char'", "'Integer'", "'String'", "'['", "']'", "'and'", "'array'", "'begin'", 
		"'constant'", "'else'", "'elsif'", "'end'", "'exit'", "'function'", "'if'", 
		"'loop'", "'mod'", "'not'", "'of'", "'or'", "'procedure'", "'program'", 
		"'record'", "'return'", "'then'", "'type'", "'var'", "'when'", "'while'"
	};
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
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public SampleParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public SampleParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected StringTemplateGroup templateLib =
	  new StringTemplateGroup("SampleParserTemplates", AngleBracketTemplateLexer.class);

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
	@Override public String[] getTokenNames() { return SampleParser.tokenNames; }
	@Override public String getGrammarFileName() { return "E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g"; }


	public static class program_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "program"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:24:1: program : 'program' IDENT '=' ( constant | variable | function | procedure | typeDecl )* 'begin' ( statement )* 'end' IDENT '.' ;
	public final program_return program() throws RecognitionException {
		program_return retval = new program_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:25:2: ( 'program' IDENT '=' ( constant | variable | function | procedure | typeDecl )* 'begin' ( statement )* 'end' IDENT '.' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:25:4: 'program' IDENT '=' ( constant | variable | function | procedure | typeDecl )* 'begin' ( statement )* 'end' IDENT '.'
			{
			match(input,53,FOLLOW_53_in_program58); 
			match(input,IDENT,FOLLOW_IDENT_in_program60); 
			match(input,28,FOLLOW_28_in_program62); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:26:3: ( constant | variable | function | procedure | typeDecl )*
			loop1:
			while (true) {
				int alt1=6;
				switch ( input.LA(1) ) {
				case 40:
					{
					alt1=1;
					}
					break;
				case 58:
					{
					alt1=2;
					}
					break;
				case 45:
					{
					alt1=3;
					}
					break;
				case 52:
					{
					alt1=4;
					}
					break;
				case 57:
					{
					alt1=5;
					}
					break;
				}
				switch (alt1) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:26:4: constant
					{
					pushFollow(FOLLOW_constant_in_program67);
					constant();
					state._fsp--;

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:26:15: variable
					{
					pushFollow(FOLLOW_variable_in_program71);
					variable();
					state._fsp--;

					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:26:26: function
					{
					pushFollow(FOLLOW_function_in_program75);
					function();
					state._fsp--;

					}
					break;
				case 4 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:26:37: procedure
					{
					pushFollow(FOLLOW_procedure_in_program79);
					procedure();
					state._fsp--;

					}
					break;
				case 5 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:26:49: typeDecl
					{
					pushFollow(FOLLOW_typeDecl_in_program83);
					typeDecl();
					state._fsp--;

					}
					break;

				default :
					break loop1;
				}
			}

			match(input,39,FOLLOW_39_in_program89); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:28:3: ( statement )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==IDENT||(LA2_0 >= 46 && LA2_0 <= 47)||LA2_0==60) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:28:3: statement
					{
					pushFollow(FOLLOW_statement_in_program93);
					statement();
					state._fsp--;

					}
					break;

				default :
					break loop2;
				}
			}

			match(input,43,FOLLOW_43_in_program98); 
			match(input,IDENT,FOLLOW_IDENT_in_program100); 
			match(input,19,FOLLOW_19_in_program102); 
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


	public static class constant_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "constant"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:32:1: constant : 'constant' IDENT ':' type ':=' expression ';' ;
	public final constant_return constant() throws RecognitionException {
		constant_return retval = new constant_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:33:2: ( 'constant' IDENT ':' type ':=' expression ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:33:4: 'constant' IDENT ':' type ':=' expression ';'
			{
			match(input,40,FOLLOW_40_in_constant113); 
			match(input,IDENT,FOLLOW_IDENT_in_constant115); 
			match(input,23,FOLLOW_23_in_constant117); 
			pushFollow(FOLLOW_type_in_constant119);
			type();
			state._fsp--;

			match(input,24,FOLLOW_24_in_constant121); 
			pushFollow(FOLLOW_expression_in_constant123);
			expression();
			state._fsp--;

			match(input,25,FOLLOW_25_in_constant125); 
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
	// $ANTLR end "constant"


	public static class variable_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "variable"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:36:1: variable : 'var' IDENT ( ',' IDENT )* ':' type ( ':=' expression )? ';' ;
	public final variable_return variable() throws RecognitionException {
		variable_return retval = new variable_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:37:2: ( 'var' IDENT ( ',' IDENT )* ':' type ( ':=' expression )? ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:37:4: 'var' IDENT ( ',' IDENT )* ':' type ( ':=' expression )? ';'
			{
			match(input,58,FOLLOW_58_in_variable136); 
			match(input,IDENT,FOLLOW_IDENT_in_variable138); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:37:16: ( ',' IDENT )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0==17) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:37:17: ',' IDENT
					{
					match(input,17,FOLLOW_17_in_variable141); 
					match(input,IDENT,FOLLOW_IDENT_in_variable143); 
					}
					break;

				default :
					break loop3;
				}
			}

			match(input,23,FOLLOW_23_in_variable147); 
			pushFollow(FOLLOW_type_in_variable149);
			type();
			state._fsp--;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:37:38: ( ':=' expression )?
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==24) ) {
				alt4=1;
			}
			switch (alt4) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:37:39: ':=' expression
					{
					match(input,24,FOLLOW_24_in_variable152); 
					pushFollow(FOLLOW_expression_in_variable154);
					expression();
					state._fsp--;

					}
					break;

			}

			match(input,25,FOLLOW_25_in_variable158); 
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


	public static class type_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "type"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:40:1: type : ( 'Integer' | 'Boolean' | 'String' | 'Char' | IDENT | typeSpec );
	public final type_return type() throws RecognitionException {
		type_return retval = new type_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:41:2: ( 'Integer' | 'Boolean' | 'String' | 'Char' | IDENT | typeSpec )
			int alt5=6;
			switch ( input.LA(1) ) {
			case 33:
				{
				alt5=1;
				}
				break;
			case 31:
				{
				alt5=2;
				}
				break;
			case 34:
				{
				alt5=3;
				}
				break;
			case 32:
				{
				alt5=4;
				}
				break;
			case IDENT:
				{
				alt5=5;
				}
				break;
			case 26:
			case 38:
			case 54:
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
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:41:4: 'Integer'
					{
					match(input,33,FOLLOW_33_in_type169); 
					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:42:4: 'Boolean'
					{
					match(input,31,FOLLOW_31_in_type174); 
					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:43:4: 'String'
					{
					match(input,34,FOLLOW_34_in_type179); 
					}
					break;
				case 4 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:44:4: 'Char'
					{
					match(input,32,FOLLOW_32_in_type184); 
					}
					break;
				case 5 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:45:4: IDENT
					{
					match(input,IDENT,FOLLOW_IDENT_in_type189); 
					}
					break;
				case 6 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:46:4: typeSpec
					{
					pushFollow(FOLLOW_typeSpec_in_type194);
					typeSpec();
					state._fsp--;

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


	public static class typeDecl_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "typeDecl"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:49:1: typeDecl : 'type' IDENT '=' typeSpec ';' ;
	public final typeDecl_return typeDecl() throws RecognitionException {
		typeDecl_return retval = new typeDecl_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:50:2: ( 'type' IDENT '=' typeSpec ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:50:4: 'type' IDENT '=' typeSpec ';'
			{
			match(input,57,FOLLOW_57_in_typeDecl205); 
			match(input,IDENT,FOLLOW_IDENT_in_typeDecl207); 
			match(input,28,FOLLOW_28_in_typeDecl209); 
			pushFollow(FOLLOW_typeSpec_in_typeDecl211);
			typeSpec();
			state._fsp--;

			match(input,25,FOLLOW_25_in_typeDecl213); 
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
	// $ANTLR end "typeDecl"


	public static class typeSpec_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "typeSpec"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:53:1: typeSpec : ( arrayType | recordType | enumType );
	public final typeSpec_return typeSpec() throws RecognitionException {
		typeSpec_return retval = new typeSpec_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:54:2: ( arrayType | recordType | enumType )
			int alt6=3;
			switch ( input.LA(1) ) {
			case 38:
				{
				alt6=1;
				}
				break;
			case 54:
				{
				alt6=2;
				}
				break;
			case 26:
				{
				alt6=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 6, 0, input);
				throw nvae;
			}
			switch (alt6) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:54:4: arrayType
					{
					pushFollow(FOLLOW_arrayType_in_typeSpec224);
					arrayType();
					state._fsp--;

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:55:4: recordType
					{
					pushFollow(FOLLOW_recordType_in_typeSpec229);
					recordType();
					state._fsp--;

					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:56:4: enumType
					{
					pushFollow(FOLLOW_enumType_in_typeSpec234);
					enumType();
					state._fsp--;

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
	// $ANTLR end "typeSpec"


	public static class arrayType_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "arrayType"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:59:1: arrayType : 'array' '[' INTEGER '..' INTEGER ']' 'of' type ;
	public final arrayType_return arrayType() throws RecognitionException {
		arrayType_return retval = new arrayType_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:60:2: ( 'array' '[' INTEGER '..' INTEGER ']' 'of' type )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:60:4: 'array' '[' INTEGER '..' INTEGER ']' 'of' type
			{
			match(input,38,FOLLOW_38_in_arrayType245); 
			match(input,35,FOLLOW_35_in_arrayType247); 
			match(input,INTEGER,FOLLOW_INTEGER_in_arrayType249); 
			match(input,20,FOLLOW_20_in_arrayType251); 
			match(input,INTEGER,FOLLOW_INTEGER_in_arrayType253); 
			match(input,36,FOLLOW_36_in_arrayType255); 
			match(input,50,FOLLOW_50_in_arrayType257); 
			pushFollow(FOLLOW_type_in_arrayType259);
			type();
			state._fsp--;

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
	// $ANTLR end "arrayType"


	public static class recordType_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "recordType"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:63:1: recordType : 'record' ( field )* 'end' 'record' ;
	public final recordType_return recordType() throws RecognitionException {
		recordType_return retval = new recordType_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:64:2: ( 'record' ( field )* 'end' 'record' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:64:4: 'record' ( field )* 'end' 'record'
			{
			match(input,54,FOLLOW_54_in_recordType270); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:64:13: ( field )*
			loop7:
			while (true) {
				int alt7=2;
				int LA7_0 = input.LA(1);
				if ( (LA7_0==IDENT) ) {
					alt7=1;
				}

				switch (alt7) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:64:13: field
					{
					pushFollow(FOLLOW_field_in_recordType272);
					field();
					state._fsp--;

					}
					break;

				default :
					break loop7;
				}
			}

			match(input,43,FOLLOW_43_in_recordType275); 
			match(input,54,FOLLOW_54_in_recordType277); 
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
	// $ANTLR end "recordType"


	public static class field_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "field"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:67:1: field : IDENT ':' type ';' ;
	public final field_return field() throws RecognitionException {
		field_return retval = new field_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:68:2: ( IDENT ':' type ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:68:4: IDENT ':' type ';'
			{
			match(input,IDENT,FOLLOW_IDENT_in_field288); 
			match(input,23,FOLLOW_23_in_field290); 
			pushFollow(FOLLOW_type_in_field292);
			type();
			state._fsp--;

			match(input,25,FOLLOW_25_in_field294); 
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
	// $ANTLR end "field"


	public static class enumType_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "enumType"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:71:1: enumType : '<' IDENT ( ',' IDENT )* '>' ;
	public final enumType_return enumType() throws RecognitionException {
		enumType_return retval = new enumType_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:72:2: ( '<' IDENT ( ',' IDENT )* '>' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:72:4: '<' IDENT ( ',' IDENT )* '>'
			{
			match(input,26,FOLLOW_26_in_enumType305); 
			match(input,IDENT,FOLLOW_IDENT_in_enumType307); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:72:14: ( ',' IDENT )*
			loop8:
			while (true) {
				int alt8=2;
				int LA8_0 = input.LA(1);
				if ( (LA8_0==17) ) {
					alt8=1;
				}

				switch (alt8) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:72:15: ',' IDENT
					{
					match(input,17,FOLLOW_17_in_enumType310); 
					match(input,IDENT,FOLLOW_IDENT_in_enumType312); 
					}
					break;

				default :
					break loop8;
				}
			}

			match(input,29,FOLLOW_29_in_enumType316); 
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
	// $ANTLR end "enumType"


	public static class statement_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "statement"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:75:1: statement : ( assignmentStatement | ifStatement | loopStatement | whileStatement | procedureCallStatement );
	public final statement_return statement() throws RecognitionException {
		statement_return retval = new statement_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:76:2: ( assignmentStatement | ifStatement | loopStatement | whileStatement | procedureCallStatement )
			int alt9=5;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				int LA9_1 = input.LA(2);
				if ( (LA9_1==24) ) {
					alt9=1;
				}
				else if ( (LA9_1==13) ) {
					alt9=5;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 9, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 46:
				{
				alt9=2;
				}
				break;
			case 47:
				{
				alt9=3;
				}
				break;
			case 60:
				{
				alt9=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 9, 0, input);
				throw nvae;
			}
			switch (alt9) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:76:4: assignmentStatement
					{
					pushFollow(FOLLOW_assignmentStatement_in_statement327);
					assignmentStatement();
					state._fsp--;

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:77:4: ifStatement
					{
					pushFollow(FOLLOW_ifStatement_in_statement332);
					ifStatement();
					state._fsp--;

					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:78:4: loopStatement
					{
					pushFollow(FOLLOW_loopStatement_in_statement337);
					loopStatement();
					state._fsp--;

					}
					break;
				case 4 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:79:4: whileStatement
					{
					pushFollow(FOLLOW_whileStatement_in_statement342);
					whileStatement();
					state._fsp--;

					}
					break;
				case 5 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:80:4: procedureCallStatement
					{
					pushFollow(FOLLOW_procedureCallStatement_in_statement347);
					procedureCallStatement();
					state._fsp--;

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
	// $ANTLR end "statement"


	public static class procedureCallStatement_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "procedureCallStatement"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:83:1: procedureCallStatement : IDENT '(' ( actualParameters )? ')' ';' ;
	public final procedureCallStatement_return procedureCallStatement() throws RecognitionException {
		procedureCallStatement_return retval = new procedureCallStatement_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:84:2: ( IDENT '(' ( actualParameters )? ')' ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:84:4: IDENT '(' ( actualParameters )? ')' ';'
			{
			match(input,IDENT,FOLLOW_IDENT_in_procedureCallStatement358); 
			match(input,13,FOLLOW_13_in_procedureCallStatement360); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:84:14: ( actualParameters )?
			int alt10=2;
			int LA10_0 = input.LA(1);
			if ( (LA10_0==CHAR_LITERAL||(LA10_0 >= IDENT && LA10_0 <= INTEGER)||LA10_0==STRING_LITERAL||LA10_0==13||LA10_0==16||LA10_0==18||LA10_0==49) ) {
				alt10=1;
			}
			switch (alt10) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:84:14: actualParameters
					{
					pushFollow(FOLLOW_actualParameters_in_procedureCallStatement362);
					actualParameters();
					state._fsp--;

					}
					break;

			}

			match(input,14,FOLLOW_14_in_procedureCallStatement365); 
			match(input,25,FOLLOW_25_in_procedureCallStatement367); 
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
	// $ANTLR end "procedureCallStatement"


	public static class actualParameters_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "actualParameters"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:87:1: actualParameters : expression ( ',' expression )* ;
	public final actualParameters_return actualParameters() throws RecognitionException {
		actualParameters_return retval = new actualParameters_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:88:2: ( expression ( ',' expression )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:88:4: expression ( ',' expression )*
			{
			pushFollow(FOLLOW_expression_in_actualParameters378);
			expression();
			state._fsp--;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:88:15: ( ',' expression )*
			loop11:
			while (true) {
				int alt11=2;
				int LA11_0 = input.LA(1);
				if ( (LA11_0==17) ) {
					alt11=1;
				}

				switch (alt11) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:88:16: ',' expression
					{
					match(input,17,FOLLOW_17_in_actualParameters381); 
					pushFollow(FOLLOW_expression_in_actualParameters383);
					expression();
					state._fsp--;

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
	// $ANTLR end "actualParameters"


	public static class ifStatement_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "ifStatement"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:91:1: ifStatement : 'if' expression 'then' ( statement )+ ( 'elsif' expression 'then' ( statement )+ )* ( 'else' ( statement )+ )? 'end' 'if' ';' ;
	public final ifStatement_return ifStatement() throws RecognitionException {
		ifStatement_return retval = new ifStatement_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:92:2: ( 'if' expression 'then' ( statement )+ ( 'elsif' expression 'then' ( statement )+ )* ( 'else' ( statement )+ )? 'end' 'if' ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:92:4: 'if' expression 'then' ( statement )+ ( 'elsif' expression 'then' ( statement )+ )* ( 'else' ( statement )+ )? 'end' 'if' ';'
			{
			match(input,46,FOLLOW_46_in_ifStatement396); 
			pushFollow(FOLLOW_expression_in_ifStatement398);
			expression();
			state._fsp--;

			match(input,56,FOLLOW_56_in_ifStatement400); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:92:27: ( statement )+
			int cnt12=0;
			loop12:
			while (true) {
				int alt12=2;
				int LA12_0 = input.LA(1);
				if ( (LA12_0==IDENT||(LA12_0 >= 46 && LA12_0 <= 47)||LA12_0==60) ) {
					alt12=1;
				}

				switch (alt12) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:92:27: statement
					{
					pushFollow(FOLLOW_statement_in_ifStatement402);
					statement();
					state._fsp--;

					}
					break;

				default :
					if ( cnt12 >= 1 ) break loop12;
					EarlyExitException eee = new EarlyExitException(12, input);
					throw eee;
				}
				cnt12++;
			}

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:93:3: ( 'elsif' expression 'then' ( statement )+ )*
			loop14:
			while (true) {
				int alt14=2;
				int LA14_0 = input.LA(1);
				if ( (LA14_0==42) ) {
					alt14=1;
				}

				switch (alt14) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:93:4: 'elsif' expression 'then' ( statement )+
					{
					match(input,42,FOLLOW_42_in_ifStatement408); 
					pushFollow(FOLLOW_expression_in_ifStatement410);
					expression();
					state._fsp--;

					match(input,56,FOLLOW_56_in_ifStatement412); 
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:93:30: ( statement )+
					int cnt13=0;
					loop13:
					while (true) {
						int alt13=2;
						int LA13_0 = input.LA(1);
						if ( (LA13_0==IDENT||(LA13_0 >= 46 && LA13_0 <= 47)||LA13_0==60) ) {
							alt13=1;
						}

						switch (alt13) {
						case 1 :
							// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:93:30: statement
							{
							pushFollow(FOLLOW_statement_in_ifStatement414);
							statement();
							state._fsp--;

							}
							break;

						default :
							if ( cnt13 >= 1 ) break loop13;
							EarlyExitException eee = new EarlyExitException(13, input);
							throw eee;
						}
						cnt13++;
					}

					}
					break;

				default :
					break loop14;
				}
			}

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:94:3: ( 'else' ( statement )+ )?
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==41) ) {
				alt16=1;
			}
			switch (alt16) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:94:4: 'else' ( statement )+
					{
					match(input,41,FOLLOW_41_in_ifStatement422); 
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:94:11: ( statement )+
					int cnt15=0;
					loop15:
					while (true) {
						int alt15=2;
						int LA15_0 = input.LA(1);
						if ( (LA15_0==IDENT||(LA15_0 >= 46 && LA15_0 <= 47)||LA15_0==60) ) {
							alt15=1;
						}

						switch (alt15) {
						case 1 :
							// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:94:11: statement
							{
							pushFollow(FOLLOW_statement_in_ifStatement424);
							statement();
							state._fsp--;

							}
							break;

						default :
							if ( cnt15 >= 1 ) break loop15;
							EarlyExitException eee = new EarlyExitException(15, input);
							throw eee;
						}
						cnt15++;
					}

					}
					break;

			}

			match(input,43,FOLLOW_43_in_ifStatement431); 
			match(input,46,FOLLOW_46_in_ifStatement433); 
			match(input,25,FOLLOW_25_in_ifStatement435); 
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
	// $ANTLR end "ifStatement"


	public static class assignmentStatement_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "assignmentStatement"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:98:1: assignmentStatement : IDENT ':=' expression ';' ;
	public final assignmentStatement_return assignmentStatement() throws RecognitionException {
		assignmentStatement_return retval = new assignmentStatement_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:99:2: ( IDENT ':=' expression ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:99:4: IDENT ':=' expression ';'
			{
			match(input,IDENT,FOLLOW_IDENT_in_assignmentStatement446); 
			match(input,24,FOLLOW_24_in_assignmentStatement448); 
			pushFollow(FOLLOW_expression_in_assignmentStatement450);
			expression();
			state._fsp--;

			match(input,25,FOLLOW_25_in_assignmentStatement452); 
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
	// $ANTLR end "assignmentStatement"


	public static class exitStatement_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "exitStatement"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:102:1: exitStatement : 'exit' 'when' expression ';' ;
	public final exitStatement_return exitStatement() throws RecognitionException {
		exitStatement_return retval = new exitStatement_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:103:2: ( 'exit' 'when' expression ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:103:4: 'exit' 'when' expression ';'
			{
			match(input,44,FOLLOW_44_in_exitStatement463); 
			match(input,59,FOLLOW_59_in_exitStatement465); 
			pushFollow(FOLLOW_expression_in_exitStatement467);
			expression();
			state._fsp--;

			match(input,25,FOLLOW_25_in_exitStatement469); 
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
	// $ANTLR end "exitStatement"


	public static class whileStatement_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "whileStatement"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:106:1: whileStatement : 'while' expression 'loop' ( statement | exitStatement )* 'end' 'loop' ';' ;
	public final whileStatement_return whileStatement() throws RecognitionException {
		whileStatement_return retval = new whileStatement_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:107:2: ( 'while' expression 'loop' ( statement | exitStatement )* 'end' 'loop' ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:107:4: 'while' expression 'loop' ( statement | exitStatement )* 'end' 'loop' ';'
			{
			match(input,60,FOLLOW_60_in_whileStatement480); 
			pushFollow(FOLLOW_expression_in_whileStatement482);
			expression();
			state._fsp--;

			match(input,47,FOLLOW_47_in_whileStatement484); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:108:3: ( statement | exitStatement )*
			loop17:
			while (true) {
				int alt17=3;
				int LA17_0 = input.LA(1);
				if ( (LA17_0==IDENT||(LA17_0 >= 46 && LA17_0 <= 47)||LA17_0==60) ) {
					alt17=1;
				}
				else if ( (LA17_0==44) ) {
					alt17=2;
				}

				switch (alt17) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:108:4: statement
					{
					pushFollow(FOLLOW_statement_in_whileStatement489);
					statement();
					state._fsp--;

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:108:14: exitStatement
					{
					pushFollow(FOLLOW_exitStatement_in_whileStatement491);
					exitStatement();
					state._fsp--;

					}
					break;

				default :
					break loop17;
				}
			}

			match(input,43,FOLLOW_43_in_whileStatement497); 
			match(input,47,FOLLOW_47_in_whileStatement499); 
			match(input,25,FOLLOW_25_in_whileStatement501); 
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
	// $ANTLR end "whileStatement"


	public static class loopStatement_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "loopStatement"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:112:1: loopStatement : 'loop' ( statement | exitStatement )* 'end' 'loop' ';' ;
	public final loopStatement_return loopStatement() throws RecognitionException {
		loopStatement_return retval = new loopStatement_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:113:2: ( 'loop' ( statement | exitStatement )* 'end' 'loop' ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:113:4: 'loop' ( statement | exitStatement )* 'end' 'loop' ';'
			{
			match(input,47,FOLLOW_47_in_loopStatement512); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:113:11: ( statement | exitStatement )*
			loop18:
			while (true) {
				int alt18=3;
				int LA18_0 = input.LA(1);
				if ( (LA18_0==IDENT||(LA18_0 >= 46 && LA18_0 <= 47)||LA18_0==60) ) {
					alt18=1;
				}
				else if ( (LA18_0==44) ) {
					alt18=2;
				}

				switch (alt18) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:113:12: statement
					{
					pushFollow(FOLLOW_statement_in_loopStatement515);
					statement();
					state._fsp--;

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:113:22: exitStatement
					{
					pushFollow(FOLLOW_exitStatement_in_loopStatement517);
					exitStatement();
					state._fsp--;

					}
					break;

				default :
					break loop18;
				}
			}

			match(input,43,FOLLOW_43_in_loopStatement521); 
			match(input,47,FOLLOW_47_in_loopStatement523); 
			match(input,25,FOLLOW_25_in_loopStatement525); 
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
	// $ANTLR end "loopStatement"


	public static class returnStatement_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "returnStatement"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:116:1: returnStatement : 'return' expression ';' ;
	public final returnStatement_return returnStatement() throws RecognitionException {
		returnStatement_return retval = new returnStatement_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:117:2: ( 'return' expression ';' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:117:4: 'return' expression ';'
			{
			match(input,55,FOLLOW_55_in_returnStatement536); 
			pushFollow(FOLLOW_expression_in_returnStatement538);
			expression();
			state._fsp--;

			match(input,25,FOLLOW_25_in_returnStatement540); 
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
	// $ANTLR end "returnStatement"


	public static class procedure_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "procedure"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:120:1: procedure : 'procedure' IDENT '(' ( parameters )? ')' '=' ( constant | variable )* 'begin' ( statement )* 'end' IDENT '.' ;
	public final procedure_return procedure() throws RecognitionException {
		procedure_return retval = new procedure_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:121:2: ( 'procedure' IDENT '(' ( parameters )? ')' '=' ( constant | variable )* 'begin' ( statement )* 'end' IDENT '.' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:121:4: 'procedure' IDENT '(' ( parameters )? ')' '=' ( constant | variable )* 'begin' ( statement )* 'end' IDENT '.'
			{
			match(input,52,FOLLOW_52_in_procedure551); 
			match(input,IDENT,FOLLOW_IDENT_in_procedure553); 
			match(input,13,FOLLOW_13_in_procedure555); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:121:26: ( parameters )?
			int alt19=2;
			int LA19_0 = input.LA(1);
			if ( (LA19_0==IDENT||LA19_0==58) ) {
				alt19=1;
			}
			switch (alt19) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:121:26: parameters
					{
					pushFollow(FOLLOW_parameters_in_procedure557);
					parameters();
					state._fsp--;

					}
					break;

			}

			match(input,14,FOLLOW_14_in_procedure560); 
			match(input,28,FOLLOW_28_in_procedure562); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:122:3: ( constant | variable )*
			loop20:
			while (true) {
				int alt20=3;
				int LA20_0 = input.LA(1);
				if ( (LA20_0==40) ) {
					alt20=1;
				}
				else if ( (LA20_0==58) ) {
					alt20=2;
				}

				switch (alt20) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:122:4: constant
					{
					pushFollow(FOLLOW_constant_in_procedure567);
					constant();
					state._fsp--;

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:122:15: variable
					{
					pushFollow(FOLLOW_variable_in_procedure571);
					variable();
					state._fsp--;

					}
					break;

				default :
					break loop20;
				}
			}

			match(input,39,FOLLOW_39_in_procedure577); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:124:3: ( statement )*
			loop21:
			while (true) {
				int alt21=2;
				int LA21_0 = input.LA(1);
				if ( (LA21_0==IDENT||(LA21_0 >= 46 && LA21_0 <= 47)||LA21_0==60) ) {
					alt21=1;
				}

				switch (alt21) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:124:3: statement
					{
					pushFollow(FOLLOW_statement_in_procedure581);
					statement();
					state._fsp--;

					}
					break;

				default :
					break loop21;
				}
			}

			match(input,43,FOLLOW_43_in_procedure586); 
			match(input,IDENT,FOLLOW_IDENT_in_procedure588); 
			match(input,19,FOLLOW_19_in_procedure590); 
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
	// $ANTLR end "procedure"


	public static class function_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "function"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:127:1: function : 'function' IDENT '(' ( parameters )? ')' ':' type '=' ( constant | variable )* 'begin' ( statement | returnStatement )* 'end' IDENT '.' ;
	public final function_return function() throws RecognitionException {
		function_return retval = new function_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:128:2: ( 'function' IDENT '(' ( parameters )? ')' ':' type '=' ( constant | variable )* 'begin' ( statement | returnStatement )* 'end' IDENT '.' )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:128:4: 'function' IDENT '(' ( parameters )? ')' ':' type '=' ( constant | variable )* 'begin' ( statement | returnStatement )* 'end' IDENT '.'
			{
			match(input,45,FOLLOW_45_in_function600); 
			match(input,IDENT,FOLLOW_IDENT_in_function602); 
			match(input,13,FOLLOW_13_in_function604); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:128:25: ( parameters )?
			int alt22=2;
			int LA22_0 = input.LA(1);
			if ( (LA22_0==IDENT||LA22_0==58) ) {
				alt22=1;
			}
			switch (alt22) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:128:25: parameters
					{
					pushFollow(FOLLOW_parameters_in_function606);
					parameters();
					state._fsp--;

					}
					break;

			}

			match(input,14,FOLLOW_14_in_function609); 
			match(input,23,FOLLOW_23_in_function611); 
			pushFollow(FOLLOW_type_in_function613);
			type();
			state._fsp--;

			match(input,28,FOLLOW_28_in_function615); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:129:3: ( constant | variable )*
			loop23:
			while (true) {
				int alt23=3;
				int LA23_0 = input.LA(1);
				if ( (LA23_0==40) ) {
					alt23=1;
				}
				else if ( (LA23_0==58) ) {
					alt23=2;
				}

				switch (alt23) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:129:4: constant
					{
					pushFollow(FOLLOW_constant_in_function620);
					constant();
					state._fsp--;

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:129:15: variable
					{
					pushFollow(FOLLOW_variable_in_function624);
					variable();
					state._fsp--;

					}
					break;

				default :
					break loop23;
				}
			}

			match(input,39,FOLLOW_39_in_function630); 
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:131:3: ( statement | returnStatement )*
			loop24:
			while (true) {
				int alt24=3;
				int LA24_0 = input.LA(1);
				if ( (LA24_0==IDENT||(LA24_0 >= 46 && LA24_0 <= 47)||LA24_0==60) ) {
					alt24=1;
				}
				else if ( (LA24_0==55) ) {
					alt24=2;
				}

				switch (alt24) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:131:4: statement
					{
					pushFollow(FOLLOW_statement_in_function635);
					statement();
					state._fsp--;

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:131:14: returnStatement
					{
					pushFollow(FOLLOW_returnStatement_in_function637);
					returnStatement();
					state._fsp--;

					}
					break;

				default :
					break loop24;
				}
			}

			match(input,43,FOLLOW_43_in_function643); 
			match(input,IDENT,FOLLOW_IDENT_in_function645); 
			match(input,19,FOLLOW_19_in_function647); 
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
	// $ANTLR end "function"


	public static class parameters_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "parameters"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:135:1: parameters : parameter ( ',' parameter )* ;
	public final parameters_return parameters() throws RecognitionException {
		parameters_return retval = new parameters_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:136:2: ( parameter ( ',' parameter )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:136:4: parameter ( ',' parameter )*
			{
			pushFollow(FOLLOW_parameter_in_parameters658);
			parameter();
			state._fsp--;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:136:14: ( ',' parameter )*
			loop25:
			while (true) {
				int alt25=2;
				int LA25_0 = input.LA(1);
				if ( (LA25_0==17) ) {
					alt25=1;
				}

				switch (alt25) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:136:15: ',' parameter
					{
					match(input,17,FOLLOW_17_in_parameters661); 
					pushFollow(FOLLOW_parameter_in_parameters663);
					parameter();
					state._fsp--;

					}
					break;

				default :
					break loop25;
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
	// $ANTLR end "parameters"


	public static class parameter_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "parameter"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:139:1: parameter : ( 'var' )? IDENT ':' type ;
	public final parameter_return parameter() throws RecognitionException {
		parameter_return retval = new parameter_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:140:2: ( ( 'var' )? IDENT ':' type )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:140:4: ( 'var' )? IDENT ':' type
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:140:4: ( 'var' )?
			int alt26=2;
			int LA26_0 = input.LA(1);
			if ( (LA26_0==58) ) {
				alt26=1;
			}
			switch (alt26) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:140:4: 'var'
					{
					match(input,58,FOLLOW_58_in_parameter676); 
					}
					break;

			}

			match(input,IDENT,FOLLOW_IDENT_in_parameter679); 
			match(input,23,FOLLOW_23_in_parameter681); 
			pushFollow(FOLLOW_type_in_parameter683);
			type();
			state._fsp--;

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
	// $ANTLR end "parameter"


	public static class term_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "term"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:146:1: term : ( IDENT | '(' expression ')' | INTEGER | STRING_LITERAL | CHAR_LITERAL | IDENT '(' actualParameters ')' );
	public final term_return term() throws RecognitionException {
		term_return retval = new term_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:147:2: ( IDENT | '(' expression ')' | INTEGER | STRING_LITERAL | CHAR_LITERAL | IDENT '(' actualParameters ')' )
			int alt27=6;
			switch ( input.LA(1) ) {
			case IDENT:
				{
				int LA27_1 = input.LA(2);
				if ( (LA27_1==13) ) {
					alt27=6;
				}
				else if ( ((LA27_1 >= 14 && LA27_1 <= 18)||(LA27_1 >= 21 && LA27_1 <= 22)||(LA27_1 >= 25 && LA27_1 <= 30)||LA27_1==37||(LA27_1 >= 47 && LA27_1 <= 48)||LA27_1==51||LA27_1==56) ) {
					alt27=1;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 27, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 13:
				{
				alt27=2;
				}
				break;
			case INTEGER:
				{
				alt27=3;
				}
				break;
			case STRING_LITERAL:
				{
				alt27=4;
				}
				break;
			case CHAR_LITERAL:
				{
				alt27=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 27, 0, input);
				throw nvae;
			}
			switch (alt27) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:147:4: IDENT
					{
					match(input,IDENT,FOLLOW_IDENT_in_term697); 
					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:148:4: '(' expression ')'
					{
					match(input,13,FOLLOW_13_in_term702); 
					pushFollow(FOLLOW_expression_in_term704);
					expression();
					state._fsp--;

					match(input,14,FOLLOW_14_in_term706); 
					}
					break;
				case 3 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:149:4: INTEGER
					{
					match(input,INTEGER,FOLLOW_INTEGER_in_term711); 
					}
					break;
				case 4 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:150:4: STRING_LITERAL
					{
					match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_term716); 
					}
					break;
				case 5 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:151:4: CHAR_LITERAL
					{
					match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_term721); 
					}
					break;
				case 6 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:152:4: IDENT '(' actualParameters ')'
					{
					match(input,IDENT,FOLLOW_IDENT_in_term726); 
					match(input,13,FOLLOW_13_in_term728); 
					pushFollow(FOLLOW_actualParameters_in_term730);
					actualParameters();
					state._fsp--;

					match(input,14,FOLLOW_14_in_term732); 
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
	// $ANTLR end "term"


	public static class negation_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "negation"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:155:1: negation : ( 'not' )* term ;
	public final negation_return negation() throws RecognitionException {
		negation_return retval = new negation_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:156:2: ( ( 'not' )* term )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:156:4: ( 'not' )* term
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:156:4: ( 'not' )*
			loop28:
			while (true) {
				int alt28=2;
				int LA28_0 = input.LA(1);
				if ( (LA28_0==49) ) {
					alt28=1;
				}

				switch (alt28) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:156:4: 'not'
					{
					match(input,49,FOLLOW_49_in_negation743); 
					}
					break;

				default :
					break loop28;
				}
			}

			pushFollow(FOLLOW_term_in_negation746);
			term();
			state._fsp--;

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
	// $ANTLR end "negation"


	public static class unary_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "unary"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:159:1: unary : ( '+' | '-' )* negation ;
	public final unary_return unary() throws RecognitionException {
		unary_return retval = new unary_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:160:2: ( ( '+' | '-' )* negation )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:160:4: ( '+' | '-' )* negation
			{
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:160:4: ( '+' | '-' )*
			loop29:
			while (true) {
				int alt29=2;
				int LA29_0 = input.LA(1);
				if ( (LA29_0==16||LA29_0==18) ) {
					alt29=1;
				}

				switch (alt29) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:
					{
					if ( input.LA(1)==16||input.LA(1)==18 ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					}
					break;

				default :
					break loop29;
				}
			}

			pushFollow(FOLLOW_negation_in_unary766);
			negation();
			state._fsp--;

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
	// $ANTLR end "unary"


	public static class mult_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "mult"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:163:1: mult : unary ( ( '*' | '/' | 'mod' ) unary )* ;
	public final mult_return mult() throws RecognitionException {
		mult_return retval = new mult_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:164:2: ( unary ( ( '*' | '/' | 'mod' ) unary )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:164:4: unary ( ( '*' | '/' | 'mod' ) unary )*
			{
			pushFollow(FOLLOW_unary_in_mult777);
			unary();
			state._fsp--;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:164:10: ( ( '*' | '/' | 'mod' ) unary )*
			loop30:
			while (true) {
				int alt30=2;
				int LA30_0 = input.LA(1);
				if ( (LA30_0==15||LA30_0==21||LA30_0==48) ) {
					alt30=1;
				}

				switch (alt30) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:164:11: ( '*' | '/' | 'mod' ) unary
					{
					if ( input.LA(1)==15||input.LA(1)==21||input.LA(1)==48 ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_unary_in_mult792);
					unary();
					state._fsp--;

					}
					break;

				default :
					break loop30;
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
	// $ANTLR end "mult"


	public static class add_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "add"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:167:1: add : mult ( ( '+' | '-' ) mult )* ;
	public final add_return add() throws RecognitionException {
		add_return retval = new add_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:168:2: ( mult ( ( '+' | '-' ) mult )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:168:4: mult ( ( '+' | '-' ) mult )*
			{
			pushFollow(FOLLOW_mult_in_add805);
			mult();
			state._fsp--;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:168:9: ( ( '+' | '-' ) mult )*
			loop31:
			while (true) {
				int alt31=2;
				int LA31_0 = input.LA(1);
				if ( (LA31_0==16||LA31_0==18) ) {
					alt31=1;
				}

				switch (alt31) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:168:10: ( '+' | '-' ) mult
					{
					if ( input.LA(1)==16||input.LA(1)==18 ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_mult_in_add816);
					mult();
					state._fsp--;

					}
					break;

				default :
					break loop31;
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
	// $ANTLR end "add"


	public static class relation_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "relation"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:171:1: relation : add ( ( '=' | '/=' | '<' | '<=' | '>=' | '>' ) add )* ;
	public final relation_return relation() throws RecognitionException {
		relation_return retval = new relation_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:172:2: ( add ( ( '=' | '/=' | '<' | '<=' | '>=' | '>' ) add )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:172:4: add ( ( '=' | '/=' | '<' | '<=' | '>=' | '>' ) add )*
			{
			pushFollow(FOLLOW_add_in_relation829);
			add();
			state._fsp--;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:172:8: ( ( '=' | '/=' | '<' | '<=' | '>=' | '>' ) add )*
			loop32:
			while (true) {
				int alt32=2;
				int LA32_0 = input.LA(1);
				if ( (LA32_0==22||(LA32_0 >= 26 && LA32_0 <= 30)) ) {
					alt32=1;
				}

				switch (alt32) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:172:9: ( '=' | '/=' | '<' | '<=' | '>=' | '>' ) add
					{
					if ( input.LA(1)==22||(input.LA(1) >= 26 && input.LA(1) <= 30) ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_add_in_relation856);
					add();
					state._fsp--;

					}
					break;

				default :
					break loop32;
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
	// $ANTLR end "relation"


	public static class expression_return extends ParserRuleReturnScope {
		public StringTemplate st;
		public Object getTemplate() { return st; }
		public String toString() { return st==null?null:st.toString(); }
	};


	// $ANTLR start "expression"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:175:1: expression : relation ( ( 'and' | 'or' ) relation )* ;
	public final expression_return expression() throws RecognitionException {
		expression_return retval = new expression_return();
		retval.start = input.LT(1);

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:176:2: ( relation ( ( 'and' | 'or' ) relation )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:176:4: relation ( ( 'and' | 'or' ) relation )*
			{
			pushFollow(FOLLOW_relation_in_expression869);
			relation();
			state._fsp--;

			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:176:13: ( ( 'and' | 'or' ) relation )*
			loop33:
			while (true) {
				int alt33=2;
				int LA33_0 = input.LA(1);
				if ( (LA33_0==37||LA33_0==51) ) {
					alt33=1;
				}

				switch (alt33) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\Sample.g:176:14: ( 'and' | 'or' ) relation
					{
					if ( input.LA(1)==37||input.LA(1)==51 ) {
						input.consume();
						state.errorRecovery=false;
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_relation_in_expression880);
					relation();
					state._fsp--;

					}
					break;

				default :
					break loop33;
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
	// $ANTLR end "expression"

	// Delegated rules



	public static final BitSet FOLLOW_53_in_program58 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_program60 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_28_in_program62 = new BitSet(new long[]{0x0610218000000000L});
	public static final BitSet FOLLOW_constant_in_program67 = new BitSet(new long[]{0x0610218000000000L});
	public static final BitSet FOLLOW_variable_in_program71 = new BitSet(new long[]{0x0610218000000000L});
	public static final BitSet FOLLOW_function_in_program75 = new BitSet(new long[]{0x0610218000000000L});
	public static final BitSet FOLLOW_procedure_in_program79 = new BitSet(new long[]{0x0610218000000000L});
	public static final BitSet FOLLOW_typeDecl_in_program83 = new BitSet(new long[]{0x0610218000000000L});
	public static final BitSet FOLLOW_39_in_program89 = new BitSet(new long[]{0x1000C80000000080L});
	public static final BitSet FOLLOW_statement_in_program93 = new BitSet(new long[]{0x1000C80000000080L});
	public static final BitSet FOLLOW_43_in_program98 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_program100 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_19_in_program102 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_40_in_constant113 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_constant115 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_23_in_constant117 = new BitSet(new long[]{0x0040004784000080L});
	public static final BitSet FOLLOW_type_in_constant119 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_24_in_constant121 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_constant123 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_constant125 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_58_in_variable136 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_variable138 = new BitSet(new long[]{0x0000000000820000L});
	public static final BitSet FOLLOW_17_in_variable141 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_variable143 = new BitSet(new long[]{0x0000000000820000L});
	public static final BitSet FOLLOW_23_in_variable147 = new BitSet(new long[]{0x0040004784000080L});
	public static final BitSet FOLLOW_type_in_variable149 = new BitSet(new long[]{0x0000000003000000L});
	public static final BitSet FOLLOW_24_in_variable152 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_variable154 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_variable158 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_33_in_type169 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_31_in_type174 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_34_in_type179 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_32_in_type184 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_type189 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_typeSpec_in_type194 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_57_in_typeDecl205 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_typeDecl207 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_28_in_typeDecl209 = new BitSet(new long[]{0x0040004004000000L});
	public static final BitSet FOLLOW_typeSpec_in_typeDecl211 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_typeDecl213 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arrayType_in_typeSpec224 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_recordType_in_typeSpec229 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumType_in_typeSpec234 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_38_in_arrayType245 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_35_in_arrayType247 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_INTEGER_in_arrayType249 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_20_in_arrayType251 = new BitSet(new long[]{0x0000000000000100L});
	public static final BitSet FOLLOW_INTEGER_in_arrayType253 = new BitSet(new long[]{0x0000001000000000L});
	public static final BitSet FOLLOW_36_in_arrayType255 = new BitSet(new long[]{0x0004000000000000L});
	public static final BitSet FOLLOW_50_in_arrayType257 = new BitSet(new long[]{0x0040004784000080L});
	public static final BitSet FOLLOW_type_in_arrayType259 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_54_in_recordType270 = new BitSet(new long[]{0x0000080000000080L});
	public static final BitSet FOLLOW_field_in_recordType272 = new BitSet(new long[]{0x0000080000000080L});
	public static final BitSet FOLLOW_43_in_recordType275 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_54_in_recordType277 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_field288 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_23_in_field290 = new BitSet(new long[]{0x0040004784000080L});
	public static final BitSet FOLLOW_type_in_field292 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_field294 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_26_in_enumType305 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_enumType307 = new BitSet(new long[]{0x0000000020020000L});
	public static final BitSet FOLLOW_17_in_enumType310 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_enumType312 = new BitSet(new long[]{0x0000000020020000L});
	public static final BitSet FOLLOW_29_in_enumType316 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_assignmentStatement_in_statement327 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ifStatement_in_statement332 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_loopStatement_in_statement337 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_whileStatement_in_statement342 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_procedureCallStatement_in_statement347 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_procedureCallStatement358 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_13_in_procedureCallStatement360 = new BitSet(new long[]{0x0002000000056990L});
	public static final BitSet FOLLOW_actualParameters_in_procedureCallStatement362 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_14_in_procedureCallStatement365 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_procedureCallStatement367 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_actualParameters378 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_17_in_actualParameters381 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_actualParameters383 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_46_in_ifStatement396 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_ifStatement398 = new BitSet(new long[]{0x0100000000000000L});
	public static final BitSet FOLLOW_56_in_ifStatement400 = new BitSet(new long[]{0x1000C00000000080L});
	public static final BitSet FOLLOW_statement_in_ifStatement402 = new BitSet(new long[]{0x1000CE0000000080L});
	public static final BitSet FOLLOW_42_in_ifStatement408 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_ifStatement410 = new BitSet(new long[]{0x0100000000000000L});
	public static final BitSet FOLLOW_56_in_ifStatement412 = new BitSet(new long[]{0x1000C00000000080L});
	public static final BitSet FOLLOW_statement_in_ifStatement414 = new BitSet(new long[]{0x1000CE0000000080L});
	public static final BitSet FOLLOW_41_in_ifStatement422 = new BitSet(new long[]{0x1000C00000000080L});
	public static final BitSet FOLLOW_statement_in_ifStatement424 = new BitSet(new long[]{0x1000C80000000080L});
	public static final BitSet FOLLOW_43_in_ifStatement431 = new BitSet(new long[]{0x0000400000000000L});
	public static final BitSet FOLLOW_46_in_ifStatement433 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_ifStatement435 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_assignmentStatement446 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_24_in_assignmentStatement448 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_assignmentStatement450 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_assignmentStatement452 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_44_in_exitStatement463 = new BitSet(new long[]{0x0800000000000000L});
	public static final BitSet FOLLOW_59_in_exitStatement465 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_exitStatement467 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_exitStatement469 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_60_in_whileStatement480 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_whileStatement482 = new BitSet(new long[]{0x0000800000000000L});
	public static final BitSet FOLLOW_47_in_whileStatement484 = new BitSet(new long[]{0x1000D80000000080L});
	public static final BitSet FOLLOW_statement_in_whileStatement489 = new BitSet(new long[]{0x1000D80000000080L});
	public static final BitSet FOLLOW_exitStatement_in_whileStatement491 = new BitSet(new long[]{0x1000D80000000080L});
	public static final BitSet FOLLOW_43_in_whileStatement497 = new BitSet(new long[]{0x0000800000000000L});
	public static final BitSet FOLLOW_47_in_whileStatement499 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_whileStatement501 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_47_in_loopStatement512 = new BitSet(new long[]{0x1000D80000000080L});
	public static final BitSet FOLLOW_statement_in_loopStatement515 = new BitSet(new long[]{0x1000D80000000080L});
	public static final BitSet FOLLOW_exitStatement_in_loopStatement517 = new BitSet(new long[]{0x1000D80000000080L});
	public static final BitSet FOLLOW_43_in_loopStatement521 = new BitSet(new long[]{0x0000800000000000L});
	public static final BitSet FOLLOW_47_in_loopStatement523 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_loopStatement525 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_55_in_returnStatement536 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_returnStatement538 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_returnStatement540 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_52_in_procedure551 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_procedure553 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_13_in_procedure555 = new BitSet(new long[]{0x0400000000004080L});
	public static final BitSet FOLLOW_parameters_in_procedure557 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_14_in_procedure560 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_28_in_procedure562 = new BitSet(new long[]{0x0400018000000000L});
	public static final BitSet FOLLOW_constant_in_procedure567 = new BitSet(new long[]{0x0400018000000000L});
	public static final BitSet FOLLOW_variable_in_procedure571 = new BitSet(new long[]{0x0400018000000000L});
	public static final BitSet FOLLOW_39_in_procedure577 = new BitSet(new long[]{0x1000C80000000080L});
	public static final BitSet FOLLOW_statement_in_procedure581 = new BitSet(new long[]{0x1000C80000000080L});
	public static final BitSet FOLLOW_43_in_procedure586 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_procedure588 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_19_in_procedure590 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_45_in_function600 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_function602 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_13_in_function604 = new BitSet(new long[]{0x0400000000004080L});
	public static final BitSet FOLLOW_parameters_in_function606 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_14_in_function609 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_23_in_function611 = new BitSet(new long[]{0x0040004784000080L});
	public static final BitSet FOLLOW_type_in_function613 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_28_in_function615 = new BitSet(new long[]{0x0400018000000000L});
	public static final BitSet FOLLOW_constant_in_function620 = new BitSet(new long[]{0x0400018000000000L});
	public static final BitSet FOLLOW_variable_in_function624 = new BitSet(new long[]{0x0400018000000000L});
	public static final BitSet FOLLOW_39_in_function630 = new BitSet(new long[]{0x1080C80000000080L});
	public static final BitSet FOLLOW_statement_in_function635 = new BitSet(new long[]{0x1080C80000000080L});
	public static final BitSet FOLLOW_returnStatement_in_function637 = new BitSet(new long[]{0x1080C80000000080L});
	public static final BitSet FOLLOW_43_in_function643 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_function645 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_19_in_function647 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_parameter_in_parameters658 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_17_in_parameters661 = new BitSet(new long[]{0x0400000000000080L});
	public static final BitSet FOLLOW_parameter_in_parameters663 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_58_in_parameter676 = new BitSet(new long[]{0x0000000000000080L});
	public static final BitSet FOLLOW_IDENT_in_parameter679 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_23_in_parameter681 = new BitSet(new long[]{0x0040004784000080L});
	public static final BitSet FOLLOW_type_in_parameter683 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_term697 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_13_in_term702 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_expression_in_term704 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_14_in_term706 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_in_term711 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_term716 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CHAR_LITERAL_in_term721 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENT_in_term726 = new BitSet(new long[]{0x0000000000002000L});
	public static final BitSet FOLLOW_13_in_term728 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_actualParameters_in_term730 = new BitSet(new long[]{0x0000000000004000L});
	public static final BitSet FOLLOW_14_in_term732 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_49_in_negation743 = new BitSet(new long[]{0x0002000000002990L});
	public static final BitSet FOLLOW_term_in_negation746 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_negation_in_unary766 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unary_in_mult777 = new BitSet(new long[]{0x0001000000208002L});
	public static final BitSet FOLLOW_set_in_mult780 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_unary_in_mult792 = new BitSet(new long[]{0x0001000000208002L});
	public static final BitSet FOLLOW_mult_in_add805 = new BitSet(new long[]{0x0000000000050002L});
	public static final BitSet FOLLOW_set_in_add808 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_mult_in_add816 = new BitSet(new long[]{0x0000000000050002L});
	public static final BitSet FOLLOW_add_in_relation829 = new BitSet(new long[]{0x000000007C400002L});
	public static final BitSet FOLLOW_set_in_relation832 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_add_in_relation856 = new BitSet(new long[]{0x000000007C400002L});
	public static final BitSet FOLLOW_relation_in_expression869 = new BitSet(new long[]{0x0008002000000002L});
	public static final BitSet FOLLOW_set_in_expression872 = new BitSet(new long[]{0x0002000000052990L});
	public static final BitSet FOLLOW_relation_in_expression880 = new BitSet(new long[]{0x0008002000000002L});
}
