[TOC]

## 项目介绍

本项目旨在实现可用性高的基于互联网网页的小型分布式文件系统。在已有的项目的基础上，希望实现容器化服务器端、多用户权限支持、更高效的文件传输、减轻中央服务器负担、提高文件安全性和可用性等优化，做出可用性高的“私人网盘”。

## 理论依据

### 容器化技术

#### 容器化技术的优势

#### 容器化技术的代表：Docker

### 多用户权限支持——RBAC介绍

以角色为基础的访问控制（Role-based access control，RBAC），是一种较新且广为使用的访问控制机制，其不同于其他定访问控制直接赋予使用者权限，而是将权限赋予角色。

![feasibility-RBAC-1](files/feasibility-RBAC-1.png)

在一个组织中，会因为不同的职责产生不同的角色，执行某项操作的权限被赋予某个的角色。组织成员则被赋予不同的角色，这些用户通过被赋予角色来取得执行某系统功能的权限。

对于批量的用户权限调整，只需调整用户关联的角色权限，无需对每一个用户都进行权限调整，既提升效率，又降低了出现漏调的概率。

数据库设计示意图如下：

![feasibility-RBAC-2](files/feasibility-RBAC-2.jpg)

### 纠删码

#### 编解码原理

#### Reed–Solomon 码

### 分离数据与控制链接

### //其他

## 技术依据

### Docker

### 实现多用户权限支持的技术

#### 前置项目关于用户权限的设计

##### 数据库配置

服务器数据库模块负责分布式文件系统的数据库访问，包括封装了数据库访问方法的Query 类与用于定义数据结构的 FileItem、DeviceItem、RequestItem 类。 

本分布式文件系统使用数据库维护所有的元数据，数据库中具体包括表 FILE 用于存储文件的逻辑位置与属性、表 FRAGMENT 用于存储碎片的物理位置、表 REQUEST 用于存储服务器对客户端的碎片请求、表 DEVICE 用于存储系统中客户端的信息、表 USER 用于存储网页的注册用户。

```mysql
CREATE TABLE `DEVICE` ( 
`ID` int NOT NULL AUTO_INCREMENT, 
`IP` char(20) NOT NULL DEFAULT '', 
`PORT` int NOT NULL DEFAULT 0, 
`ISONLINE` boolean NOT NULL, 
`RS` int NULL DEFAULT 0 , 
PRIMARY KEY (`ID`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `FRAGMENT` ( 
`ID` int NOT NULL, 
`PATH` char(20) NOT NULL DEFAULT '', 
PRIMARY KEY (`ID`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

CREATE TABLE `FILE` ( 
`ID` int NOT NULL AUTO_INCREMENT, 
`NAME` char(20) NOT NULL DEFAULT '', 
`PATH` char(60) NOT NULL DEFAULT '', 
`ATTRIBUTE` char(10) NOT NULL DEFAULT '', 
`TIME` char(10) NOT NULL DEFAULT '', 
`NOA` int NOT NULL DEFAULT 1, 
`ISFOLDER` boolean NOT NULL DEFAULT false, 
PRIMARY KEY (`id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

CREATE TABLE `REQUEST` (
`ID` int NOT NULL AUTO_INCREMENT, 
`TYPE` int NOT NULL DEFAULT 0, 
`FRAGMENTID` int NOT NULL DEFAULT 0, 
`DEVICEID` int NOT NULL DEFAULT 0, 
PRIMARY KEY (`ID`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

CREATE TABLE `USER` ( 
`ID` int NOT NULL AUTO_INCREMENT, 
`NAME` char(20) NOT NULL UNIQUE DEFAULT '', 
`PASSWD` char(20) NOT NULL DEFAULT '', 
PRIMARY KEY (`ID`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

CREATE UNIQUE INDEX `idx_FILE_PATH_NAME` ON `DFS`.`FILE` (PATH, NAME) 
COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT; 
CREATE UNIQUE INDEX `idx_USER_NAME` ON `DFS`.`USER` (NAME) COMMENT ''
ALGORITHM DEFAULT LOCK DEFAULT;
```

Query 类定义了对上述五个表查询、修改、删除、新增条目的函数，其通过 JDBC 接口实现了对数据的访问，访问的流程为： 

（一）在构造函数中使用 DriverManager.getConnection 函数创建到数据库的连接（一个Connection 类实例）； 

（二）通过 Connection 类实例的 createStatement 函数创建一个 Statement 类实例； 

（三）通过 Statement 类实例的 executeQuery 函数执行 SQL，SQL 的内容可以使用格式化字符串根据函数的参数填入不同的内容，该函数将返回一个 ResultSet 类实例； 

（四）对 ResultSet 类实例，使用 next 函数与 getInt、getBoolean、getString 等函数遍历查询的每个结果； 

（五）对 ResultSet 类实例与 Statement 类实例，执行 close 函数关闭连接； 

（六）在 closeConnection 函数中，调用 Connection 类实例 close 函数关闭连接。 

##### 注册/登录相关代码

UserReg.java

```java
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
```

UserLogin.java

```java
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
```

#### 达到改进目标用到的技术

##### 架构选择

服务器端采用三层架构的方式，分成了表现层、业务层和持久层。表现层使用JSP和Servlet程序，与浏览器客户端进行数据的交互。业务层使用Service程序，进行业务逻辑处理和事务处理。持久层使用Dao程序，进行数据库的持久化操作。

##### Spring Security

Spring Security 是一个Spring生态中安全方面的框架，能够为基于 Spring 的企业应用系统提供声明式的安全访问控制解决方案。



### Reed-Solomon

### WebAssembly

### TODO 实现分离数据与控制链接的技术

#### //其他

## 技术路线

### 前端

### 客户端

### 服务器

### 其他

## 参考文献