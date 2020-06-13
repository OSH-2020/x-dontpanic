package dataConnect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

class FileTransporter {

	public static boolean recvFile(File f, DataInputStream socIn, DataOutputStream socOut) {
		try {
			FileOutputStream fos = new FileOutputStream(f);
			
			// 确认文件长度
			long fileLength = socIn.readLong();
			
			// 接收文件
			byte[] sendBytes = new byte[1024];
			long read = 0;
			int r;
			while (read<fileLength) {				
				r = socIn.read(sendBytes);
				read+=r;
				fos.write(sendBytes, 0, r);
				fos.flush();
			}
			
			fos.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean sendFile(File f, DataInputStream socIn, DataOutputStream socOut) {
		try {
			FileInputStream fis = new FileInputStream(f);
			
			// 确认文件长度
			socOut.writeLong(f.length());
			socOut.flush();

			// 传输文件
			byte[] sendBytes = new byte[1024];
			int length = 0;
			while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
				socOut.write(sendBytes, 0, length);
				socOut.flush();
			}

			fis.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
