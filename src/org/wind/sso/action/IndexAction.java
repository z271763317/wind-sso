package org.wind.sso.action;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wind.mvc.annotation.controller.An_Controller;
import org.wind.mvc.annotation.controller.An_URL;
import org.wind.mvc.bean.URLMethod;
import org.wind.sso.bean.AuthData;
import org.wind.sso.model.AuthCachePrefix;
import org.wind.sso.model.AuthCacheRedis;
import org.wind.sso.model.AuthCookie;
import org.wind.sso.model.AuthLogin;
import org.wind.sso.util.RegexUtil;
import org.wind.sso.util.ToolUtil;
import org.wind.sso.util.system.SSOUtil;
import org.wind.sso.util.system.SystemUtil;
import org.wind.sso.util.system.Yzm;
import org.wind.sso.util.system.YzmUtil;

import redis.clients.jedis.Jedis;

/**
 * @描述 : 首页Action
 * @作者 : 胡璐璐
 * @时间 : 2020年11月19日 08:59:29
 */
@An_Controller("/index")
public class IndexAction {

	/**登录页面**/
	@An_URL(method= {URLMethod.GET})
	public String index(HttpServletRequest request, HttpServletResponse response,String referer) throws Exception{
		if(referer==null){
			throw new IllegalArgumentException("缺少来源URL（referer参数）");
		}else{
			String ssoId=SSOUtil.getSsoId(request);
			try {
				Map<String,String> ssoMap=SSOUtil.getSSO(ssoId, ToolUtil.getRequestIP(request));
				//已登录过
				if(ssoMap!=null && ssoMap.size()>0) {
					String newSsoId=ssoMap.get(SSOUtil.key_ssoId);		//重新登录的
					if(newSsoId!=null) {
						AuthData authData=SystemUtil.getAuthData();
						AuthCookie obj=authData.getAuthCookie();
						SSOUtil.addCookie_ssoId(response, newSsoId, obj.getSsoIdExpiry());
					}
					response.sendRedirect(referer);
					return null;
				}else{
					AuthData obj=SystemUtil.getAuthData();
					AuthLogin objAl=obj.getAuthLogin();
					if(objAl!=null) {
						//this.generateSession(request, response);		//设置会话ID
						byte pageFile[]=objAl.getPageFile();
						if(pageFile==null || pageFile.length<=0) {
	//						request.setAttribute("referer", referer);
							request.setAttribute("name", obj.getAuth().getName());
							return "";
						}else{
							response.setCharacterEncoding("UTF-8");		//编码方式
							response.setContentType("text/html");		//设置为html格式
							ServletOutputStream os=response.getOutputStream();
							os.write(pageFile);
							os.close();		//关闭，使之后面的不用继续执行
							return null;
						}
					}else{
						throw new IllegalArgumentException("该域名未设置登录信息");
					}
				
				}
			}catch(Exception e) {
				return "";
			}
		}
	}
	/**验证码**/
	@An_URL("/captcha")
	public void captcha(HttpServletResponse response,String userName) throws Exception{
		userName=RegexUtil.clearEmpty(userName);
		if(userName!=null) {
			AuthData obj=SystemUtil.getAuthData();
			AuthLogin objAl=obj.getAuthLogin();
			int pwdErrorNumYzm=objAl.getPwdErrorNumYzm();
			//开启验证码
			if(pwdErrorNumYzm>=0) {
				byte[] captcha = null;		//验证码图片数据
				/*将生成的验证码保存在session中*/
				Jedis jedis=SystemUtil.getJedis();
		        try {
		        	AuthCacheRedis objRedis=obj.getAuthCacheRedis();
		        	AuthCachePrefix objPrefix=obj.getAuthCachePrefix();
		        	//
		        	boolean isYzm=SystemUtil.getIsYzm(jedis,userName);
		        	//是否开启验证码
		        	if( isYzm) {
			        	Yzm objYzm=YzmUtil.generate();
			        	String createText = objYzm.getText();
			        	captcha = objYzm.getImage();
			        	jedis.set(objPrefix.getCaptcha()+userName, createText);
			        	jedis.expire(objPrefix.getCaptcha()+userName, objRedis.getCaptchaExpiry());
		        	}
		        }finally{
		        	jedis.close();
		        }
		        //显示验证码
		        if(captcha!=null) {
				    response.setHeader("Cache-Control", "no-store");
				    response.setHeader("Pragma", "no-cache");
				    response.setDateHeader("Expires", 0);
				    response.setContentType("image/jpeg");
				    ServletOutputStream sout = null;
				    try {
				    	sout = response.getOutputStream();
				        sout.write(captcha);
				        sout.flush();
				    }catch(IOException e) {
				    	throw e;
				    }finally {
				    	if(sout!=null){try {sout.close();}catch(Exception e) {}}
				    }
		        }
			}
		}
	}
}
