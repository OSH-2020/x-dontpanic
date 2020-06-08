package fileDetector;

import java.io.File;
import java.util.List;

/**
 * 定时（默认周期为 2 秒）检测给定的空文件夹<br>
 * 一旦检测到文件放入，检测停止，对加入的文件调用回调接口 FileHandler 的 handle(File file) 方法<br>
 * 所有新加入的文件处理完毕之后，将文件夹清空，继续检测
 */
public class FolderScanner extends Thread {

	public static final int BYTES_IN_SHARDS = 500000;

	private File folder[];
	private String address[];
	private client.SynItem synItem;

	private static File tmpFragmentFolder;

	// 每次检测的时间间隔
	private final int interval = 60000;

	// 是否继续检测的标识，如果为 false 则检测线程停止
	private boolean detecting = true;

	public FolderScanner(File f[], String addr[], client.SynItem syn) {
		folder = f;
		address = addr;
		synItem = syn;
	}

	public static void init(File tmp) {
		tmpFragmentFolder = tmp;
	}

	@Override
	public void run() {
		FileUploader fUploader = new FileUploader();
		if (!fUploader.checkFolders(address)) {
			System.out.println("ERR: can not register folder");
			synItem.setStatus(2);
			return;
		}
		while (detecting) {
			try {
				scanFiles();
				Thread.sleep(interval);
			} catch (InterruptedException ex) {
				ex.printStackTrace(System.err);
			}
		}
	}

	// 扫描文件夹，如果有文件加入则处理该文件
	private void scanFiles() {

		for (int i = 0; i < folder.length; i++) {
			List<File> files = FileUtil.getAllFiles(folder[i]);
			for (File file : files) {
				if (!handleFile(file, i))
					return;
			}
			// 处理完毕之后，清空文件夹
			FileUtil.clearFolder(folder[i]);
		}
	}

	// 停止检测
	public void stopDetecting() {
		detecting = false;
	}

	public boolean handleFile(File file, int i) {

		String fileName = file.getName(); // 文件名
		String filePath = address[i] + '/';
		String attribute = "";
		if (file.canRead()) {
			attribute = attribute + 'r';
		} else {
			attribute = attribute + '-';
		}
		if (file.canWrite()) {
			attribute = attribute + 'w';
		} else {
			attribute = attribute + '-';
		}
		int noa = (int) (file.length() / BYTES_IN_SHARDS) + 1;
		noa = noa * 2;

		FileAttrs fileAttrs = new FileAttrs(fileName, filePath, attribute, noa);

		FileUploader fUploader = new FileUploader();

		int id = fUploader.registerFile(fileAttrs);
		if (id == -2) {
			System.out.println("ERR: can not get file id");
			synItem.setStatus(2);
			return false;
		} else if (id == -1) {
			System.out.println("ERR: server already has this file, skip it");
			return true;
		}

		try {
			if (!com.backblaze.erasure.Encoder.encode(file, tmpFragmentFolder, id)) {
				System.out.println("ERR: can not split file");
				synItem.setStatus(2);
				return false;
			}
		} catch (Exception e) {
			System.out.println("ERR: can not split file");
			synItem.setStatus(2);
			return false;
		}

		for (int j = 0; j < noa; j++) {
			if (!fUploader.pushFragment(id, j, noa)) {
				System.out.println("ERR: can not upload fragments");
				synItem.setStatus(2);
				return false;
			}
		}

		// 处理完毕，清空块文件夹
		FileUtil.clearFolder(tmpFragmentFolder);

		return true;
	}

}
