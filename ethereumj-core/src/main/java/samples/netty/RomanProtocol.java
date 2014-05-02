package samples.netty;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 10/04/14 11:19
 */
public class RomanProtocol {


    public static String getAnswer(String msg){

        if (msg.equals("9191-Hello"))
            return ("9191-Good day sir");

        if (msg.equals("9191-Good day sir"))
            return ("9191-What is you name");

        if (msg.equals("9191-What is you name"))
            return ("9191-My Name is Ethereum");

        if (msg.matches("9191-My Name is ([\\w])*")) {

            String name = msg.substring(16);

            return ("9191-Good to see  you: " + name);
        }

        if (msg.matches("9191-Good to see  you: ([\\w])*")) {

           return ("9191-Hello");
        }


         return "9191-Sorry I don't understand you";
    }

    public static void main(String args[]){

        System.out.println(getAnswer("9191-My Name is Vasia"));


//        1800-0770-77


    }
}
