package database;

public class FileItem {
		
	private int id;
	private String name;
	private String path;
	private String attribute;
	private String time;
	private int noa;
	private boolean isFolder;
	
	FileItem(int id,String name,String path,String attribute,String time,int noa,boolean isFolder) {
		this.id=id;
		this.name=name;
		this.path=path;
		this.attribute=attribute;
		this.time=time;
		this.noa=noa;
		this.isFolder=isFolder;
	}
	
	public FileItem(String name,String path,String attribute,String time,int noa,boolean isFolder){
		this.id=0;
		this.name=name;
		this.path=path;
		this.attribute=attribute;
		this.time=time;
		this.noa=noa;
		this.isFolder=isFolder;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getNoa() {
		return noa;
	}

	public void setNoa(int noa) {
		this.noa = noa;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

}
