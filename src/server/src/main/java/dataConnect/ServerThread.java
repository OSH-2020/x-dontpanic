package dataConnect;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {

	private ServerSocket server;

	public ServerThread(int port) throws IOException {
		server = new ServerSocket(port);
		System.out.println("data socket setup!");
	}

	public void run() {
		while (true) {
			try {
				Socket socket = server.accept();
				ClientThread thread = new ClientThread(socket);
				thread.start();
				System.out.println("accepted a data link!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}
}