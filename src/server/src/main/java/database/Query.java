package database;

import java.sql.*;

public class Query {
	
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    //static final String DB_URL = "jdbc:mysql://localhost:3306/mysql?useSSL=false";
    static final String DB_URL = "jdbc:mysql://mysql:3306/mysql?useSSL=false";
    static final String USER = "root";
    static final String PASS = "201314";
    
    Connection conn = null;
	
	public Query(){
		try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER,PASS);       
        }
		catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public void closeConnection(){
		try{
            if (conn!=null) 
            	conn.close();
        }
		catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public FileItem queryFile(String path,String name){
        Statement stmt = null;
        ResultSet rs = null;
        FileItem fileItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FILE WHERE NAME='%s' AND PATH='%s'",name,path);
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int id  = rs.getInt("ID");
                int noa = rs.getInt("NOA");
                String attr = rs.getString("ATTRIBUTE");
                String time = rs.getString("TIME");
                boolean isFolder = rs.getBoolean("ISFOLDER");
    
                fileItem=new FileItem(id,name,path,attr,time,noa,isFolder);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return fileItem;
	}
	
	public FileItem queryFile(int id){
        Statement stmt = null;
        ResultSet rs = null;
        FileItem fileItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FILE WHERE ID='%d'",id);
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
            	int noa = rs.getInt("NOA");
                String name  = rs.getString("NAME");
                String path  = rs.getString("PATH");              
                String attr = rs.getString("ATTRIBUTE");
                String time = rs.getString("TIME");
                boolean isFolder = rs.getBoolean("ISFOLDER");
    
                fileItem=new FileItem(id,name,path,attr,time,noa,isFolder);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return fileItem;
	}
	
	public FileItem[] queryFile(String path){
		Statement stmt = null;
        ResultSet rs = null;
        FileItem fileArray[] = null;
        
        int id, noa;
        String name,attr, time;
        boolean isFolder;
        
        int count,i;
        
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FILE WHERE PATH='%s'",path);
            rs = stmt.executeQuery(sql);
            
            if (!rs.last())
            	return null;           	
            count = rs.getRow();
            fileArray=new FileItem[count];
            i=0;
            rs.first();
            
            while (i<count) {
                id = rs.getInt("ID");                
                noa = rs.getInt("NOA");
                name = rs.getString("NAME");
                attr = rs.getString("ATTRIBUTE");
                time = rs.getString("TIME");
                isFolder = rs.getBoolean("ISFOLDER");
    
                fileArray[i]=new FileItem(id,name,path,attr,time,noa,isFolder);
                rs.next();
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return fileArray;
	}
	
	public String queryFragment(int id){
        Statement stmt = null;
        ResultSet rs = null;
        String path = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.FRAGMENT WHERE ID='%d'",id);
            rs = stmt.executeQuery(sql);
            
            if (rs.next())
                path = rs.getString("PATH");
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return path;
	}
	
	public int queryFragmentNumbers(int fileId){
		Statement stmt = null;
        ResultSet rs = null;       
        
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT COUNT(*) FROM DFS.FRAGMENT WHERE ID>='%d' AND ID<'%d'",
            		fileId*100, (fileId+1)*100);
            rs = stmt.executeQuery(sql);
                        
            rs.next();
            return rs.getInt(1);
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
	}
	
	public DeviceItem[] queryOnlineDevice(){
        Statement stmt = null;
        ResultSet rs = null;
        DeviceItem deviceArray[] = null;
        
        String ip;
        int port,rst,id;

        int count,i;

        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.DEVICE WHERE ISONLINE=true ORDER BY RS DESC");
            rs = stmt.executeQuery(sql);
            
            if (!rs.last())
                return null;
            count = rs.getRow();
            deviceArray=new DeviceItem[count];
            i=0;
            rs.first();

            while (i<count){

                id = rs.getInt("ID");
                ip  = rs.getString("IP");
                port = rs.getInt("PORT");
                rst = rs.getInt("RS");
  
                deviceArray[i]=new DeviceItem(id,ip,port,true,rst);
                rs.next();
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                    rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                    stmt.close();
            }
            catch(Exception e){
            }            
        }
        return deviceArray;
	}

	public DeviceItem queryDevice(int id){
		Statement stmt = null;
        ResultSet rs = null;
        DeviceItem deviceItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.DEVICE WHERE ID='%d'",id);
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                String ip  = rs.getString("IP");
                int port = rs.getInt("PORT");
                boolean isOnline = rs.getBoolean("ISONLINE");
                int rst = rs.getInt("RS");
  
                deviceItem=new DeviceItem(id,ip,port,isOnline,rst);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return deviceItem;
	}
	
	public RequestItem queryRequestById(int id){
        Statement stmt = null;
        ResultSet rs = null;
        RequestItem requestItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.REQUEST WHERE ID='%d'",id);
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int type = rs.getInt("TYPE");
                int fid = rs.getInt("FRAGMENTID");
                int did = rs.getInt("DEVICEID");
    
                requestItem=new RequestItem(id,type,fid,did);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return requestItem;
	}
	
	public RequestItem queryFirstRequest(int id){
        Statement stmt = null;
        ResultSet rs = null;
        RequestItem requestItem = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.REQUEST WHERE DEVICEID='%d' LIMIT 1",id);
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
            	int rid = rs.getInt("ID");
                int type = rs.getInt("TYPE");
                int fid = rs.getInt("FRAGMENTID");
    
                requestItem=new RequestItem(rid,type,fid,id);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return requestItem;
	}	
	
	public RequestItem[] queryRequest(int deviceId){
		Statement stmt = null;
        ResultSet rs = null;
        RequestItem requsetArray[] = null;
        
        int id, type, fid, did;
        
        int count,i;
        
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.REQUEST WHERE DEVICEID='%d'",deviceId);
            rs = stmt.executeQuery(sql);
            
            if (!rs.last())
            	return null;           	
            count = rs.getRow();
            requsetArray=new RequestItem[count];
            i=0;
            rs.first();
            
            while (i<count) {
                id = rs.getInt("ID");                
                type = rs.getInt("TYPE");
                did = rs.getInt("DEVICEID");
                fid = rs.getInt("FRAGMENTID");
    
                requsetArray[i]=new RequestItem(id,type,fid,did);
                rs.next();
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return requsetArray;
	}
	
	public int queryRequestNumbers(int deviceId){
		Statement stmt = null;
        ResultSet rs = null;       
        
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT COUNT(*) FROM DFS.REQUEST WHERE DEVICEID='%d'",deviceId);
            rs = stmt.executeQuery(sql);
                        
            rs.next();
            return rs.getInt(1);
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
	}
	
	public int queryRequestNumbers(int fileId, int type){
		Statement stmt = null;
        ResultSet rs = null;       
        
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT COUNT(*) FROM DFS.REQUEST WHERE FRAGMENTID>='%d' "
            		+ "AND FRAGMENTID<'%d' AND TYPE='%d'", fileId*100, (fileId+1)*100, type);
            rs = stmt.executeQuery(sql);
                        
            rs.next();
            return rs.getInt(1);
        }
        catch(Exception e){
            e.printStackTrace();
            return 0;
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
	}
	
	public String queryUserPasswd(String name){
        Statement stmt = null;
        ResultSet rs = null;
        String passwd = null;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.USER WHERE NAME='%s'",name);
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
            	passwd = rs.getString("PASSWD");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return passwd;
	}	
	
	public int queryUserID(String name){
        Statement stmt = null;
        ResultSet rs = null;
        int id = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("SELECT * FROM DFS.USER WHERE NAME='%s'",name);
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
            	id = rs.getInt("ID");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return id;
	}
	
	public int addFile(FileItem file){
		Statement stmt = null;
		ResultSet rs = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            if (file.isFolder())
	            sql = String.format("INSERT INTO DFS.FILE (NAME,PATH,ATTRIBUTE,TIME,NOA,ISFOLDER) "
	            		+ "VALUES ('%s','%s','%s','%s',%d,true);",file.getName(),file.getPath(),
	            		file.getAttribute(),file.getTime(),file.getNoa());
            else
            	sql = String.format("INSERT INTO DFS.FILE (NAME,PATH,ATTRIBUTE,TIME,NOA,ISFOLDER) "
	            		+ "VALUES ('%s','%s','%s','%s',%d,false);",file.getName(),file.getPath(),
	            		file.getAttribute(),file.getTime(),file.getNoa());
            suc = stmt.executeUpdate(sql);
            if (suc>0){
            	rs = stmt.executeQuery("select LAST_INSERT_ID()");
            	rs.next();
            	suc=rs.getInt(1);
            }
            else
            	suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}

	public int deleteFile(int id){
		Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("DELETE FROM DFS.FILE WHERE ID=%d",id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
            	return 1;
            else
            	return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}
	
	public int alterFile(FileItem file){
		Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            if (file.isFolder())
	            sql = String.format("UPDATE DFS.FILE SET NAME='%s',PATH='%s',ATTRIBUTE='%s',"
	            		+ "TIME='%s',NOA=%d,ISFOLDER=true WHERE id=%d;",file.getName(),file.getPath(),
	            		file.getAttribute(),file.getTime(),file.getNoa(),file.getId());
            else
            	sql = String.format("UPDATE DFS.FILE SET NAME='%s',PATH='%s',ATTRIBUTE='%s',"
	            		+ "TIME='%s',NOA=%d,ISFOLDER=false WHERE id=%d;",file.getName(),file.getPath(),
	            		file.getAttribute(),file.getTime(),file.getNoa(),file.getId());
            suc = stmt.executeUpdate(sql);
            if (suc>0)
            	return 1;
            else
            	return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}
	
	public int alterDevice(DeviceItem device){
		Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            if (device.isOnline())
	            sql = String.format("UPDATE DFS.DEVICE SET IP='%s',PORT=%d,ISONLINE=true,"
	            		+ "RS=%d WHERE id=%d;",device.getIp(),device.getPort(),device.getRs(),
	            		device.getId());
            else
            	sql = String.format("UPDATE DFS.DEVICE SET IP='%s',PORT=%d,ISONLINE=false,"
	            		+ "RS=%d WHERE id=%d;",device.getIp(),device.getPort(),device.getRs(),
	            		device.getId());
            suc = stmt.executeUpdate(sql);
            if (suc>0)
            	return 1;
            else
            	return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}
	
	public int addFragment(int id,String path){
		Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
	        sql = String.format("INSERT INTO DFS.FRAGMENT VALUES (%d,'%s');",
	        		id,path);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
            	suc=1;
            else
            	suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}

	public int deleteFragment(int id){
		Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
	        sql = String.format("DELETE FROM DFS.FRAGMENT WHERE ID=%d",id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
            	suc=1;
            else
            	suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}
	
	public int alterFragment(int id, String path){
		Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("UPDATE DFS.FRAGMENT SET PATH='%s' WHERE id=%d;",
            		path, id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
            	return 1;
            else
            	return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}
	
	public int addRequest(RequestItem request){
		Statement stmt = null;
		ResultSet rs = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("INSERT INTO DFS.REQUEST (TYPE,FRAGMENTID,DEVICEID) "
            		+ "VALUES ('%d','%d','%d');",
            		request.getType(), request.getFragmentId(), request.getDeviceId());
            suc = stmt.executeUpdate(sql);
            if (suc>0){
            	rs = stmt.executeQuery("select LAST_INSERT_ID()");
            	rs.next();
            	suc=rs.getInt(1);
            }
            else
            	suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(rs!=null && !rs.isClosed()) 
                	rs.close();
            }
            catch(Exception e){
            }
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}
	
	public int deleteRequest(int id){
		Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("DELETE FROM DFS.REQUEST WHERE ID=%d",id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
            	suc=1;
            else
            	suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}
	
	public int addUser(String name, String passwd){
		Statement stmt = null;
		ResultSet rs = null;
	    int suc = -1;
	    try{
	        stmt = conn.createStatement();
	        String sql;
	        sql = String.format("INSERT INTO DFS.USER (NAME,PASSWD) "
	        		+ "VALUES ('%s','%s');", name, passwd);
	        suc = stmt.executeUpdate(sql);
	        if (suc>0){
	        	rs = stmt.executeQuery("select LAST_INSERT_ID()");
	        	rs.next();
	        	suc=rs.getInt(1);
	        }
	        else
	        	suc=-1;
	    }
	    catch(Exception e){
	        e.printStackTrace();
	    }        
	    finally{
	        try{
	            if(rs!=null && !rs.isClosed()) 
	            	rs.close();
	        }
	        catch(Exception e){
	        }
	        try{
	            if(stmt!=null && !stmt.isClosed()) 
	            	stmt.close();
	        }
	        catch(Exception e){
	        }            
	    }
	    return suc;
	}
	
	public int alterUser(int id, String name, String passwd){
		Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("UPDATE DFS.USER SET NAME='%s',PASSWD=%s WHERE id=%d;",
            		name, passwd, id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
            	return 1;
            else
            	return -1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}
	
	public int deleteUser(int id){
		Statement stmt = null;
        int suc = -1;
        try{
            stmt = conn.createStatement();
            String sql;
            sql = String.format("DELETE FROM DFS.USER WHERE ID=%d",id);
            suc = stmt.executeUpdate(sql);
            if (suc>0)
            	suc=1;
            else
            	suc=-1;
        }
        catch(Exception e){
            e.printStackTrace();
        }        
        finally{
            try{
                if(stmt!=null && !stmt.isClosed()) 
                	stmt.close();
            }
            catch(Exception e){
            }            
        }
        return suc;
	}
}
