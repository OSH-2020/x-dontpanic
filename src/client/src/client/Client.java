package client;

import connect.FragmentManager;
import connect.RequestManager;
import connect.ServerConnector;

import javax.print.attribute.standard.RequestingUserName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class Client {
	private int clientId;
	private File uploadFolders[];
	private String uploadAddrs[];
	private SynItem syn;
	private static int rs = 0;
	private static int selfDataPort;

	private static final String setUpFile = "setup.ini";

	public Client() {
		System.out.println("client start");
	}

	public static void main(String[] args) throws IOException {
		Client client = new Client();
		if (!client.setUp()) {
			System.out.println("ERROR: can not read setup.ini");
			return;
		}
		client.begin();
	}

	public static int getRS() {
		//TODO
		//返回剩余容量,待实现
		return rs;
	}

	private boolean setUp() throws IOException {
		Scanner scanner = null;
		String uploadFolder, fragmentFolder, tmpFragmentFolder;
		int serverControlPort, dataPort, selfDataPort;
		String serverIp;


		try {
			FileInputStream f = new FileInputStream(setUpFile);
			scanner = new Scanner(f);
			serverIp = scanner.nextLine();
			serverControlPort = scanner.nextInt();
			selfDataPort = scanner.nextInt();
			clientId = scanner.nextInt();
			//empty line
			scanner.nextLine();
			fragmentFolder = scanner.nextLine();

			tmpFragmentFolder = scanner.nextLine();
			/*
			int i=scanner.nextInt();
			uploadFolders=new File[i];
			uploadAddrs=new String[i];
				//empty line
				scanner.nextLine();
			for (int j=0;j<i;j++){
				uploadFolder=scanner.nextLine();
				uploadFolders[j]=new File(uploadFolder);
				if (!uploadFolders[j].exists() || !uploadFolders[j].isDirectory())
					throw new Exception();
				uploadAddrs[j]=scanner.nextLine();
			}*/

			scanner.close();
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
			scanner.close();
			return false;
		}

		ServerConnector.init(serverIp, serverControlPort);
		File file = new File(fragmentFolder);
		if (!file.exists() || !file.isDirectory())
			return false;
		//TODO
		connect.FragmentManager.init(file);
		file = new File(tmpFragmentFolder);
		if (!file.exists() || !file.isDirectory())
			return false;
		//fileDetector.FolderScanner.init(file);
		//fileDetector.FileUploader.init(file, serverIp, dataPort);

		rs = 250;
		return true;
	}

	private void begin() throws IOException {

		syn = new SynItem(0);

		ServerConnector serverConnector = new ServerConnector(clientId, syn);
		serverConnector.start();

		RequestManager requestManager = new RequestManager(selfDataPort);
		requestManager.start();

		/*
		fileDetector.FolderScanner folderScanner=new fileDetector.FolderScanner(
				uploadFolders, uploadAddrs, syn);
		
		folderScanner.start();*/

		syn.waitChange(0);

		if (syn.getStatus() == 1) {
			System.out.println("Err: can not connect to server");
		} else if (syn.getStatus() == 2) {
			System.out.println("Err: can not detect files");
		}
		//folderScanner.stopDetecting();
		serverConnector.stopConnect();

	}
}
