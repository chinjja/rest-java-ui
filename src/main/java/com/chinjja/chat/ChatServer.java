package com.chinjja.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
	private SocketAddress address;
	private ServerSocket server;
	private Accepter accepter;
	private Map<String, Handler> handlers = new HashMap<>();
	
	public void setSocketAddress(SocketAddress address) {
		this.address = address;
	}
	
	public SocketAddress getSocketAddress() {
		return address;
	}
	
	public void open() throws IOException {
		if(isOpen()) return;
		
		server = new ServerSocket();
		server.bind(address);
		
		accepter = new Accepter();
		accepter.start();
		
		System.out.println("open");
	}
	
	public void close() {
		if(!isOpen()) return;
		
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		server = null;
		
		System.out.println("close");
	}
	
	public boolean isOpen() {
		return server != null;
	}
	
	class Accepter extends Thread {
		@Override
		public void run() {
			try {
				while(server != null) {
					System.out.println("accepting...");
					Socket socket = server.accept();
					System.out.println("accepted");
					Handler handler = new Handler(socket);
					handler.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				System.out.println("accepter stopped");
			}
		}
	}
	
	class Handler extends Thread {
		Socket socket;
		InputStream is;
		OutputStream os;
		byte[] buffer = new byte[2048];
		boolean closing;
		String id;
		
		Handler(Socket socket) throws IOException {
			if(socket == null) throw new NullPointerException();
			this.socket = socket;
			is = socket.getInputStream();
			os = socket.getOutputStream();
		}
		
		@Override
		public void run() {
			try {
				int len = is.read(buffer);
				if(len == -1) {
					System.out.println(socket + " no more data");
					return;
				}
				id = new String(buffer, 0, len);
				
				if(handlers.containsKey(id)) {
					System.out.println("already contains id: " + id);
					os.write("not-allow".getBytes());
					os.flush();
					return;
				} else {
					println("connected");
					handlers.put(id, this);
					os.write("allow".getBytes());
					os.flush();
				}
				while(true) {
					len = is.read(buffer);
					if(len == -1) {
						System.out.println("-1");
						return;
					}
					String recvData = new String(buffer, 0, len);
					println(recvData + " ["+recvData.length()+"]");
					
					os.write(buffer, 0, len);
					os.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				handlers.remove(id);
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				println("handler closed");
			}
		}
		
		private void println(String msg) {
			System.out.println("["+id+"] " + msg);
		}
	}
	
	public static void main(String[] args) throws IOException {
		ChatServer server = new ChatServer();
		server.setSocketAddress(new InetSocketAddress("localhost", 7879));
		server.open();
	}
}
