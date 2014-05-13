package org.ethereum.serpent;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Nullable;

public class ParserUtils {
  private ParserUtils() {}

  public static <L extends Lexer> L getLexer(Class<L> lexerClass, String source) {
    CharStream input = new ANTLRInputStream(source);
    L lexer;
    try {
      lexer = lexerClass.getConstructor(CharStream.class).newInstance(input);
    } catch (Exception e) {
      throw new IllegalArgumentException("couldn't invoke lexer constructor", e);
    }
    lexer.addErrorListener(new AntlrFailureListener());
    return lexer;
  }

  public static <P extends Parser> P getParser(Class<? extends Lexer> lexerClass, Class<P> parserClass, String source) {
    Lexer lexer = getLexer(lexerClass, source);
    TokenStream tokens = new CommonTokenStream(lexer);

    P parser;
    try {
      parser = parserClass.getConstructor(TokenStream.class).newInstance(tokens);
    } catch (Exception e) {
      throw new IllegalArgumentException("couldn't invoke parser constructor", e);
    }
    parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
    parser.removeErrorListeners(); // don't spit to stderr
    parser.addErrorListener(new DiagnosticErrorListener());
    parser.addErrorListener(new AntlrFailureListener());

    return parser;
  }

  private static class AntlrFailureListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
                            int charPositionInLine, String msg, @Nullable RecognitionException e) {
      throw new AntlrParseException(line, charPositionInLine, msg, e);
    }
  }

  public static class AntlrParseException extends RuntimeException {
    public AntlrParseException(int line, int posInLine, String msg, Throwable cause) {
      // posInLine comes in 0-indexed, but we want to 1-index it so it lines up with what editors say (they
      // tend to 1-index)
      super(String.format("at line %d column %d: %s", line, posInLine+1, msg), cause);
    }
  }
}
