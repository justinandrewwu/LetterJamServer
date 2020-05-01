import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server {

	private Selector selector = null;
	private Player[] seats;
	private int numSeated = 0;
	private static final String LJLETTERS = "AAAABBCCCDDDEEEEEEFFGGHHHIIIIKKLLLMMNNOOOOPPRRRRSSSSTTTTUUUWWYY";
	String[] playerLetters;

	public Server() {
		seats = new Player[6];
		for (int i = 0; i < seats.length; i++)
		{
			seats[i] = null;
		}
	}

	public static void main(String[] args) {
		Server mainServer = new Server();
		mainServer.mainLoop();
	}

	public void mainLoop() {

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

	private void handleAccept(ServerSocketChannel mySocket,
			SelectionKey key) throws IOException {

		System.out.println("Connection Accepted...");

		// Accept the connection and set non-blocking mode
		SocketChannel client = mySocket.accept();
		client.configureBlocking(false);

		// Register that client is reading this channel
		client.register(selector, SelectionKey.OP_READ);
		Player myplayer = new Player(client,this);

		for (int i = 0; i < seats.length; i++)
		{
			if (seats[i] == null) {
				seats[i] = myplayer;
				numSeated++;
				break;
			}
			else if (seats[i] == myplayer) {
				break;
			}
		}
	}

	private void handleRead(SelectionKey key)
			throws IOException {
		// System.out.println("Reading...");
		// create a ServerSocketChannel to read the request
		SocketChannel client = (SocketChannel) key.channel();
		Player myplayer = null;

		for (int i = 0; i < seats.length; i++)
		{
			if (seats[i].socket == client) {
				myplayer = seats[i];
				int res = myplayer.handleRead();
				if (res < 0) {
					// player has disconnected, clean up
					// close for now
					myplayer = null;
					// System.exit(0);
				}
				break;
			}
		}
		if (myplayer == null) {
			System.out.println("No matching client found");
			System.exit(0);
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

	public void processJoinMessage() {
		int numberOfPlayersExpected = 2; //TODO: change to serverwide
		if (numSeated >= numberOfPlayersExpected) {
			startGame(numSeated);
		}
	}

	private void startGame(int numPlayers) {
		playerLetters = splitStringEvenly(LJLETTERS,numPlayers);
		for (int i = 0; i < seats.length; i++)
		{
			if (seats[i] != null) {
				seats[i].startGame(playerLetters[i]);
			}
		}
	}

	private static String[] splitStringEvenly(String letters, int num) {
		String[] res = new String[num];
		int place = 0;
		letters = shuffle(letters);
		for (int i = letters.length()-1; i >= 0; i--) {
			if (res[place] == null) {
				res[place] = "";
			}
			res[place] = res[place] + letters.substring(i,i+1);
			place++;
			if (place == num) {
				place = 0;
			}
		}
		return res;
	}

	public static String shuffle(String input){
		List<Character> characters = new ArrayList<Character>();
		for(char c:input.toCharArray()){
			characters.add(c);
		}
		StringBuilder output = new StringBuilder(input.length());
		while(characters.size()!=0){
			int randPicker = (int)(Math.random()*characters.size());
			output.append(characters.remove(randPicker));
		}
		return output.toString();
	}
}

