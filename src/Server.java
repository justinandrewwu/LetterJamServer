import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

	private static Selector selector = null;
	private static Player myplayer = null;

	public static void main(String[] args) {

		try {
			selector = Selector.open();
			// We have to set connection host, port and non-blocking mode
			ServerSocketChannel socket = ServerSocketChannel.open();
			ServerSocket serverSocket = socket.socket();
			serverSocket.bind(new InetSocketAddress("localhost", 8089));
			socket.configureBlocking(false);
			int ops = socket.validOps();
			socket.register(selector, ops, null);
			while (true) {
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> i = selectedKeys.iterator();

				while (i.hasNext()) {
					SelectionKey key = i.next();

					if (key.isAcceptable()) {
						//New client has been accepted
						handleAccept(socket, key);
					} else if (key.isReadable()) {
						// We can run non-blocking operation READ on our client
						handleRead(key);
					}
					i.remove();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleAccept(ServerSocketChannel mySocket,
			SelectionKey key) throws IOException {

		System.out.println("Connection Accepted...");

		// Accept the connection and set non-blocking mode
		SocketChannel client = mySocket.accept();
		client.configureBlocking(false);

		// Register that client is reading this channel
		client.register(selector, SelectionKey.OP_READ);
		myplayer = new Player(client);
	}

	private static void handleRead(SelectionKey key)
			throws IOException {
		// System.out.println("Reading...");
		// create a ServerSocketChannel to read the request
		SocketChannel client = (SocketChannel) key.channel();
		if (myplayer != null) {
			if (client != myplayer.socket) {
				System.out.println("player socket doesn't match");
				System.exit(0);
			}

			int res = myplayer.handleRead();
			if (res < 0) {
				// player has disconnected, clean up
				// close for now
				myplayer = null;
				// System.exit(0);
			}
		}

		//		// Create buffer to read data
		//		ByteBuffer buffer = ByteBuffer.allocate(1024);
		//		int res = client.read(buffer);
		//		System.out.println("client.read " + res);
		//		System.out.println("buffer remaining "+ buffer.remaining());
		//		System.out.println("buffer limit "+ buffer.limit());
		//		System.out.println("buffer position "+ buffer.position());
		//		if (res < 0)
		//		{
		//			client.close();
		//			System.out.println("Connection closed...");
		//		}
		//		else
		//		{
		//			buffer.flip();
		//			System.out.println("buffer remaining "+ buffer.remaining());
		//			System.out.println("buffer limit "+ buffer.limit());
		//			System.out.println("buffer position "+ buffer.position());
		//			int s = buffer.getInt();
		//			System.out.println(s);
		//			System.out.println("buffer remaining "+ buffer.remaining());
		//			System.out.println("buffer limit "+ buffer.limit());
		//			System.out.println("buffer position "+ buffer.position());
		//		}
	}
}

