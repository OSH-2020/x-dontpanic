import database.DeviceItem;

import java.io.IOException;

public class DFS_server {
	
	private static final int controlPort=2333;
	//private static final int dataPort=58280;
	
	public static void main(String[] args) {

     	System.out.println("Server start");
		database.Query query = new database.Query();
		DeviceItem[] devices =query.queryOnlineDevice();
		for (DeviceItem device : devices) {
			device.setIsOnline(false);
			query.alterDevice(device);
			//System.out.println(device.getId());
		}
      	try{
	        Thread t = new controlConnect.ServerThread(controlPort);
	        t.start();
      	}catch(IOException e){
      		e.printStackTrace();
      	}   

		/*
      	try{
	        Thread t = new dataConnect.ServerThread(dataPort);
	        t.start();
      	}catch(IOException e){
      		e.printStackTrace();
      	}*/
	}
}