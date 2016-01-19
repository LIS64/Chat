package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class ChatServer implements Runnable {
	private final static int SERVER_PORT = 45450;
	private  Selector selector;
	private  ServerSocketChannel ssc;
	private byte[] buffer = new byte[2048];
    private CharBuffer charBuffer = CharBuffer.allocate(2048);
    private Charset ch = Charset.forName("UTF-8");
    private CharsetDecoder decoder = ch.newDecoder();
	private Map<SelectionKey, ByteBuffer> connections = new HashMap<>();
	
	
	public ChatServer(int port) {
		// TODO Auto-generated constructor stub
		try {
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.socket().bind(new InetSocketAddress(port));
			selector = Selector.open();
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("Сервер чата запущен на порту: "+port);
		} catch (IOException e) {
			System.out.println("Ошибка запуска сервера");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	

	public static void main(String[] args) {
		System.out.println("Для запуска сервера введите start. Для отановки сервера введите stop");
		Scanner in = new Scanner(System.in);
		boolean isExit=false;
		ChatServer chat =null;
		String inCommand;
		while (!isExit){
			inCommand=in.nextLine();
			if ("start".equals(inCommand)){
				chat = new ChatServer(SERVER_PORT);
				  new Thread(chat).start();
				} else if ("stop".equals(inCommand)){
				 System.out.println("Сервер остановлен.");
				 in.close();
				 isExit=true;
				 System.exit(0);
			} else {
				System.out.println("Неверная команда");
			}

		}
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				if (selector.isOpen()){
					selector.select();
					Set<SelectionKey> keys = selector.selectedKeys();
					for (SelectionKey key : keys){
						if (!key.isValid()){
							continue;
						}
						if (key.isAcceptable()){
							ServerSocketChannel ssc2 = (ServerSocketChannel) key.channel();
							SocketChannel channel = ssc2.accept();
							channel.configureBlocking(false);
							SelectionKey keyAcc = channel.register(selector,SelectionKey.OP_READ);
							ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
							connections.put(keyAcc, byteBuffer);
						} else if (key.isReadable()){
							
							SocketChannel channel = (SocketChannel) key.channel();
							int read;
							ByteBuffer byteBuffer = connections.get(key);
							byteBuffer.clear();
							try {
								read = channel.read(byteBuffer);
							} catch (IOException e) {
								// TODO: handle exception
								e.printStackTrace();
								closeChannel(key);
								break;
							}
							
							if (read==-1){
								closeChannel(key);
								break;
							} else if (read>0){
								byteBuffer.flip();
								byteBuffer.mark();
								insertMessagesToBD(decodeMessages(read, byteBuffer));
								byteBuffer.reset();
								int position = byteBuffer.position();
								int limit = byteBuffer.limit();
								Set<Map.Entry<SelectionKey, ByteBuffer>> users = connections.entrySet();
								for (Map.Entry<SelectionKey, ByteBuffer> user : users ){
									SelectionKey selectionKey  = user.getKey();
									selectionKey.interestOps(SelectionKey.OP_WRITE);
									ByteBuffer userByffer = user.getValue();
									userByffer.position(position);
									userByffer.limit(limit);
								}
								
							}
						} else if (key.isWritable()){
							ByteBuffer buff = connections.get(key);
							SocketChannel channel = (SocketChannel) key.channel();
							try {
								int result = channel.write(buff);
								if (result==-1){
									closeChannel(key);
								}
							} catch (IOException e) {
								// TODO: handle exception
								e.printStackTrace();
								closeChannel(key);

							}
							if (buff.position() == buff.limit()){
								key.interestOps(SelectionKey.OP_READ);
							}
						}
					}
						keys.clear();
				} else break;
				
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	
	
	   private void closeChannel(SelectionKey key) throws IOException {
	        connections.remove(key); 
	        SocketChannel socketChannel = (SocketChannel)key.channel();
	        if (socketChannel.isConnected()) {
	            socketChannel.close(); 
	        }
	        key.cancel();
	    }
	   
	   private void insertMessagesToBD(String str){
		   MessageClient mc = new MessageClient();
		   String[] s = str.split(" ");
		   mc.setDate(s[0]);
		   mc.setNameUser(s[1]);
		   mc.setMessage(s[2]);
		   ChatDAOImpl chat = new ChatDAOImpl();
		   chat.insertMessages(mc);
	   }
	   
	    private String decodeMessages(int read, ByteBuffer byteBuffer) {
	    	charBuffer.clear(); 
	        decoder.decode(byteBuffer, charBuffer, false);
	        charBuffer.flip();
	       
	        return charBuffer.toString();
	    }
	   
	    private synchronized void stopServer() {
	        Set<SelectionKey> users = connections.keySet();
	        for (SelectionKey user:users) {
	            SocketChannel s = (SocketChannel)user.channel();
	            if (s.isConnected()) {
	                try {
	                    s.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        if (ssc.isOpen()) {
	            try {
	                ssc.close();
	                selector.close();
	            } catch (IOException ignored) {}
	        }
	    }
}
