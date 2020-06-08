package database;

public class DeviceItem {
	
	private int id;
	private String ip;
	private int port;
	private boolean isOnline;
	private int rs;
	
	public DeviceItem(int id,String ip,int port,boolean isOnline,int rs) {
		this.id=id;
		this.ip=ip;
		this.port=port;
		this.isOnline=isOnline;
		this.rs=rs;
	}

	public int getId() {
		return id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setIsOnline(Boolean isOnline) {
		this.isOnline = isOnline;
	}

	public int getRs() {
		return rs;
	}

	public void setRs(int rs) {
		this.rs = rs;
	}

}
