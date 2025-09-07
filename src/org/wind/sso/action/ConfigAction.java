package org.wind.sso.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wind.mvc.annotation.controller.An_Controller;
import org.wind.mvc.annotation.controller.An_URL;
import org.wind.sso.bean.AuthData;
import org.wind.sso.model.AuthCookie;
import org.wind.sso.model.AuthLogin;
import org.wind.sso.util.system.SystemUtil;

/**
 * @描述 : 配置Action——专门用来获取配置
 * @作者 : 胡璐璐
 * @时间 : 2020年11月26日 09:09:43
 */
@An_Controller("/config")
public class ConfigAction {
	
	/**sso**/
	public static final Map<String,Object> ssoMap=new HashMap<String, Object>();
	
	/**sso配置（核心）**/
	@An_URL
	public Map<String,Object> sso(HttpServletRequest request, HttpServletResponse response) throws Exception{
		SystemUtil.isAllowAccess(request);		//是否允许访问
		AuthData obj=SystemUtil.getAuthData();
		AuthCookie authCookie=obj.getAuthCookie();
		AuthLogin authLogin=obj.getAuthLogin();
		Map<String,Object> ssoMap=new HashMap<String, Object>();
		/*cookie*/
		if(authCookie!=null) {
			ssoMap.put("domain", authCookie.getDomain());
			//
			Map<Object,Object> cookieMap=new HashMap<Object, Object>();
			Map<Object,Object> cookieMap_ssoId=new HashMap<Object, Object>();
			cookieMap_ssoId.put("path", authCookie.getSsoIdPath());
			cookieMap_ssoId.put("name",authCookie.getSsoIdName());
			cookieMap.put("ssoId", cookieMap_ssoId);
			//
			ssoMap.put("cookie", cookieMap);
		}else{
			throw new RuntimeException("【"+obj.getAuth().getDomain()+"】缺少cookie的配置");
		}
		/*认证服务器*/
		if(authLogin!=null) {
			ssoMap.put("authUrl", authLogin.getUrl());		//认证服务器URL（根路径，SSO调用登录接口所在的服务器URL，一般绑定的是wind-bg服务的url）	
		}else{
			throw new RuntimeException("【"+obj.getAuth().getDomain()+"】缺少认证服务器URL配置（登录URL的根路径）");
		}
		return ssoMap;
	}
	
}
