package controlConnect;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

class ClientThread extends Thread {
	private Socket clientsocket;
	private BufferedReader inFromClient = null;
	private DataOutputStream outToClient = null;
	private int clientId=-1;

	public ClientThread(Socket socket) {
		this.clientsocket = socket;
	}

	public void run() {

		System.out.println("start!");
		try {
			clientsocket.setKeepAlive(true);
			// if debug, delete next line
			clientsocket.setSoTimeout(60000);
			inFromClient = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
			outToClient = new DataOutputStream(clientsocket.getOutputStream());

			while (true) {
					String sentence = inFromClient.readLine();// 对得到的流进行处理
					if (readsentence(sentence) == 0)
						break;
					System.out.println("C-RECV: " + sentence);
			}	
			
		} catch (TimeoutException e) {
			System.out.println("C-times out");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			inFromClient.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			clientsocket.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			outToClient.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if (clientId!=-1){
			try {		
				database.Query query=new database.Query();
				database.DeviceItem deviceitem;
				deviceitem = query.queryDevice(clientId);
				deviceitem.setIsOnline(false);
				query.alterDevice(deviceitem);
				query.closeConnection();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		System.out.println("C-client thread ended");
	}

	private int readsentence(String sentence) throws Exception {

		if (sentence.charAt(0) == '1') {
			String s[], ip;
			s = sentence.split(" ");

			int id = Integer.parseInt(s[1]);
			
			if (clientId!=-1 && clientId!=id){
				outToClient.writeBytes("Error!\n");
				outToClient.flush();
				return 0;
			}
			
			//int port = clientsocket.getPort();
			int rs = Integer.parseInt(s[2]);
			//ip = clientsocket.getInetAddress().getHostAddress();

			database.Query query = new database.Query();
			database.DeviceItem deviceitem;
			deviceitem = query.queryDevice(id);
			if (deviceitem == null) {
				// 不允许通过报文新建client
				System.out.println("No such device ID!");
				return 0;
			} else {
				clientId = id;
				//deviceitem.setIp(ip);
				//deviceitem.setPort(port);
				deviceitem.setIsOnline(true);
				deviceitem.setRs(rs);
				query.alterDevice(deviceitem);
			}

			outToClient.writeBytes(String.format("received with %d unread request!\n", query.queryRequestNumbers(id)));
			outToClient.flush();

			query.closeConnection();
			return 1;
		} else if (sentence.charAt(0) == '2') {
			String s[];
			s = sentence.split(" ");

			int id = Integer.parseInt(s[1]);
			
			if (clientId!=-1 && clientId!=id){
				outToClient.writeBytes("Error!\n");
				outToClient.flush();
				return 0;
			}

			database.Query query = new database.Query();
			database.RequestItem request;
			request = query.queryFirstRequest(id);
			query.closeConnection();

			if (request == null) {
				outToClient.writeBytes("ERROR!\n");
				outToClient.flush();
				return 0;
			} else {
				outToClient.writeBytes(
						String.format("%d %d %d\n", request.getId(), request.getFragmentId(), request.getType()));
				outToClient.flush();
			}
			return 1;
		}else if (sentence.charAt(0) == '3') {
			String s[];
			s = sentence.split(" ");

			int id = Integer.parseInt(s[1]);

			if (clientId!=-1 && clientId!=id){
				outToClient.writeBytes("Error!\n");
				outToClient.flush();
				return 0;
			}

			String ip = s[2];
			int port = Integer.parseInt(s[3]);

			database.Query query = new database.Query();
			database.DeviceItem deviceitem;
			deviceitem = query.queryDevice(id);
			if (deviceitem == null) {
				// 不允许通过报文新建client
				System.out.println("No such device ID!");
				return 0;
			} else {
				clientId = id;
				deviceitem.setIp(ip);
				deviceitem.setPort(port);
				//deviceitem.setIsOnline(true);
				//deviceitem.setRs(rs);
				query.alterDevice(deviceitem);
			}

			outToClient.writeBytes(String.format("set ip=%s, port=%d successfully\n", ip,port));
			outToClient.flush();

			query.closeConnection();
			return 1;
		}
		return 0;
	}

}