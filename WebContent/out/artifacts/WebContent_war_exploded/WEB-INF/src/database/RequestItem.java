package database;

public class RequestItem {
	
	private int id;
	private int type;
	private int fragmentId;
	private int deviceId;
	
	RequestItem(int id, int type, int fid, int did){
		this.id=id;
		this.type=type;
		this.fragmentId=fid;
		this.deviceId=did;
	}
	
	public RequestItem(int type, int fid, int did){
		this.id=0;
		this.type=type;
		this.fragmentId=fid;
		this.deviceId=did;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFragmentId() {
		return fragmentId;
	}

	public void setFragmentId(int fragmentId) {
		this.fragmentId = fragmentId;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

}
