package database;

public class DeviceItem {
	
	private int id;
	private String ip;
	private int port;
	private boolean isOnline;
	private int rs;
	private int time;
	private int leftrs;
	
	public DeviceItem(int id,String ip,int port,boolean isOnline,int rs,int time,int leftrs) {
		this.id=id;
		this.ip=ip;
		this.port=port;
		this.isOnline=isOnline;
		this.rs=rs;
		this.time=time;
		this.leftrs=leftrs;
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

	public int getRs() { return rs; }

	public void setRs(int rs) {
		this.rs = rs;
	}

	public int getTime() { return time; }

	public void setTime(int time) {
		this.time = time;
	}

	public int getLeftrs() { return leftrs; }

	public void setLeftrs(int leftrs) { this.leftrs = leftrs; }
}
