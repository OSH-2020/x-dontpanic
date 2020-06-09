package userManagement;

import java.io.File;
import java.util.LinkedList;

import com.opensymphony.xwork2.ActionSupport;

import database.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class FileDownloader extends ActionSupport{
	
	private	static final long serialVersionUID = 1L;
	private String path;
	private String name;
	//用来返回结果给前端
    private	String	result;
	private JSONObject devices;
	private String fileType;
	private int fileSize;
	private int noa;
	private int nod;
	//TODO
	private static final String fragmentFolderPath = "/usr/local/tomcat/webapps/DFS/CloudDriveServer/downloadFragment";
	private static final String fileFolderPath = "/usr/local/tomcat/webapps/DFS/CloudDriveServer/tmpFile";
	
    public	String	getPath()
    {
    	return this.path;
    }
    
	public void setPath(String path)
	{
		this.path = path;
	}
	
    public	String	getResult()
    {
    	return this.result;
    }
    
	public void setResult(String result)
	{
		this.result = result;
	}
	
    public	String	getName()
    {
    	return this.name;
    }
    
	public void setName(String name)
	{
		this.name = name;
	}

	private JSONObject test;

	public	JSONObject	getTest()
	{
		return this.test;
	}

	public void setTest(String name)
	{
		this.name = name;
	}


	public	JSONObject	getDevices()
	{
		return this.devices;
	}

	public void setDevices(JSONObject devices)
	{
		this.devices = devices;
	}

	public	String	getFileType()
	{
		return this.fileType;
	}

	public void setFileType(String devices)
	{
		this.fileType = fileType;
	}
	public	int	getFileSize()
	{
		return this.fileSize;
	}

	public void setFileSize(int fileSize)
	{
		this.fileSize = fileSize;
	}

	public	int	getNoa()
	{
		return this.noa;
	}

	public void setNoa(int noa)
	{
		this.noa = noa;
	}

	public	int	getNod()
	{
		return this.nod;
	}

	public void setNod(int nod)
	{
		this.nod = nod;
	}

	
	public String downloadRegister(){
		//return -1 if error
		//return 0 if can not collect enough fragments
		//else, return 1
		
		System.out.println("downloadRegister is called");
		
		Query query=new Query();
		FileItem fileItem=query.queryFile(path, name);
		DeviceItem onlineDevice[]=query.queryOnlineDevice();

		if(onlineDevice==null)
		{
			System.out.println(1);
			result = "NotEnoughFragments";
			/*
			JSONObject responseDetailsJson = new JSONObject();
			JSONArray jsonArray = new JSONArray();

			JSONObject formDetailsJson = new JSONObject();
			formDetailsJson.put("id", "1");
			formDetailsJson.put("name", "name1");
			jsonArray.add(formDetailsJson);
			formDetailsJson = new JSONObject();
			formDetailsJson.put("id", "2");
			formDetailsJson.put("name", "name2");
			jsonArray.add(formDetailsJson);

			responseDetailsJson.put("forms", jsonArray);
			test=responseDetailsJson;
			System.out.println(test);*/
			return "success";
		}

		if (fileItem==null || fileItem.getNod()<1){
			query.closeConnection();
			result = "Error";
			return "success";
		}		
		else{
			int nod=fileItem.getNod();
			int noa=fileItem.getNoa();
			int id=fileItem.getId();
			int deviceID;
			String str;
			LinkedList<AnotherRequestItem> reqItems = new LinkedList<>();
			JSONArray jsonArray = new JSONArray();
			for (int i=0;i<nod+noa;i++){
				str=query.queryFragment(id*100+i);
				if (str==null || str.equals("-1"))
					continue;
				deviceID=Integer.parseInt(str);
				for (DeviceItem deviceItem : onlineDevice){//TODO
					if (deviceItem.getId()==deviceID){
						DeviceItem curDevice=query.queryDevice(deviceID);
						//reqItems.add(new AnotherRequestItem(curDevice.getIp(), String.valueOf(curDevice.getPort()), String.valueOf(id*100+i),fileType,i));

						JSONObject formDetailsJson = new JSONObject();
						formDetailsJson.put("filename", String.valueOf(id*100+i));
						formDetailsJson.put("fragmentId", i);
						formDetailsJson.put("ip", curDevice.getIp());
						formDetailsJson.put("port", String.valueOf(curDevice.getPort()));
						jsonArray.add(formDetailsJson);
						break;
					}
				}
			}
			if (jsonArray.size() < nod){
				query.closeConnection();
				result = "NotEnoughFragments";
				return "success";
			}
			else{

				System.out.println(reqItems.size());

				//System.out.println(jsonArray.size());
				//System.out.println(jsonArray.toString());
				devices= new JSONObject();

				devices.put("forms", jsonArray);
				System.out.println(devices);

				this.fileSize= fileItem.getFileSize();
				this.fileType= fileItem.getFileType();
				this.nod=fileItem.getNod();
				this.noa=fileItem.getNoa();
				query.closeConnection();
				result = "OK";
				return "success";
			}
		}
	}
	
	public String progressCheck(){
		//return -1 if error
		//else, return a number from 0 to 100 as # of fragments which have been downloaded
		Query query=new Query();
		FileItem fileItem=query.queryFile(path, name);
		query.closeConnection();
		if (fileItem==null)
		{
			result = "Error";
			return "success";
		}
		else{
			String fileId=Integer.toString(fileItem.getId());
			int collectedFiles = 0;
			File dir=new File(fragmentFolderPath);			
			String files[]=dir.list();
			for (String file : files){
				if (file.substring(0, file.length()-2).equals(fileId))
					collectedFiles++;
			}
			float percentage = (float)collectedFiles / fileItem.getNoa();
			//只需要总数目的一半　　因此进度×２
			percentage *= 2;
			collectedFiles = (int) (percentage * 100);
			System.out.println("pregress check is called,return "+ collectedFiles);
			
			result = Integer.toString(collectedFiles);
			return "success";
		}		
	}
	
	
	
	
	public String decodeFile(){
		//return 1 and DELETE ALL FRAGMENTS OF INPUT FILE if decode successfully
		//else, return 0
		
		System.out.println("decodeFile is called");
		
		Query query=new Query();
		FileItem fileItem=query.queryFile(path, name);
		query.closeConnection();
		if (fileItem==null)
		{
			result = "Error";
			return "success";
		}
		else{
			try {
				if (com.backblaze.erasure.Decoder.decode(
						new File(fragmentFolderPath), new File(fileFolderPath+'/'+name), 
						fileItem.getId(), fileItem.getNoa())) {
					File dir=new File(fragmentFolderPath);			
					File files[]=dir.listFiles();
					String fileId=Integer.toString(fileItem.getId());
					String str;
					for (File file : files){
						str=file.getName();
						if (str.substring(0, str.length()-2).equals(fileId))
							file.delete();
					}	
				{
					result = "OK";
					return "success";
				}
			}			
			else
			{
				result = "Error";
				return "success";
			}
			}
			catch (Exception e) {
					result = "Error";
					return "success";
			}
		}		
	}
	
	/* only for debug 
	public static void main(String args[]) {

     	System.out.println(downloadRegister("TIM/", "2016.jpg"));
      	System.out.println(downloadRegister("TIM/tmp/", "2015.jpg"));
      	System.out.println(downloadRegister("TIM/", "2015.jpg"));
      	
      	System.out.println(progressCheck("TIM/", "2016.jpg"));
      	System.out.println(progressCheck("TIM/tmp/", "2015.jpg"));
      	System.out.println(progressCheck("TIM/", "2015.jpg"));
      	
      	System.out.println(decodeFile("TIM/", "2016.jpg"));
      	System.out.println(decodeFile("TIM/tmp/", "2015.jpg"));
      	
	}
	*/
}
