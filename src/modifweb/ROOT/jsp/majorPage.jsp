<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!-- 导入ｄａｔａｂａｓｅ支持的包 -->
<%@ page import="database.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="zh-CN">
<head>

<!--bootstrap和JQerry相关库-->
<script src="../js/jquery/jquery.js"></script>
<script src="../js/jquery/jquery.cookie.js"></script>
<script src="../js/bootstrap-3.3.7/js/bootstrap.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/bootstrap-3.3.7/css/bootstrap.min.css">
<!--AJAX相关js动作-->

<script src="../js/ec/object_hash.js" type="text/javascript"></script>
<script src="../js/ec/erasure.js"></script>
<script src="../js/ec/funcs.js"></script>

<script src="../js/majorPage_ajax.js"></script>
<meta name="viewport" content="width=device-width, initial-scale=1.0">					<!-- 为了让 Bootstrap 开发的网站对移动设备友好，确保适当的绘制和触屏缩放 -->

<link rel="stylesheet" href="../layui/css/layui.css" media="all">
<script src="../layui/js/layui.all.js" charset="utf-8"></script>
<script src="../layui/js/layui.js" charset="utf-8"></script>

<title>DFS 分布式网盘</title>

</head>

<body>

<div class="layui-row">
	<div>
		<div style="
			display: table-cell;
			vertical-align: middle;
			text-align: center;
			padding: 0 5%;
			background: #ffffff;
			-webkit-font-smoothing: antialiased;
			-moz-osx-font-smoothing: grayscale;">
			<div style="margin:0 auto;">
				<h1 style="
					font-family: Arial, Helvetica, sans-serif;
					font-size: 6rem;
					color: #000000;">
					DFS - Distributed FileSystem
				</h1>
			</div>

			<div class="carousel slide" id="carousel-275345">
				<ol class="carousel-indicators">
					<li data-slide-to="0" data-target="#carousel-275345">
					</li>
					<li data-slide-to="1" data-target="#carousel-275345" class="active">
					</li>
					<li data-slide-to="2" data-target="#carousel-275345">
					</li>
				</ol>
				<div class="carousel-inner">
					<div class="item">
						<img alt="" src="../material/pic1.jpg" />
						<div class="carousel-caption">
							<h4>
								xyy1
							</h4>
							<p>
								xyy1
							</p>
						</div>
					</div>
					<div class="item active">
						<img alt="" src="../material/pic2.jpg" />
						<div class="carousel-caption">
							<h4>
								xyy2
							</h4>
							<p>
								xyy2
							</p>
						</div>
					</div>
					<div class="item">
						<img alt="" src="../material/pic3.jpg" />
						<div class="carousel-caption">
							<h4>
								xyy3
							</h4>
							<p>
								xyy3
							</p>
						</div>
					</div>
				</div>
				<a class="left carousel-control" href="#carousel-275345" data-slide="prev">
					<span class="glyphicon glyphicon-chevron-left"></span>
				</a>
				<a class="right carousel-control" href="#carousel-275345" data-slide="next">
					<span class="glyphicon glyphicon-chevron-right"></span>
				</a>
			</div>
		</div>
	</div>
</div>

<div>
	<div class="clearfix" style="margin-bottom: 50px;"></div>

<!-- 路径导航 -->
	<div class="row clearfix">
		<div class="col-md-12 column">
			<div>当前访问位置：　 </div>
			<ul class="breadcrumb" id = "curr_path">
			<!--
				<li>
					 <a href="#">Home</a>
				</li>
				<li>
					 <a href="#">Library</a>
				</li>
				<li class="active">
					Data
				</li>
			 -->

			</ul>
		</div>
	</div>

