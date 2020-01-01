package com.chinjja.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ChatServer {
	public static String ENCORDING = "UTF-8";
	
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
		Date connect_time;
		Date disconnect_time;
		
		Handler(Socket socket) throws IOException {
			if(socket == null) throw new NullPointerException();
			this.socket = socket;
			is = socket.getInputStream();
			os = socket.getOutputStream();
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					int len = is.read(buffer);
					if(len == -1) {
						return;
					}
					String recvData = new String(buffer, 0, len);
					process(recvData);
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
				println("handler closed " + new Date());
			}
		}
		
		private void println(String msg) {
			System.out.println("["+id+"] " + msg);
		}
		
		private void validate(JsonObject o) {
			switch(o.get("type").getAsString()) {
			case "connect":
				if(id != null) throw new IllegalArgumentException();
				break;
			default:
				if(id == null) throw new IllegalArgumentException();
				break;
			}
		}
		
		private void process(String data) throws IOException {
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(data);
			
			if(json.isJsonObject()) {
				JsonObject o = json.getAsJsonObject();
				validate(o);
				
				switch(o.get("type").getAsString()) {
				case "send_to":
					send_to(o);
					break;
				case "connect":
					connect(o);
					break;
				case "disconnect":
					disconnect(o);
					break;
				case "create_user":
				case "delete_user":
				case "create_room":
				case "delete_room":
				case "get_room":
				case "create_friend":
				case "delete_friend":
				case "get_friend":
				default:
					throw new IllegalStateException();
				}
			} else {
				os.write(json.toString().getBytes(ENCORDING));
			}
		}
		
		private void send_to(JsonObject json) throws IOException {
			JsonArray to_list = json.get("to").getAsJsonArray();
			String msg = json.get("msg").getAsString();
			JsonObject d = new JsonObject();
			d.addProperty("type", "receive_from");
			d.addProperty("from", id);
			d.addProperty("msg", msg);
			d.addProperty("date", System.currentTimeMillis());
			byte[] json_data = d.toString().getBytes(ENCORDING);
			JsonArray to_success = new JsonArray();
			for(int i = 0; i < to_list.size(); i++) {
				String to_id = to_list.get(i).getAsString();
				if(id.equals(to_id)) continue;
				Handler to_handler = handlers.get(to_id);
				if(to_handler != null) {
					to_success.add(to_list.get(i));
					to_handler.os.write(json_data);
				}
			}
			json.remove("to");
			json.add("to", to_success);
			write(json, null);
		}
		
		private void connect(JsonObject json) throws IOException {
			String id = json.get("id").getAsString();
			
			if(handlers.containsKey(id)) {
				write(json, "already exists id");
				System.out.println("already contains id: " + id);
			} else {
				handlers.put(id, this);
				this.id = id;
				connect_time = new Date(json.get("date").getAsLong());
				disconnect_time = null;
				write(json, null);
				println("connected " + connect_time);
			}
		}
		
		private void disconnect(JsonObject json) throws IOException {
			disconnect_time = new Date(json.get("date").getAsLong());
			write(json, null);
			println("disconnected " + disconnect_time);
		}
		
		private void acknowled(JsonObject json, String reason) {
			if(reason == null) {
				json.addProperty("acknowled", "ok");
			} else {
				json.addProperty("acknowled", "nok");
				json.addProperty("reason", reason);
			}
		}
		
		private void write(JsonObject json, String reason) throws IOException {
			acknowled(json, reason);
			os.write(json.toString().getBytes(ENCORDING));
		}
	}
	
	public static void main(String[] args) throws IOException {
		ChatServer server = new ChatServer();
		server.setSocketAddress(new InetSocketAddress("localhost", 7879));
		server.open();
	}
}
