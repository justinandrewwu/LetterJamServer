import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Player {
	private static final int MARKER = 0x4C54524A;
	private static final int MSG_JOIN = 1001;
	private static final int MSG_MORE_LETTERS = 1002;
	private static final int MSG_MY_WORD = 1003;
	private static final int MSG_SUGGEST = 1004;
	private static final int MSG_VOTE = 1005;
	private static final int MSG_CHOSEN_CLUE = 1006;
	private static final int MSG_GIVE_CLUE = 1007;
	private static final int MSG_PLAYER_DECIDES = 1008;
	private static final int MSG_LETTER_GUESS = 1009;

	private static final int MSG_USER_JOIN = 2001;
	private static final int MSG_PICK_WORD = 2002;
	private static final int MSG_PLAYER_READY = 2003;
	private static final int MSG_TABLE_READY = 2004;
	private static final int MSG_VALID_CLUE = 2005;
	private static final int MSG_VOTED = 2006;
	private static final int MSG_SEND_CLUE = 2007;
	private static final int MSG_ANAGRAM = 2008;
	private static final int MSG_NUMBER_OF_LETTERS_RIGHT = 2009;

	private static final int MSG_ERROR = 3001;

	private static int nullread = 0;
	private static int printnullread = 1;

	public SocketChannel socket;
	private ByteBuffer rbuf;
	private ByteBuffer wbuf;

	Player(SocketChannel s)
	{
		socket = s;
		rbuf = ByteBuffer.allocate(1024);
		wbuf = ByteBuffer.allocate(1024);
	}

	public int handleRead()
	{
		int numread = 0;
		try {
			if (rbuf.limit() > 0) {
				System.out.println("before read position = " + rbuf.position() + " limit = " + rbuf.limit());
			}
			int res = socket.read(rbuf);
			if (res < 0) {
				System.out.println("Client disconnected");
				socket.close();
				return -1;
			}
			if (res == 0) {
				nullread++;
				if (nullread > printnullread) {
					System.out.println("Number of null reads = " + nullread);
					printnullread *= 10;
				}
			}
			rbuf.flip();
			if (res > 0) {
				System.out.println("read " + res + " bytes position = " + rbuf.position() + " limit = " + rbuf.limit());
			}
			int avail = rbuf.limit() - rbuf.position();
			if (avail > 0) {
				System.out.println("avail = "+avail);
			}
			while (avail >= 12) {        // we have to have at least the header to proceed
				int marker = rbuf.getInt(rbuf.position());
				if (marker != MARKER) {
					closeConnection("Bad Marker detected in Message " + marker);
				}
				int length = rbuf.getInt(rbuf.position()+4);
				System.out.println("parsed length = "+length);
				if (length > avail) {
					// we don't have the whole message, wait for it
					System.out.println("Not enough data to read a whole message length = "+length+ " avail = "+ avail);
					System.out.println("  position ="+rbuf.position()+" limit = "+rbuf.limit());
					rbuf.compact();
					System.out.println("After compact position ="+rbuf.position()+" limit = "+rbuf.limit());
					// rbuf.flip();
					// System.out.println("After flip ="+rbuf.position()+" limit = "+rbuf.limit());
					return numread;
				}
				int msgid = rbuf.getInt(rbuf.position()+8);
				switch (msgid) {
					case MSG_JOIN: {
						String name = decodeString(length-12, 12);
						System.out.println("Received MSG_JOIN " + name);
						processJoinMessage(name);
						break;
					}
					case MSG_MORE_LETTERS: {
						System.out.println("Received MSG_MORE_LETTERS");
						break;
					}
					case MSG_MY_WORD: {
						System.out.println("Received MSG_MY_WORD");
						break;
					}
					case MSG_SUGGEST: {
						System.out.println("Received MSG_SUGGEST");
						break;
					}
					case MSG_VOTE: {
						System.out.println("Received MSG_VOTE");
						break;
					}
					case MSG_CHOSEN_CLUE: {
						System.out.println("Received MSG_CHOSEN_CLUE");
						break;
					}
					case MSG_GIVE_CLUE: {
						System.out.println("Received MSG_GIVE_CLUE");
						break;
					}
					case MSG_PLAYER_DECIDES: {
						System.out.println("Received MSG_PLAYER_DECIDES");
						break;
					}
					case MSG_LETTER_GUESS: {
						System.out.println("Received MSG_LETTER_GUESS");
						break;
					}
					case MSG_ERROR: {
						String str = decodeString(length-12, 12);
						System.out.println("Received MSG_ERROR " + str);
						break;
					}
					default: {
						// Unknown message
						closeConnection("Unknown message id "+ msgid);
						return -1;
					}
				}
				numread++;
				avail = rbuf.limit() - rbuf.position();
				System.out.println("avail = "+avail);
			}
			if (avail == 0) {
				rbuf.clear();
			} else {
				rbuf.compact();
			}
			return numread;

		} catch (Exception e) {
			System.out.println("handleRead Exception: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	private void processJoinMessage(String name) {
		sendStringMsg(wbuf,MSG_USER_JOIN,name);
	}

	public int sendStringMsg(ByteBuffer rbuf,int msgid, String str)
	{
		try {
			int length = str.length() + 12;
			System.out.println("Length = "+length);
			rbuf.clear();
			rbuf.putInt(MARKER);
			rbuf.putInt(length);
			rbuf.putInt(msgid);
			rbuf.put(str.getBytes());
			rbuf.flip();
			int bytes = socket.write(rbuf);
			System.out.println("bytes written "+bytes);
			return bytes;
		} catch (IOException e) {
			System.out.println("sendJoin Exception: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	//  decode the string from the read rbuf
	//  position in the rbuf will change to the end of the string
	public String decodeString(int len, int index)
	{
		byte arr[] = new byte[len];
		rbuf.position(rbuf.position()+index);
		rbuf.get(arr, 0, len);
		return new String(arr);

	}

	public int sendStringMsg(int msgid, String str)
	{
		try {
			int length = str.length() + 12;
			wbuf.reset();
			wbuf.putInt(MARKER);
			wbuf.putInt(length);
			wbuf.putInt(msgid);
			wbuf.put(str.getBytes());
			wbuf.flip();
			int bytes = socket.write(wbuf);
			return bytes;
		} catch (IOException e) {
			System.out.println("sendJoin Exception: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	public int closeConnection(String str)
	{
		if (str != null) {
			System.out.println("closing Connection: "+str);
			sendStringMsg(MSG_ERROR, str);
		}
		// free rbufs?
		try {
			socket.close();
		} catch (Exception e) {
			System.out.println("socket close Exception: " + e);
			e.printStackTrace();
		}
		return 0;
	}
}
