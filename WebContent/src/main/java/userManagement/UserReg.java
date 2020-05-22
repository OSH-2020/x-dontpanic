package userManagement;

import com.opensymphony.xwork2.ActionSupport;

import database.*;


public class UserReg extends ActionSupport{

	private	static final long serialVersionUID = 1L;
	private String userName;
	private String userPasswd;
	//用来返回结果给前端
    private	String	result;
    
    public	void	setResult(String result)
    {
    	this.result = result;
    }
    
    public	String	getResult()
    {
    	return this.result;
    }
    
	public void setUserName(String name)
	{
		this.userName = name;
	}
	
	public void setUserPasswd(String Passwd)
	{
		this.userPasswd = Passwd;
	}
	
	public String getUserName()
	{
		return this.userName;
	}
	
	public String getUserPasswd()
	{
		return this.userPasswd;
	}
	

	
	@Override  
	public String execute() throws Exception
	{
		
		Query query = new Query();
		int ID = query.addUser(userName, userPasswd);
		query.closeConnection();
		if(ID==-1)
			result = "注册失败!";
		else
			result = "恭喜你，注册成功!";
		return "success";
	}
	

}
