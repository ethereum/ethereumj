package  samples.niotut;


/* BASED ON: http://rox-xmlrpc.sourceforge.net/niotut/#The client */

import org.ethereum.util.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

public class NioClient implements Runnable {
	// The host:port combination to connect to
	private InetAddress hostAddress;
	private int port;

	// The selector we'll be monitoring
	private Selector selector;

	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	// A list of PendingChange instances
	private List pendingChanges = new LinkedList();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map pendingData = new HashMap();
	
	// Maps a SocketChannel to a RspHandler
	private Map rspHandlers = Collections.synchronizedMap(new HashMap());

    public SocketChannel socket;
    public RspHandler handler;


    public static final String helloString = "22 40 08 91 00 00 00 79 F8 77 80 0B 80 AD 45 74 " +
            "68 65 72 65 75 6D 28 2B 2B 29 2F 5A 65 72 6F 47 " +
            "6F 78 2F 76 30 2E 34 2E 31 2F 6E 63 75 72 73 65 " +
            "73 2F 4C 69 6E 75 78 2F 67 2B 2B 07 82 76 5F B8 " +
            "40 D8 D6 0C 25 80 FA 79 5C FC 03 13 EF DE BA 86 " +
            "9D 21 94 E7 9E 7C B2 B5 22 F7 82 FF A0 39 2C BB " +
            "AB 8D 1B AC 30 12 08 B1 37 E0 DE 49 98 33 4F 3B " +
            "CF 73 FA 11 7E F2 13 F8 74 17 08 9F EA F8 4C 21 " +
            "B0 ";

    public static final String pingString = "22 40 08 91 00 00 00 02 C1 02 ";
    public static final String pongString = "22 40 08 91 00 00 00 02 C1 03 ";

    public static final String getPeersString = "22 40 08 91 00 00 00 02 C1 10 ";

    public static final String getTransactions = "22 40 08 91 00 00 00 02 C1 16 ";

    public static final String getChain = "22 40 08 91 00 00 00 26 F8 24 14 " +
            "AB 6B 9A 56 13 97 0F AA 77 1B 12 D4 49 B2 E9 BB 92 5A B7 A3 69 F0 A4 B8 6B 28 6E 9D 54 00 99 CF " +
            "82 01 00 ";

    public static final String getTxString = "22 40 08 91 00 00 00 02 C1 16 ";


	
	public NioClient(InetAddress hostAddress, int port) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();

        // Start a new connection
        this.socket = this.initiateConnection();

