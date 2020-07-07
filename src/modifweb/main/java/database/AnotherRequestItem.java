package database;

public class AnotherRequestItem {

	private String ip;
	private String port;
	private String filename;
	private String fileType;
	private int fileSize;
	//private int fileId;
	private int fragmentId;

	/*
	AnotherRequestItem(int ip, int port, String filename){
		this.ip=ip;
		this.port=port;
		this.filename=filename;
	}*/


	public AnotherRequestItem(String ip, String port, String filename,String fileType,int fragmentId){
		this.ip=ip;
		this.port=port;
		this.filename=filename;
		this.fragmentId=fragmentId;
		this.fileType=fileType;
	}

	public String getIp() {
		return ip;
	}

	public String getFileType(){
		return fileType;
	}

	public int getFragmentId() {
		return fragmentId;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}


}
