package com.chinjja.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {
	public static void main(String[] args) throws UnknownHostException, IOException {
		try(Socket socket = new Socket("localhost", 7879)) {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			os.write("chinjja".getBytes());
			os.flush();
			byte[] buffer = new byte[1024];
			int len = is.read(buffer);
			if(len == -1) return;

			System.out.println("echo: " + new String(buffer, 0, len));
			os.write("close".getBytes());
			os.flush();
			socket.shutdownInput();
			socket.shutdownOutput();
		}
	}
}
