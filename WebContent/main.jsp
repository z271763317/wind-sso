<%@ page language="java" pageEncoding="UTF-8" session="false" %>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<base href="<%=basePath%>" />
	<title>首页跳转</title>
</head>
<body>
	<jsp:forward page="index"></jsp:forward>
</body>
</html>