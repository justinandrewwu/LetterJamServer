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

public class Player {
	static final int MARKER = 0x4C54524A;
	static final int MSG_JOIN = 1001;
	static final int MSG_ERROR = 1002;

	static int nullread = 0;
	static int printnullread = 1;

	public SocketChannel socket;
	private ByteBuffer buffer;
	private ByteBuffer wbuf;

	Player(SocketChannel s)
	{
		socket = s;
		buffer = ByteBuffer.allocate(1024);
		wbuf = ByteBuffer.allocate(1024);
	}

	public int handleRead()
	{
		int numread = 0;
		try {
			if (buffer.limit() > 0) {
				System.out.println("before read position = " + buffer.position() + " limit = " + buffer.limit());
			}
			int res = socket.read(buffer);
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
			buffer.flip();
			if (res > 0) {
				System.out.println("read " + res + " bytes position = " + buffer.position() + " limit = " + buffer.limit());
			}
			int avail = buffer.limit() - buffer.position();
			if (avail > 0) {
				System.out.println("avail = "+avail);
			}
			while (avail >= 12) {        // we have to have at least the header to proceed
				int marker = buffer.getInt(buffer.position());
				if (marker != MARKER) {
					closeConnection("Bad Marker detected in Message " + marker);
				}
				int length = buffer.getInt(buffer.position()+4);
				System.out.println("parsed length = "+length);
				if (length > avail) {
					// we don't have the whole message, wait for it
					System.out.println("Not enough data to read a whole message length = "+length+ " avail = "+ avail);
					System.out.println("  position ="+buffer.position()+" limit = "+buffer.limit());
					buffer.compact();
					System.out.println("After compact position ="+buffer.position()+" limit = "+buffer.limit());
					// buffer.flip();
					// System.out.println("After flip ="+buffer.position()+" limit = "+buffer.limit());
					return numread;
				}
				int msgid = buffer.getInt(buffer.position()+8);
				switch (msgid) {
					case MSG_JOIN: {
						String name = decodeString(length-12, 12);
						System.out.println("Received MSG_JOIN " + name);
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
				avail = buffer.limit() - buffer.position();
				System.out.println("avail = "+avail);
			}
			if (avail == 0) {
				buffer.clear();
			} else {
				buffer.compact();
			}
			return numread;

		} catch (Exception e) {
			System.out.println("handleRead Exception: " + e);
			e.printStackTrace();
			return -1;
		}
	}

	//  decode the string from the read buffer
	//  position in the buffer will change to the end of the string
	public String decodeString(int len, int index)
	{
		byte arr[] = new byte[len];
		buffer.position(buffer.position()+index);
		buffer.get(arr, 0, len);
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
		// free buffers?
		try {
			socket.close();
		} catch (Exception e) {
			System.out.println("socket close Exception: " + e);
			e.printStackTrace();
		}
		return 0;

	}
}
