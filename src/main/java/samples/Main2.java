package samples;

import org.ethereum.net.client.ClientPeer;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 10/04/14 12:50
 */
public class Main2 {


    public static void main(String args[]){

//        66.49.191.123
//        54.204.10.41
//        130.88.0.226
//        85.65.126.45
//        54.72.31.55

//        new ClientPeer().connect("66.49.191.123", 30303);
//        new ClientPeer().connect("66.49.191.123", 30303);

//        new ClientPeer().connect("54.72.31.55", 30303);

        String ip_1 = "131.104.252.4";
        String ip_2 = "107.170.57.247";
        String ip_3 = "68.48.173.163";
        String ip_4 = "86.183.231.205";
        String ip_5 = "68.185.234.64";
        String ip_6 = "207.219.69.154";




//        new ClientPeer().connect("192.168.1.102", 30303);


//        new ClientPeer().connect("83.172.226.79", 30303);
//       new ClientPeer().connect("68.48.173.163", 31313);
//        new ClientPeer().connect("86.150.41.127", 30303);

//        new ClientPeer().connect("82.217.72.169", 30303); nicksavers
//        new ClientPeer().connect("94.197.120.80", 30303); stephan (ursium)


//        new ClientPeer().connect("54.72.31.55", 30303);     // peer discovery: capability = 4

        new ClientPeer().connect("54.201.28.117", 30303); // poc-5
//        new ClientPeer().connect("94.210.200.192", 30303); // poc-5
//        new ClientPeer().connect("62.78.198.208", 30303); // poc-5 not stable



    }
}

/*   POC - 5

        Hello:  [/206.223.168.190] Canada
        Hello:  [/94.210.200.192] Netherlands
        Hello:  [/88.69.198.198] Germany
        Hello:  [/24.157.83.122] Canada
        Hello:  [/71.202.162.40] United States
        Hello:  [/64.231.10.208] Canada
        Hello:  [/85.65.126.45] Israel
        Hello:  [/62.78.198.208] Finland
        Hello:  [/50.133.12.228] United States
        Hello:  [/77.166.77.107] Netherlands
        Hello:  [/110.77.217.185] Thailand
        Hello:  [/64.231.9.30] Canada
        Hello:  [/213.100.250.57] Sweden
        Hello:  [/162.243.203.121] United States
        Hello:  [/82.217.72.169] Netherlands
        Hello:  [/99.231.80.166] Canada
        Hello:  [/131.104.252.4] Canada
        Hello:  [/54.204.10.41] United States
        Hello:  [/54.201.28.117] United States
        Hello:  [/67.204.1.162] Canada
        Hello:  [/82.240.16.5] France
        Hello:  [/74.79.23.119] United States
*/


