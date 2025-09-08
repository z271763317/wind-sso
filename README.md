-- 环境

mysql：5.x以上版本（尽量使用最新版）

redis：5以上（尽量使用最新版）

jdk：8以上

tomcat：9.x（最高）

-- 步骤（Java版）

1、将【wind-sso.sql】导入到数据库，根据实际情况配置（有例子和表字段说明），若不想在该处修改，可部署【wind-sso-bg】系统，通过界面修改配置

2、子系统导入【wind-sso-client.jar】，并在根目录下（工程在src，web在classese）下引入【wind-sso-client.properties】文件，并配置好相应的参数（详情查看该文件）

3、wind-sso-client方法介绍：

  （1）、SSOUtil.isLogin：是否已登录

  （2）、SSOUtil.getLoginPageUrl：获取登录页面URL
  
  （3）、SSOUtil.getLoginPageUrlParam：获取登录页面URL（带参数）

  （4）、SSOUtil.exit：退出（清除在redis里的会话信息）

  （5）、SSOUtil.getSSO：获取SSO对象（附带：ssoId会话唯一标识、、session数据、session的代理对象等）

-- 通用接入

1、同【步骤1】

2、接口列表：

  认证服务器：SSO调用登录接口所在的服务器URL，一般绑定的是【具有用户登录、注册接口】的服务

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

           {
              "【参数1】":"【认证服务器的注册接口参数1，如：用户名】",
              "【参数2】":"【认证服务器的注册接口参数2，如：密码】",
              "【.....】":"........",
              "【参数N】":"【认证服务器的注册接口参数N，如：确认密码】",
           }

        响应：json结构。如：

           {
              "code":【状态码。1=成功；0=失败】,
              "message":"消息，若code为0，则一般会产生"
           }

  （2）、是否已登录：



