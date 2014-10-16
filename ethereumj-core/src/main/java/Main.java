import edu.emory.mathcs.backport.java.util.Arrays;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.net.message.MessageFactory;
import org.ethereum.net.p2p.P2pHandler;
import org.ethereum.util.RLP;
import org.spongycastle.util.encoders.Hex;

/**
 *
 * @author: Roman Mandeleil
 * Created on: 14/10/2014 14:39
 */

public class Main {

    public static void main(String[] args) {

        Ethereum eth = new EthereumImpl();
        eth.connect("localhost", 30303);

//        new P2pHandler().adaptMessageIds( Arrays.asList(new String[]{"eth", "shh"}) );

//        byte[] encoded = Hex.decode("c111");
//        MessageFactory.createMessage(encoded);




    }
}
