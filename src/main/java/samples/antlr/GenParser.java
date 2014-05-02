package samples.antlr;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 25/04/14 17:06
 */
public class GenParser {


    public static void main(String args[]){

        String userDir = System.getProperty("user.dir");
        String grammarFile = userDir + "\\src\\main\\java\\samples\\antlr\\Sample.g";
        org.antlr.Tool.main(new String[]{grammarFile});
    }
}
