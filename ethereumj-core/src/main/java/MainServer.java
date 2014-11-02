import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumImpl;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.net.server.PeerServer;

/**
 *
 * @author: Roman Mandeleil
 * Created on: 14/10/2014 14:39
 */

public class MainServer extends EthereumListenerAdapter{

    Ethereum eth;

    public MainServer(Ethereum eth) {
        this.eth = eth;
    }

    public static void main(String[] args) {

        Ethereum eth = new EthereumImpl();
        PeerServer server = new PeerServer();
        server.start(30303);

    }

}
