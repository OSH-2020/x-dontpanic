$(document).ready(function(){
//注册ａｊａｘ
  $("#regSubmitButton").click(function(){
	  var	userName=$("#inputUsername_reg").val();
	  var	userPasswd=$("#inputPassword_reg").val();
	  var	form=new FormData();
	  form.append("userName",userName);
	  form.append("userPasswd",userPasswd);
	  $.ajax({
    	url:"UserReg.action",
    	type:"POST",
    	data:form,
    	dataType:"text",
    	processData:false,
	  	contentType:false,
    	success:function(databack){
    		var obj = $.parseJSON(databack);
    		var feedback = obj.result;
    		//alert(userName);
    		$("#statusFeedback").text(feedback);
    	}
    });
  });
 //登录ａｊａｘ 
  $("#loginSubmitButton").click(function(){
	  var	userName=$("#inputUsername_login").val();
	  var	userPasswd=$("#inputPassword_login").val();
	  var	form=new FormData();
	  var	str=new String("login sucessfully!");
	  form.append("userName",userName);
	  form.append("userPasswd",userPasswd);
	  $.ajax({
    	url:"UserLogin.action",
    	type:"POST",
    	data:form,
    	dataType:"text",
    	processData:false,
	  	contentType:false,
    	success:function(databack){
   			var obj = $.parseJSON(databack);
   			var feedback = obj.result;
    		if(feedback==str)
    			window.location.href='jsp/majorPage.jsp';
    		//格式是ｊｓｏｎ　输出反馈信息到ｃｏｎｓｏｌｅ
    		else
    	   		$("#statusFeedback").text(feedback);
    	}
    });
  });
  
});