        // Register the response handler
        this.handler = new RspHandler();
        this.rspHandlers.put(socket, handler);

	}

	public void send(byte[] data) throws IOException {

		// And queue the data we want written
		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socket);
			if (queue == null) {
				queue = new ArrayList();
				this.pendingData.put(socket, queue);
			}
			queue.add(ByteBuffer.wrap(data));
		}

		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
	}

	public void run() {


		while (true) {
			try {
				// Process any pending changes
				synchronized (this.pendingChanges) {
					Iterator changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();

						switch (change.type) {
                            case ChangeRequest.CHANGEOPS:
                                SelectionKey key = change.socket.keyFor(this.selector);
                                key.interestOps(change.ops);
                                break;
                            case ChangeRequest.REGISTER:
                                change.socket.register(this.selector, change.ops);
                                break;
						}
					}
					this.pendingChanges.clear();
				}

				// Wait for an event one of the registered channels
                // THIS ONE ACTUALLY BLOCKS AND SHOULD AWAKE WHEN SOME I/O HAPPENS
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isConnectable()) {
						this.establishConnection(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {

			numRead = socketChannel.read(this.readBuffer);
            System.out.println("reading: " + numRead);
//            key.interestOps(SelectionKey.OP_WRITE);

		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
//			key.channel().close();
//			key.cancel();


			return;
		}

		// Handle the response


		this.handleResponse(socketChannel, this.readBuffer.array(), numRead);
	}

	private void handleResponse(SocketChannel socketChannel, byte[] data, int numRead) throws IOException {

		// Make a correctly sized copy of the data before handing it
		// to the client
		byte[] rspData = new byte[numRead];
		System.arraycopy(data, 0, rspData, 0, numRead);



        // Look up the handler for this channel
		RspHandler handler = (RspHandler) this.rspHandlers.get(socketChannel);
		
		// And pass the response to it
		if (handler.handleResponse(rspData)) {
			// The handler has seen enough, close the connection
//			socketChannel.close();
//			socketChannel.keyFor(this.selector).cancel();
		}
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socketChannel);


			// Write until there's not more data ...
			while (queue != null && !queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
                byte[] packet = buf.array();

                System.out.print("write: ");
                Utils.printHexStringForByteArray(packet);

				socketChannel.write(buf);

				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}
//            key.interestOps(SelectionKey.OP_READ);

			if (queue == null || queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
                key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	private void establishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
	
		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			// Cancel the channel's registration with our selector
			System.out.println(e);
			key.cancel();
			return;
		}
	
		// Register an interest in writing on this channel
		key.interestOps(SelectionKey.OP_WRITE);
	}

	private SocketChannel initiateConnection() throws IOException {
		// Create a non-blocking socket channel
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
	
		// Kick off connection establishment
		socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));
	
		// Queue a channel registration since the caller is not the 
		// selecting thread. As part of the registration we'll register
		// an interest in connection events. These are raised when a channel
		// is ready to complete connection establishment.
		synchronized(this.pendingChanges) {
			this.pendingChanges.add(new ChangeRequest(socketChannel,
                    ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
		}
		
		return socketChannel;
	}

	private Selector initSelector() throws IOException {
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}

	public static void main(String[] args) {
		try {

			NioClient client = new NioClient(InetAddress.getByName("localhost"), 20202);
			Thread t = new Thread(client, "ClientThread");
			t.setDaemon(true);
			t.start();

//            client.send(new byte[0]);
//            client.handler.waitForResponse();
            Thread.sleep(2000);


            System.out.println("\nsending HELLO");
            client.enableWriting();
//            client.send(hexStringToByteArr(helloString));
            Thread.sleep(100);

            System.out.println("\nsending PONG");
//            client.enableWriting();
//            client.send(hexStringToByteArr(pongString));

            Thread.sleep(100);
            System.out.println("\nsending GETCHAIN");
//            client.enableWriting();
//            client.send(hexStringToByteArr(getChain));




//            System.out.println("\nsending PING");
//            client.send(hexStringToByteArr(pingString));
//            client.handler.waitForResponse();


            System.out.println("SLEEPING");
            Thread.sleep(5000);

            client.handler.waitForResponse();

//            System.out.println("\nsending GETCHAIN");
//            client.send(hexStringToByteArr(getChain));
//            client.handler.waitForResponse();


//            System.out.println("\nsending GETPEERS");
//            client.send(hexStringToByteArr(getPeersString));
//            client.handler.waitForResponse();
//            client.handler.waitForResponse();

//            System.out.println("\nsending GETTRANSACTIONS");
//            client.send(hexStringToByteArr(getTransactions));


            System.out.println("\nsleeping 5 secs before death");


            Thread.sleep(5000);

            client.socket.close();





/*
            client.send(hexStringToByteArr(helloString));
            client.handler.waitForResponse();


            System.out.println("");
            client.send(hexStringToByteArr(getPeersString));
            client.handler.waitForResponse();


            System.out.println("");
            client.handler.waitForResponse();

            System.out.println("");
            client.handler.waitForResponse();
*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    public static byte[]  hexStringToByteArr(String hexString){

        String hexSymbols = "0123456789ABCDEF";

        int arrSize = (int) (hexString.length() / 3);
        byte[] result = new byte[arrSize];

        for (int i = 0; i < arrSize; ++i){

            int digit1 = hexSymbols.indexOf( hexString.charAt(i * 3) );
            int digit2 = hexSymbols.indexOf( hexString.charAt(i * 3 + 1) );

            result[i] = (byte) (digit1 * 16 + digit2);
        }


        return result;
    }


    public void enableWriting(){
        SelectionKey key =  this.socket.keyFor(this.selector);
        key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
    }

    public void enableOnlyReading(){
        SelectionKey key =  this.socket.keyFor(this.selector);
        key.interestOps(SelectionKey.OP_READ);
    }

}
