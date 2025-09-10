-- 需要安装

	mysql：5.x以上版本（尽量使用最新版）
	
	redis：5以上（尽量使用最新版）
	
	jdk：8以上
	
	tomcat：9.x（最高）

-- 配置文件（在【src/wind-sso.properties】）

	#域名（当前部署的SSO绑定使用的域名）
	domain=127.0.0.1:8080
	
	#【tcp、http连接超时时间】 
	conTimeout=15000
	#【tcp、http读取超时时间】
	readTimeout=45000
	#【HttpClient连接池最大连接数】
	conPoolSize=50
	#【HttpClient每个路由最大连接数】
	routeConSize=10
	
	#异步线程数_删除_sso（单数据。默认：1）
	thread_delete_sso=3
	#异步线程数_删除_用户名（指定用户名下的所有sso。默认：1）
	thread_delete_sso_userName=2
	#异步线程数_插入（单数据。默认：1）
	thread_insert=3
	#异步线程数_保存（单数据。默认：1）
	thread_save=2

-- 导入数据库

    将【wind-sso.sql】导入到数据库，数据库名：wind-sso（若修改，则相关配置也一并修改）。根据实际情况配置（有例子和表字段说明），若不想在该处修改，可部署【wind-sso-bg】系统，通过界面修改配置

-- SSO核心配置

 	需要在数据库表设置，或部署【wind-sso-bg】后台管理系统，通过界面化配置

-- 步骤（Java版）
	
	1、子系统导入【wind-sso-client.jar】，并在根目录下（工程在src，web在classese）下引入【wind-sso-client.properties】文件，并配置好相应的参数（详情查看该文件）
	
	2、wind-sso-client方法介绍：
	
	  （1）、SSOUtil.isLogin：是否已登录
	
	  （2）、SSOUtil.getLoginPageUrl：获取登录页面URL
	  
	  （3）、SSOUtil.getLoginPageUrlParam：获取登录页面URL（带参数）
	
	  （4）、SSOUtil.exit：退出（清除在redis里的会话信息）
	
	  （5）、SSOUtil.getSSO：获取SSO对象（附带：ssoId会话唯一标识、、session数据、session的代理对象等）

   3、子系统拦截器（具有登录验证功能）在进入控制器方法前判断是否已登录，若未登录请先【获取登录页面URL（带参数）】，然后进行【重定向】跳转（SSO登录页面）。子系统首页也应该执行该逻辑，并且已登录则进入后台操作地，例：

   		<%@page import="org.wind.sso.client.util.SSOUtil"%>
		<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
		<%
			boolean isLogin=SSOUtil.isLogin(request, response);
			//已登录
			if(isLogin){
				response.sendRedirect("user");
			}else{
				String loginPageUrl=SSOUtil.getLoginPageUrlParam(request);
				response.sendRedirect(loginPageUrl);
			}
		%>
		<!DOCTYPE html>
		<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
			<title>登录跳转</title>
		</head>
		</html>

   4、一切操作完毕后，并且部署启动成功后，请访问该系统的首页URL（或模块主页URL）验证
	
-- 通用接入
	
	1、同【步骤1】
	
	2、接口列表：
	
	  认证服务器：SSO调用登录接口所在的服务器URL，一般绑定的是【具有用户登录、注册接口】的服务。
	
	  请求方法：GET、POST（推荐）
	
	  （1）、获取配置：
	  
	        URL：【sso服务端url】/config
	
	        参数：无
	
	        响应：json结构。如：
	
	            {
	              "code":【状态码。1=成功；0=失败】,
	              "message":"消息，若code为0，则一般会产生",
	              "domain":"【当前域名】",
	              "cookie":{
	                "ssoId":{
	                  "name":"【ssoId的自定义名，如：_wind-sso_ssoId_1】",
	                  "path":"【ssoId的path，如：/】"
	                }
	              },
	              "authUrl":"【认证服务器URL】"
	            }
	
	  （2）、注册：
	
	        URL：【sso服务端url】/reg
	
	        参数：
	
	          参数1 = 【认证服务器的注册接口参数1，如：用户名】
	          参数2 = 【认证服务器的注册接口参数2，如：密码】
	          .............
	          参数N = 【认证服务器的注册接口参数N，如：确认密码】
	           
	        响应：json结构。如：
	
	           {
	              "code":【状态码。1=成功；0=失败】,
	              "message":"消息，若code为0，则一般会产生"
	           }
	
	  （3）、登录（登录页面使用）：
	
	        URL：【sso服务端url】/login
	
	        参数：
	
	          userName = 【用户名】
	          passWord = 【密码】
	          captcha  = 【验证码，用户名登录失败一定次数后会开启需要】
	          
	        响应：json结构。如：
	
	           {
	              "code":【状态码。1=成功；0=失败】,
	              "message":"消息，若code为0，则一般会产生",
	              "ssoId":"【单点会话唯一凭证】",
	              "isYzm":"【是否开启验证码。用户名登录失败一定次数后为true】"
	           }
	
	  （4）、获取：SSO（对象，可用来判断是否已登录）：
	
	        要求：调用者IP有在对应域名SSO的白名单内
	
	        URL：【sso服务端url】/user/getSSO
	
	        参数：
	
	          ssoId = 【单点会话唯一凭证】
	          ip = 【已登录的用户IP】
	
	        响应：json结构。如：
	
	           {
	              "code":【状态码。1=成功；0=失败】,
	              "message":"消息，若code为0，则一般会产生",
	              "sso":{
	                "ssoId":"【单点会话唯一凭证】",
	                "session":"【会话数据，调用认证服务器登录接口产生，建议json格式】"
	              }
	           }
	
	  （5）、保存：会话信息
	
	        要求：session数据是json格式。调用者IP有在对应域名SSO的白名单内
	
	        URL：【sso服务端url】/user/saveSession
	
	        参数：
	
	          ssoId = 【单点会话唯一凭证】
	          handlerSession = 【被处理的会话。key=原seesion（或新）的key，value=Map、json对象
		                 				  （key=操作【type、value】；value=对应操作的值【type=1是添加或修改，2=删除；value=则是type=1的时候的值】）
	                            】
	
	        响应：json结构。如：
	
	           {
	              "code":【状态码。1=成功；0=失败】,
	              "message":"消息，若code为0，则一般会产生"
	           }
	
	  （6）、退出
	
	        要求：调用者IP有在对应域名SSO的白名单内
	        
	        URL：【sso服务端url】/user/exit
	
	        参数：
	
	          ssoId = 【单点会话唯一凭证】
	
	        响应：json结构。如：
	
	           {
	              "code":【状态码。1=成功；0=失败】,
	              "message":"消息，若code为0，则一般会产生"
	           }
	
	  （7）、退出全部（退出指定用户名所有的会话）
	
	        要求：调用者IP有在对应域名SSO的白名单内
	
	        URL：【sso服务端url】/user/exitAll
	
	        参数：
	
	          userName = 【用户名】
	
	        响应：json结构。如：
	
	           {
	              "code":【状态码。1=成功；0=失败】,
	              "message":"消息，若code为0，则一般会产生"
	           }
	
	  （8）、获取 : 登录URL
	
	        要求：调用者IP有在对应域名SSO的白名单内
	
	        URL：【sso服务端url】/user/getLoginUrl
	
	        参数：无
	
	        响应：json结构。如：
	
	           {
	              "code":【状态码。1=成功；0=失败】,
	              "message":"消息，若code为0，则一般会产生",
	              "href":"【sso的登录URL】"
	           }



