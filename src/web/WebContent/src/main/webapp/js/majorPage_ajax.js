/*
function createAndDownloadFile(fileName, fileType, content) {
	var aTag = document.createElement('a');
	var blob = new Blob([content], { type: fileType, name: fileName });
	aTag.download = fileName;
	aTag.href = URL.createObjectURL(blob);
	aTag.click();
	URL.revokeObjectURL(blob);
}*/
/*
function waitForSocketConnection(socket, callback){
	setTimeout(
		function () {
			if (socket.readyState === 1) {
				console.log("Connection is made")
				if (callback != null){
					callback();
				}
			} else {
				console.log("wait for connection...")
				waitForSocketConnection(socket, callback);
			}

		}, 10); // wait 5 milisecond for the connection...
}
function syncSleep(time) {
	const start = new Date().getTime();
	while (new Date().getTime() - start < time) {}
}*/
function WebSocketDownload(ip,port,fragmentName,content,digest,fragmentId)
{
	var ret_bytes;
	var ret_digest;
	if ("WebSocket" in window)
	{
		let ws = new WebSocket("ws://"+ip+":"+port);
		ws.binaryType="arraybuffer";
		ws.onopen = function()
		{
				//alert("Sending Message...");
				ws.send("D");
				ws.send(fragmentName);
				console.log('send filename');
		};

		ws.onmessage = function (evt)
		{
			let received_data = evt.data;
			//alert("received");
			//if(evt.data instanceof Blob ){
			if(evt.data instanceof ArrayBuffer ){
				//alert("Received arraybuffer");
				console.log('Blob');
				ret_bytes= received_data;
				console.log('recv bytes');
			}
			if(typeof(evt.data) =='string') {
				//alert("Received data string");
				console.log('string');
				ret_digest= received_data;
				console.log('recv digest');
			}
		};

		ws.onclose = function()
		{
			// sí websocket
			//alert("Connection Closed...");
			content[fragmentId]=ret_bytes;
			digest[fragmentId]=ret_digest;
			console.log('closed connection');
		};
	}
	else
	{
	}
	/*
	alert("Start...");
	syncSleep(2000);
	//alert("Finish...");
	console.log(ret_bytes);
	*/
	return ret_bytes;
}
/*
function decodeFile(fileName,fileType,nod,noa,content,digest,fileSize)
{
	console.log(fileName);
	console.log(fileType);
	console.log((fileSize));
	console.log(nod);
	console.log(noa);

	for(var i=0;i<noa+nod;i++){
		console.log(content[i]);
		console.log(digest[i]);
	}
}*/
function decodeFile(fileName, fileType, numOfDivision, numOfAppend, content, digest, fileSize) {
	//clean wrong parts
	var errors = 0;
	/*
	for (var i = 0; i < content.length; i++) {
		if (digest[i] != objectHash.MD5(content[i])) {
			errors += 1;
			content[i] = new Uint8Array(content[i].length);
		}
	}*/

	//console.log(content);
	const t5 = Date.now();//Decode timing start

	var contentView=new Array(content.length);
	for(var i=0;i<content.length;i++){
		contentView[i]=new Uint8Array(content[i]);
	}
	var decoded = erasure.recombine(contentView, fileSize, numOfDivision, numOfAppend);
	//console.log(decoded);
	if (decoded.length > fileSize)
		decoded = decoded.subarray(0, original.length);
	const t6 = Date.now();//Decode timing end

	// after decoded, download the file and show info(time, errors)
	createAndDownloadFile(fileName, fileType, decoded);

	if (document.getElementById("decode") != null)
		document.getElementById("decode").innerHTML += "Decode with " + errors + " errors suceeded in " + (t6 - t5) + "mS</br>";
	console.log("Erasure decode took " + (t6 - t5) + " mS");
	return Promise.resolve(true);
}
function WebSocketUpload(ip,port,fragmentName,fragmentContent,digest)
{
	if ("WebSocket" in window)
	{
		var ws = new WebSocket("ws://"+ip+":"+port);

		ws.onopen = function()
		{
			ws.send("U");
			ws.send(fragmentName);
			console.log(fragmentName);
			ws.send(fragmentContent);
			console.log(fragmentContent);
			ws.send(digest);
		};

		ws.onmessage = function (evt)
		{
			let respondMsg = evt.data;
			console.log(respondMsg);//success or not
		};

		ws.onclose = function()
		{
			console.log("upload closed");
		};
	}
	else
	{
		alert("");//TODO
	}
}
function encodeFile(selectedFile) {
	/* After file selected, get info(name, type, size) as global,
     * and read filestream As ArrayBuffer
     * use FileReader, seemingly usable for Chrome & Firefox
     * turn to upLoader()
     * handleFileSelect(this) -> upLoader
     * */
	// sendFragments((str)fileName,(str)fileType,(int)numOfDivision,(int)numOfAppend,(byte[][])content(content),(string[])digest,(int)fileSize);

	let numOfDivision = 5;
	let numOfAppend = 2;
	var fileType = [];
	var fileName = [];
	var fileSize;
	// TODO temp fix
	var content = [];
	var digest = [];
	/*
     * user choose a file, and trigger handleFileSelect -> upLoader(this)
     * upLoader get the file in *.result as raw, then create a worker to do encoding
     * evt : from                  ¨L not this evt
     *  function handleFileSelect(evt) {
            ...
            var reader = new FileReader();
            ...                 ¨L Maybe this evt(I'm not sure)
            reader.onload = upLoader;
        }
     * */
	function upLoader(evt) {
		/*
		if (document.getElementById("tips") != null)
			document.getElementById("tips").innerHTML = "<h3>Please wait during erasure code profiling...</h3></br>";
		/*receive file*/

		var fileString = evt.target.result;
		/*
		if (document.getElementById("info") != null)
			document.getElementById("info").innerHTML = "loaded as Uint8Array...</br>";
		if (document.getElementById("encode") != null)
			document.getElementById("encode").innerHTML = "";
		if (document.getElementById("decode") != null)
			document.getElementById("decode").innerHTML = "";*/
		let raw = new Uint8Array(fileString);
		/*if (document.getElementById("info") != null)
			document.getElementById("info").innerHTML +=
				"<h3>file name</h3> " + fileName
				+ "</br><h3>file type</h3> " + fileType
				+ "</br><h3>file size</h3> " + fileSize / 1024 + " KB"
				+ "</br>Division " + numOfDivision
				+ " Append " + numOfAppend
				+ "</br></br>";
*/
		// create a worker to do the erasure coding
		/*
		var blob = new Blob(["onmessage = function(e) { postMessage(e.data); }"]);
		// Obtain a blob URL reference to our worker 'file'.
		var blobURL = window.URL.createObjectURL(blob);
		var worker = new Worker(blobURL);
		worker.onmessage = function (e) {
			alert("waiting for worker");
			console.log(e.data);*/
			/*fileEncoder*/
			const t1 = Date.now();//Encode timing start
			//TODO
			content = erasure.split(raw, numOfDivision, numOfAppend);
			const t2 = Date.now();//Encode timing end
			console.log("Erasure encode took " + (t2 - t1) + " mS");
			//if (document.getElementById("encode") != null)
			//	document.getElementById("encode").innerHTML += "Encode took " + (t2 - t1) + "mS to generate " + content.length + " fragments</br>";
			//TODO
			//var digest = new Array();
			const t3 = Date.now();//Hash timing start
			for (var i = 0; i < content.length; i++) {
				digest[i] = objectHash.MD5(content[i]);
			}
			const t4 = Date.now();//Hash timing end
			//if (document.getElementById("encode") != null)
			//	document.getElementById("encode").innerHTML += "Hash took " + (t4 - t3) + "mS to generate " + content.length + " digests</br>";
			/* Next we can use sendFragments() to send the results to the backend,
             * hopefully content[][] remain as 2d array
             * */
			// Here we use decodeFile to test if encode and decode both work properlly.
			//decodeFile(fileName, fileType, numOfDivision, numOfAppend, content, digest, fileSize);
			console.log("Success");
		//};
		//console.log(raw);
		//worker.postMessage({ input: raw });
	}
	if (selectedFile) {
		//console.log(selectedFile);
		fileType = selectedFile.type;
		fileName = selectedFile.name;
		fileSize = selectedFile.size;
		var reader = new FileReader();
		//reader.readAsBinaryString(files[0]);
		reader.onload = upLoader;
		reader.readAsArrayBuffer(selectedFile);
		alert("reading");
	}
	console.log(content);
	return {
		fileName: fileName,
		fileType: fileType,
		numOfDivision: numOfDivision,
		numOfAppend: numOfAppend,
		content: content,
		digest: digest,
		fileSize: fileSize
	}
}
function fileUpload() {

	let selectedFile = document.getElementById('files').files[0];
	let fileInfo = encodeFile(selectedFile);

	var uploadForm = new FormData();
	var deviceArray;
	var fileId;
	var path = "/";
	if(curr_path_array.length>1)
		path="";
	for(var i=1;i<curr_path_array.length;i++)
		path = path + curr_path_array[i] + "/" ;
	uploadForm.append("path", path);
	uploadForm.append("fileName", fileInfo.fileName);
	uploadForm.append("fileType", fileInfo.fileType);
	uploadForm.append("nod", fileInfo.numOfDivision);
	uploadForm.append("noa", fileInfo.numOfAppend);
	uploadForm.append("fileSize", fileInfo.fileSize);
	uploadForm.append("whose", $.cookie("username"));
	$.ajax({
		url: "FileUploader!uploadRegister.action",
		type: "POST",
		data: uploadForm,
		dataType: "text",
		processData: false,
		contentType: false,
		async: false,								//此处采用同步查询进度
		success: function (databack) {
			var retFileInfo = $.parseJSON(databack);
			let result = retFileInfo.result;
			deviceArray = retFileInfo.devices.forms;
			fileId=retFileInfo.fileId;
			console.log(result);
			//alert(result);
		}
	});

	/*
    //错误处理 TODO
    if(result=="NotEnoughFragments")
    {
        $("#statusFeedback").text("在线碎片数目不足！");
        return;
    }
    else if(result == "Error")
    {
        $("#statusFeedback").text("服务器响应该请求内部出错！");
        return;
    }*/
	//i=0;
	//let fragmentName=fileId * 100 + i;
	//WebSocketUpload(deviceArray[i].ip, deviceArray[i].port, fragmentName.toString(), fileInfo.content[i], fileInfo.digest[i]);

	//alert("Before upload");
	for (var i = 0; i < deviceArray.length; i++) {
		WebSocketUpload(deviceArray[i].ip, deviceArray[i].port, (fileId * 100 + i).toString(), fileInfo.content[i], fileInfo.digest[i]);
	}
}
$(document).ready(function(){
	curr_path_array = [];
	curr_path_array[0] = "/";
	curr_path_html = "<li>ROOT</li>";
	
	//面包屑式访问路径显示  初始化
	$("#curr_path").html(curr_path_html);
	
	//文件下载
	$("#button_download").click(
	function()
		{
			var path;
			var name;
			var item=$("#file_list_body").children();
			item = item.next();
			while(item.length!=0)
				{
					name = "";
					path = "";
					//如果ｉｔｅｍ不为空，则进行处理
					var children=item.children();
					if( (children[1].children[1].className=="glyphicon glyphicon-file") && (children[1].children[0].children[0].checked) )
						{
							//文件路径
							path = path + "/";
/*********/					if(curr_path_array.length>1)
								path="";
							for(var i=1;i<curr_path_array.length;i++)
								path = path + curr_path_array[i] + "/" ;
							//文件名
							name = name + $.trim(children[1].innerText);
							//alert(path + "  " + name);


							/*
							 *
							 * 此处应当利用ａｊａｘ　远程调用　downloadRegister(String path, String name)；
							 *
							 * */
							//利用ａｊａｘ　远程调用　downloadRegister(String path, String name)；
							var result;
							var	form=new FormData();
							var deviceArray;
							var fileInfo;
							form.append("path",path);
							form.append("name",name);
							$.ajax({
									url:"FileDownloader!downloadRegister.action",
									type:"POST",
									data:form,
									dataType:"text",
									processData:false,
									contentType:false,
									async: false,								//此处采用同步查询进度
									success:function(databack){
										fileInfo = $.parseJSON(databack);
										result = fileInfo.result;
										deviceArray = fileInfo.devices.forms;
										console.log(result);
										//alert(result);
									}
							});

							//错误处理
							if(result=="NotEnoughFragments")
							{
								$("#statusFeedback").text("在线碎片数目不足！");
								return;
							}
							else if(result == "Error")
							{
								$("#statusFeedback").text("服务器响应该请求内部出错！");
								return;
							}
							var content= new Array(fileInfo.noa+fileInfo.nod);
							var digest= new Array(fileInfo.noa+fileInfo.nod);
							for(var i=0;i<deviceArray.length;i++)
							{
								console.log(deviceArray[i]);
								let received_bytes=WebSocketDownload(deviceArray[i].ip,deviceArray[i].port,deviceArray[i].filename,content,digest,deviceArray[i].fragmentId);
								//console.log(received_bytes);
								console.log('Back');
								//console.log(content[deviceArray[i].fragmentId];
								//createAndDownloadFile(deviceArray[i].filename, 'jpg', received_bytes)
							}
							alert("Downloading File...");
							//setTimeout(myHandler(content,deviceArray),1000);
							decodeFile(fileInfo.name,fileInfo.fileType,fileInfo.nod,fileInfo.noa,content,digest,fileInfo.fileSize);
							/* TODO
							//添加进度条
							var ratio１ = 0;
							var progress_bar='<div class="progress progress-striped active"><div class="progress-bar progress-bar-success" role=\"progressbar" style="width: '
								+ratio１+'%;">'
								+path+name+'</div></div>';
							$("#download_progress_area").append(progress_bar);
							*/


						}
					//
					item = item.next();
				}
		}
	);

	$("#button_upload").click(function() {
		$("#files").click();
	});

	/*
		<tr id="file_list_first">
		<td> </td>
 		<td> <label><input type="checkbox">&emsp;&emsp;</label><span class="glyphicon glyphicon-folder-open"></span>&emsp;../</td>
 		<td></td>
 		<td></td>
		</tr>

*/
	
	
	//点击文件目录进入其子目录　　刷新文件目录列表
	$("#file_list_body").on("click","tr.file_list_go",
			function()
			{
				//如果是文件而不是文件夹，点击不刷新目录，提示信息
				if(this.children[1].children[1].className=="glyphicon glyphicon-file")
				{
					$("#statusFeedback").text("您所点击的是文件而不是文件夹，无法进入该目录！");
					return;
				}
				//更新路径显示
				curr_path_array = curr_path_array.concat( $.trim(this.children[1].innerText) ) //此处用$.trim去除空格
				curr_path_html = "<li>ROOT</li>";
				for(var i=1;i<curr_path_array.length;i++)
				curr_path_html = curr_path_html + "<li>" + curr_path_array[i] + "</li>";
				$("#curr_path").html(curr_path_html);		
				//ajax
				var QueryPath="/";
/*********/		if(curr_path_array.length>1)
					QueryPath="";
				for(var i=1;i<curr_path_array.length;i++)
				{
					QueryPath = QueryPath + curr_path_array[i] + "/" ;
				}
				var	form=new FormData();

				var whose = $.cookie("username");
				form.append("Whose", whose);
				form.append("Path",QueryPath);
				//console.log(form.get("whose"));
				//alert(queryPath);
				$.ajax({
						url:"GetFileList.action",
						type:"POST",
						data:form,
						dataType:"text",
						processData:false,
						contentType:false,
						success:function(databack){
							var obj = $.parseJSON(databack);
							var new_file_list = obj.html;
							//alert(new_file_list);
							$("#file_list_body").html(new_file_list);
						}
				});
				$("#statusFeedback").text("成功进入该目录！");
			}
	);
	
	//点击的是返回上一层的文件项
	$("#file_list_body").on("click","tr.file_list_back",
			function()
			{
				//如果是顶层目录，点击上级目录无操作，提示信息
				if(curr_path_array.length==1)
				{
					$("#statusFeedback").text("已经是根目录了，无法返回上一层！");
					return; 
				}
				//更新路径显示
				curr_path_array.pop();
				curr_path_html = "<li>ROOT</li>";
				for(var i=1;i<curr_path_array.length;i++)
				curr_path_html = curr_path_html + "<li>" + curr_path_array[i] + "</li>";
				$("#curr_path").html(curr_path_html);	
				
				//ajax
				var QueryPath="/";
/*********/		if(curr_path_array.length>1)
					QueryPath="";
				for(var i=1;i<curr_path_array.length;i++)
				{
					QueryPath = QueryPath + curr_path_array[i] + "/" ;
				}
				var	form=new FormData();
				form.append("Path",QueryPath);
				var whose = $.cookie("username");
				form.append("Whose", whose);
				
				//alert(queryPath);
				$.ajax({
						url:"GetFileList.action",
						type:"POST",
						data:form,
						dataType:"text",
						processData:false,
						contentType:false,
						success:function(databack){
							var obj = $.parseJSON(databack);
							var new_file_list = obj.html;
							//alert(new_file_list);
							$("#file_list_body").html(new_file_list);
						}
				});
				$("#statusFeedback").text("成功返回上层目录！");
			}
	);

	
	//定时刷新预下载进度
	function refresh_progress(){
		var progressArray = $("#download_progress_area").children();
		var str="";
		var ratio=100;
		for(var i=0;i<progressArray.length;i++)
		if(progressArray[i].className=="progress progress-striped active")
		{
			//alert("here length="+progressArray.length + "i="+i);
			var path="";
			var name="";
			var strArray;
			strArray = progressArray[i].innerText.split('/');
			for(var j=0;j<strArray.length-1;j++)
				path = path + strArray[j] + "/";
			name = strArray[strArray.length-1];
			//str = str + path + name + "    ";
			//alert(name+" "+path)
			/*
			 * 
			 * 此处应远程调用　public static int progressCheck(String path, String name)　　返回进度
			 * 
			 * */
			var	form=new FormData();
			form.append("path",path);
			form.append("name",name);
			$.ajax({
					url:"FileDownloader!progressCheck.action",
					type:"POST",
					data:form,
					dataType:"text",
					processData:false,
					contentType:false,
					async: false,								//此处采用同步查询进度
					success:function(databack){
						var obj = $.parseJSON(databack);
						var result = obj.result;
						if(result == "Error")
						{
							ratio = 0;
							$("#statusFeedback").text("查询进度出错！");
						}
						else
							ratio = parseInt(result);

					}
			});

			//////////////////////////////////////////////////////////////////
			//进度条的ｈｔｍｌ代码
			var progress_bar='<div class="progress progress-striped active"><div class="progress-bar progress-bar-success" role=\"progressbar" style="width: '
				+ratio+'%;">'
				+path+name+'</div></div>';
			//如果预下载完成
			if(ratio==100)
			{
				/*
				 * 
				 * 
				 * 此处应当调用远程函数　　public static int decodeFile(String path, String name)
				 * 
				 * */
				var	form=new FormData();
				form.append("path",path);
				form.append("name",name);
				$.ajax({
						url:"FileDownloader!decodeFile.action",
						type:"POST",
						data:form,
						dataType:"text",
						processData:false,
						contentType:false,
						async: false,								//此处采用同步查询进度
						success:function(databack){
							var obj = $.parseJSON(databack);
							var result = obj.result;
							if(result == "Error")
								$("#statusFeedback").text("解码拼接出错！");
							else
								$("#statusFeedback").text("解码拼接文件成功！");

						}
				});
				
				
				var temp = '<a href="/DFS/CloudDriveServer/tmpFile/' + name + '" download="' + name + '">' + progress_bar + '</a>';
				//alert(temp);
				progressArray[i].outerHTML = temp;	
				
			}
			else
			{
				//修改进度条进度
				//alert(progress_bar);
				progressArray[i].outerHTML = progress_bar;				
			}
			///////////////////////////////////////////////////////
		}
		
	}
	//设置进度刷新间隔
	window.setInterval(function(){refresh_progress();},3000);

	
	
	
	//自动删除下载过的文件链接和进度条
	$("#download_progress_area").on("click","a",
			function()
			{
				this.outerHTML = "";
			}

	);
	
	
	
//总的结束符	
});

/*
   			<tr id="file_list_first">
      			<td> </td>
         		<td> <label><input type="checkbox">&emsp;&emsp;</label><span class="glyphicon glyphicon-folder-open"></span>&emsp;../</td>
         		<td></td>
         		<td></td>
      		</tr>
 
 */