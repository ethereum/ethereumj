
package samples.stg;
import java.io.*;
import org.antlr.runtime.*;
import org.antlr.stringtemplate.*;
import org.antlr.stringtemplate.language.*;

public class Main {
    public static StringTemplateGroup templates;

    public static void main(String[] args) throws Exception {
	String templateFileName;

    String userDir = System.getProperty("user.dir");

    templateFileName = userDir + "\\src\\main\\java\\samples\\stg\\Bytecode.stg";
	templates = new StringTemplateGroup(new FileReader(templateFileName),
					    AngleBracketTemplateLexer.class);

    String srcFile = userDir + "\\src\\main\\java\\samples\\stg\\input";

	CharStream input = new ANTLRFileStream(srcFile);
    CMinusLexer lexer = new CMinusLexer(input);
	CommonTokenStream tokens = new CommonTokenStream(lexer);
	CMinusParser parser = new CMinusParser(tokens);
	parser.setTemplateLib(templates);
	RuleReturnScope r = parser.program();
	System.out.println(r.getTemplate().toString());
    }
}
