package userManagement;

import com.opensymphony.xwork2.ActionSupport;

import database.*;


public class UserLogin extends ActionSupport{

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
		String passwdStandard = query.queryUserPasswd(userName);
		query.closeConnection();
		
		if(passwdStandard==null)
		{
			result = "登录失败：该用户不存在！";
			return "success";
		}
		if(passwdStandard.compareTo(userPasswd)==0)
		{
			result = "login sucessfully!";
			//System.out.println("登录密码吻合");
			return "success";	
		}
		else
		{
			result = "登录失败：密码错误！";
			return	"success";
		}
	}
	

}
