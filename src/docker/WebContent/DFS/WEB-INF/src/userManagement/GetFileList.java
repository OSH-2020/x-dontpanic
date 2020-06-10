package userManagement;

import com.opensymphony.xwork2.ActionSupport;

import database.*;


public class GetFileList extends ActionSupport{

	private	static final long serialVersionUID = 1L;
	private String QueryPath;
	//用来返回结果给前端
    private	String	html;
    private String	status;
    
    public	void	setStatus(String status)
    {
    	this.status = status;
    }
    
    public	String	getStatus()
    {
    	return this.status;
    }
    
    public	void	setQueryPath(String Path)
    {
    	this.QueryPath = Path;
    }
    
    public	String	getQueryPath()
    {
    	return this.QueryPath;
    }
	
	public String getHtml()
	{
		return this.html;
	}
	
	public void setHtml(String html)
	{
		this.html = html;
	}
	
	@Override  
	public String execute() throws Exception
	{
		
		Query query = new Query();
		//System.out.println(QueryPath);
		FileItem[] fileArray= query.queryFile(QueryPath);
		query.closeConnection();
		
		//更新文件目录  首行始终存在
		html = html +
		"<tr class=\"file_list_back\">"+
			"<td> </td>"+
			"<td> <label><input type=\"checkbox\">&emsp;&emsp;</label><span class=\"glyphicon glyphicon-folder-open\"></span>&emsp;../</td>"+
			"<td></td>"+
			"<td></td>"+
		"</tr>";
		
		//设置查询状态
		if(fileArray==null)
		{
			status = "false";	
			return "success";
		}
		else
			status = "true";
		
		//新增的行
		for(int i=0;i<fileArray.length;i++)
		{
			html = html + 
			"<tr class=\"file_list_go\">"+
				"<td> </td>"+
				(fileArray[i].isFolder()?"<td> <label><input type=\"checkbox\"></label> 　　<span class=\"glyphicon glyphicon-folder-open\"></span>　" + fileArray[i].getName()+"</td>":"<td> <label><input type=\"checkbox\"></label> 　　<span class=\"glyphicon glyphicon-file\"></span>　" + fileArray[i].getName()+"</td>") +    
				"<td>"+fileArray[i].getAttribute()+"</td>"+
				"<td>"+fileArray[i].getTime()+"</td>"+
			"</tr>";			
		}
		
		return "success";
	}
	

}
