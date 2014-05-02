package  samples.niotut;

import org.ethereum.net.RLP;
import org.ethereum.util.Utils;

import java.util.LinkedList;
import java.util.Queue;

public class RspHandler {

    public Queue<byte[]> packetsRecived = new LinkedList<byte[]>();

	
	public synchronized boolean handleResponse(byte[] rsp) {

        packetsRecived.add(rsp);
		this.notify();
		return true;
	}
	
	public synchronized void waitForResponse() {


        while(packetsRecived.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }

        while (!packetsRecived.isEmpty()) {

            byte[] rsp = packetsRecived.remove();
            Utils.printHexStringForByteArray(rsp);


            if (rsp.length < 9){
                // Can't be any ether packet
            }

            boolean noMoreMessages = false;


            // 22 40 08 91 - magic packet
            if ((rsp[0] & 0xFF) == 0x22  &&
                (rsp[1] & 0xFF) == 0x40  &&
                (rsp[2] & 0xFF) == 0x08  &&
                (rsp[3] & 0xFF) == 0x91 ){


                // Got ethereum message
            }


            int numMessages = 0;
            // count number of messages in the packet
            for (int i = 0; i < rsp.length - 3; ++i){

                if (    (rsp[i + 0] & 0xFF) == 0x22  &&
                        (rsp[i + 1] & 0xFF) == 0x40  &&
                        (rsp[i + 2] & 0xFF) == 0x08  &&
                        (rsp[i + 3] & 0xFF) == 0x91 ){

                    ++numMessages;
                }
            }

            System.out.println("This packet contains: " + numMessages + " messages");

            for (int i = 0; i < numMessages; ++i){

                // Callc message length
                int messageLength = ((rsp[4]  & 0xFF) << 24) +
                                    ((rsp[5]  & 0xFF) << 16) +
                                    ((rsp[6]  & 0xFF) << 8) +
                                    ((rsp[7]  & 0xFF));

                byte[] msgPayload = new byte[messageLength];

                System.out.println("payload size: " + msgPayload.length );
                System.out.println("packet  size: " + messageLength );
                System.arraycopy(rsp, 8, msgPayload, 0, messageLength);

                Utils.printHexStringForByteArray(msgPayload);




                Queue<Integer> index = new LinkedList<Integer>();

                RLP.fullTraverse(msgPayload, 0, 0, msgPayload.length, 1, index);

//                Message msg = MessageFactory.createMessage(msgPayload, index);
//                System.out.println("msg: " + msg);

                // shift next message to the start of the packet array
                if (i + 1 < numMessages){

                    System.arraycopy(rsp, msgPayload.length + 8, rsp, 0, rsp.length - msgPayload.length - 8);

                }
            }

            System.out.println();
        }
    }
}
