// $ANTLR 3.5.2 E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g 2014-05-01 16:36:17

  package samples.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class PyEsqueParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "BLOCK", "DIGIT", "Dedent", "INTEGER", 
		"Id", "Indent", "NL", "NewLine", "SP", "SpaceChars"
	};
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

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public PyEsqueParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public PyEsqueParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return PyEsqueParser.tokenNames; }
	@Override public String getGrammarFileName() { return "E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g"; }


	public static class parse_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "parse"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:48:1: parse : block EOF -> block ;
	public final parse_return parse() throws RecognitionException {
		parse_return retval = new parse_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token EOF2=null;
		ParserRuleReturnScope block1 =null;

		Object EOF2_tree=null;
		RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
		RewriteRuleSubtreeStream stream_block=new RewriteRuleSubtreeStream(adaptor,"rule block");

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:49:2: ( block EOF -> block )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:49:4: block EOF
			{
			pushFollow(FOLLOW_block_in_parse74);
			block1=block();
			state._fsp--;

			stream_block.add(block1.getTree());
			EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_parse76);  
			stream_EOF.add(EOF2);

			// AST REWRITE
			// elements: block
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 49:14: -> block
			{
				adaptor.addChild(root_0, stream_block.nextTree());
			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "parse"


	public static class block_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "block"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:52:1: block : Indent block_atoms Dedent -> ^( BLOCK block_atoms ) ;
	public final block_return block() throws RecognitionException {
		block_return retval = new block_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token Indent3=null;
		Token Dedent5=null;
		ParserRuleReturnScope block_atoms4 =null;

		Object Indent3_tree=null;
		Object Dedent5_tree=null;
		RewriteRuleTokenStream stream_Indent=new RewriteRuleTokenStream(adaptor,"token Indent");
		RewriteRuleTokenStream stream_Dedent=new RewriteRuleTokenStream(adaptor,"token Dedent");
		RewriteRuleSubtreeStream stream_block_atoms=new RewriteRuleSubtreeStream(adaptor,"rule block_atoms");

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:53:2: ( Indent block_atoms Dedent -> ^( BLOCK block_atoms ) )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:53:4: Indent block_atoms Dedent
			{
			Indent3=(Token)match(input,Indent,FOLLOW_Indent_in_block91);  
			stream_Indent.add(Indent3);

			pushFollow(FOLLOW_block_atoms_in_block93);
			block_atoms4=block_atoms();
			state._fsp--;

			stream_block_atoms.add(block_atoms4.getTree());
			Dedent5=(Token)match(input,Dedent,FOLLOW_Dedent_in_block95);  
			stream_Dedent.add(Dedent5);

			// AST REWRITE
			// elements: block_atoms
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 53:30: -> ^( BLOCK block_atoms )
			{
				// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:53:33: ^( BLOCK block_atoms )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BLOCK, "BLOCK"), root_1);
				adaptor.addChild(root_1, stream_block_atoms.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "block"


	public static class block_atoms_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "block_atoms"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:56:1: block_atoms : ( Id | block )+ ;
	public final block_atoms_return block_atoms() throws RecognitionException {
		block_atoms_return retval = new block_atoms_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token Id6=null;
		ParserRuleReturnScope block7 =null;

		Object Id6_tree=null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:57:2: ( ( Id | block )+ )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:57:5: ( Id | block )+
			{
			root_0 = (Object)adaptor.nil();


			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:57:5: ( Id | block )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=3;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==Id) ) {
					alt1=1;
				}
				else if ( (LA1_0==Indent) ) {
					alt1=2;
				}

				switch (alt1) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:57:6: Id
					{
					Id6=(Token)match(input,Id,FOLLOW_Id_in_block_atoms116); 
					Id6_tree = (Object)adaptor.create(Id6);
					adaptor.addChild(root_0, Id6_tree);

					}
					break;
				case 2 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:57:11: block
					{
					pushFollow(FOLLOW_block_in_block_atoms120);
					block7=block();
					state._fsp--;

					adaptor.addChild(root_0, block7.getTree());

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

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "block_atoms"


	public static class expression_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "expression"
	// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:98:1: expression : ( INTEGER )* ;
	public final expression_return expression() throws RecognitionException {
		expression_return retval = new expression_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token INTEGER8=null;

		Object INTEGER8_tree=null;

		try {
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:99:2: ( ( INTEGER )* )
			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:99:4: ( INTEGER )*
			{
			root_0 = (Object)adaptor.nil();


			// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:99:4: ( INTEGER )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==INTEGER) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// E:\\WorkingArea\\ethereumJ\\src\\main\\java\\samples\\antlr\\PyEsque.g:99:4: INTEGER
					{
					INTEGER8=(Token)match(input,INTEGER,FOLLOW_INTEGER_in_expression246); 
					INTEGER8_tree = (Object)adaptor.create(INTEGER8);
					adaptor.addChild(root_0, INTEGER8_tree);

					}
					break;

				default :
					break loop2;
				}
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "expression"

	// Delegated rules



	public static final BitSet FOLLOW_block_in_parse74 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_parse76 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_Indent_in_block91 = new BitSet(new long[]{0x0000000000000300L});
	public static final BitSet FOLLOW_block_atoms_in_block93 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_Dedent_in_block95 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_Id_in_block_atoms116 = new BitSet(new long[]{0x0000000000000302L});
	public static final BitSet FOLLOW_block_in_block_atoms120 = new BitSet(new long[]{0x0000000000000302L});
	public static final BitSet FOLLOW_INTEGER_in_expression246 = new BitSet(new long[]{0x0000000000000082L});
}