<!-- 文件目录表＋进度区 -->
	<div class="row clearfix">
		<div class="col-md-7 column">
			<div class="row pre-scrollable">
				<table class="table" id="fileCatalogTable">
   					<thead>
      					<tr>
     						<th></th>
         					<th>文件名</th>
         					<th>读写权限</th>
         					<th>修改时间</th>
      					</tr>
   					</thead>
   					<tbody id="file_list_body">
      					<tr class="file_list_back">
      						<td> </td>
         					<td> <label><input type="checkbox">&emsp;&emsp;</label><span class="glyphicon glyphicon-folder-open"></span>&emsp;../</td>
         					<td></td>
         					<td></td>
      					</tr>
						<%
							Cookie cookie = null;
							Cookie[] cookies = null;
							String username = null;
							// 获取 cookies 的数据,是一个数组
							cookies = request.getCookies();
							if( cookies != null ){
								//out.println("<h2> 查找 Cookie 名与值</h2>");
								for (int i = 0; i < cookies.length; i++){
									cookie = cookies[i];
									if (("username").equals(cookie.getName())) {
										username = cookie.getValue();
										//out.print(username);
									}
								}
							} else{
								out.println("<h2>没有发现 Cookie</h2>");
							}
						%>
						<%
							// 重定向到新地址
								if(username == null){
							String site = new String("../index.html");
							response.setStatus(response.SC_MOVED_TEMPORARILY);
							response.setHeader("Location", site); }
						%>
						<%
							int i;
							Query query = new Query();
							FileItem[] files = query.queryFileList(username, "/");
							query.closeConnection();


							if(files==null)
								return;
							else
							{
								for(i=0;i<files.length;i++)
								{
									out.println("<tr class='file_list_go'>");
									out.println("<td></td>");
									if(files[i].isFolder()==false)
										out.println("<td> <label><input type=\"checkbox\"></label> 　　<span class=\"glyphicon glyphicon-file\"></span>　" + files[i].getFileName()+"</td>");
									else
										out.println("<td> <label><input type=\"checkbox\"></label> 　　<span class=\"glyphicon glyphicon-folder-open\"></span>　" + files[i].getFileName()+"</td>");
									out.println("<td>"+files[i].getAttribute()+"</td>");
									out.println("<td>"+files[i].getTime()+"</td>");
									out.println("</tr>");
								}
							}
						%>
   					</tbody>
				</table>
			</div>
		</div>
		<div class="col-md-1 column"></div>
		<div class="col-md-4 column">

			<h2>
				当前任务进度：
			</h2>
			<!--
				<div class="progress progress-striped active">
						<div class="progress-bar progress-bar-success" role="progressbar" style="width: 40%;">当前进度</div>
				</div>
			-->
			<div class="row pre-scrollable" id="download_progress_area">

			</div>
		</div>
	</div>

	<!-- 按钮组 -->
	<div class="row clearfix">
		<div class="col-md-8 column">
			<div class="clearfix" style="margin-bottom: 50px;"></div><!-- 清除浮动 -->

			<!-- 下载、上传、删除按钮 -->
			<div class="col-md-1 column col-md-offset-1">
				<button class="btn btn-primary" type="button" id="button_download">
				预下载
				</button>
			</div>
			<div class="col-md-1 column col-md-offset-1">
				<button class="btn btn-primary" type="button" id="button_delete">
				删除
				</button>
			</div>
			<div class="col-md-1 column col-md-offset-1">
				<input type="file" id="files" style="display: none" onchange="fileUpload();">
				<button class="btn btn-primary" type="button" id="button_upload">
				上传
				</button>
			</div>
			<div class="col-md-1 column col-md-offset-1">
				<button class="btn btn-primary" type="button" id="button_rename">
				重命名
				</button>
			</div>
	 	</div>

 		<div class="col-md-4 column">
		</div>
	</div>

	<div class="clearfix" style="margin-bottom: 50px;"></div>
</div>
<div>
	<div style="margin:0 auto; width:60%; height:100px; border:1px">
		<div class="alert alert-success alert-dismissable">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
			<h4>
				提示:
			</h4>
				<p id="statusFeedback">
				欢迎使用本系统～
				</p>
		</div>
	</div>
</div>
<div>
	<div style="margin:0 auto; width:80%; height:100px; border:1px">
		<h2>
			About：
		</h2>
		<p>
			本系统基于王珺、夏昊珺、滕思洁和郑值开发的「私有网盘」二次开发，
			剥离了服务器端的数据合并操作，
			转移到浏览器端进行，
			实现了分布式。
		</p>
		<p>
			由罗丽薇、邱子悦、袁一玮、余致远共同开发，
			有疑问欢迎提 <a href="https://github.com/OSH-2020/x-dontpanic">issue</a>。
		</p>
	</div>
</div>
</body>
</html>



