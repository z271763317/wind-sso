package org.wind.sso.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.wind.mvc.annotation.controller.An_Controller;
import org.wind.mvc.annotation.controller.An_URL;
import org.wind.sso.bean.AuthData;
import org.wind.sso.config.cache.Cache;
import org.wind.sso.model.AuthCachePrefix;
import org.wind.sso.model.AuthCookie;
import org.wind.sso.model.AuthLogin;
import org.wind.sso.model.AuthSSO;
import org.wind.sso.util.RegexUtil;
import org.wind.sso.util.ToolUtil;
import org.wind.sso.util.ValidateUtil;
import org.wind.sso.util.system.SSOUtil;
import org.wind.sso.util.system.SystemUtil;

import redis.clients.jedis.Jedis;

/**
 * @描述 : 登录Action
 * @作者 : 胡璐璐
 * @时间 : 2020年8月2日 12:39:36
 */
@An_Controller("/login")
public class LoginAction {
	
	/**登录**/
	@An_URL
	public Map<String,Object> index(HttpServletRequest request, HttpServletResponse response,HttpSession session,String userName,String passWord,String captcha) throws Exception{
		userName=RegexUtil.clearEmpty(userName);
		ValidateUtil.notEmpty(userName, "缺少【用户名】");
		ValidateUtil.notEmpty(passWord, "缺少【密码】");
		//返回数据
		Map<String,Object> resultMap=new HashMap<String,Object>();
		resultMap.put("code", Cache.response_code_failure);
		String message=null;
		//
		AuthData authData=SystemUtil.getAuthData();
		AuthCookie obj=authData.getAuthCookie();
    	AuthCachePrefix objPrefix=authData.getAuthCachePrefix();
		AuthLogin objLogin=authData.getAuthLogin();
		//
		String ssoId_source=SSOUtil.getSsoId(request);
		String ip=ToolUtil.getRequestIP(request);		//请求者IP
		Map<String,String> ssoMap=SSOUtil.getSSO(ssoId_source, ip);
		//已登录过
		if(ssoMap!=null && ssoMap.size()>0) {
			String newSsoId=ssoMap.get(SSOUtil.key_ssoId);		//重新登录的
			if(newSsoId!=null) {
				SSOUtil.addCookie_ssoId(response, newSsoId, obj.getSsoIdExpiry());
			}
			//
			resultMap.put("code", Cache.response_code_success);		//成功
			if(objLogin.getIsReturnSsoId()) {
				resultMap.put("ssoId", ssoId_source);
			}
			return resultMap;
		}else{
			Integer expiry=obj.getSsoIdExpiry();
			Jedis jedis=SystemUtil.getJedis();
			try {
				boolean isYzm=SystemUtil.getIsYzm(jedis,userName);
				//开启验证码
				if(isYzm) {
					resultMap.put("isYzm", isYzm);
					ValidateUtil.notEmpty(captcha, "缺少【验证码】");
					String t_captchaSource=jedis.get(objPrefix.getCaptcha()+userName);
					if(t_captchaSource!=null) {
						if(!t_captchaSource.equalsIgnoreCase(captcha)) {
							throw new IllegalArgumentException("验证码错误");	
						}
					}else{
						throw new IllegalArgumentException("请刷新验证码");
					}
				}
				try {
					String sessionStr=SSOUtil.login(userName, passWord);
					if(sessionStr!=null) {
						String ssoId=SSOUtil.generate_ssoId();		//单点登录会话ID
						/*SSO对象*/
						SSOUtil.put(ssoId, sessionStr,authData.getAuthCacheRedis().getSsoIdExpiry());
						SSOUtil.addCookie_ssoId(response, ssoId, expiry);
						
						jedis.del(objPrefix.getCaptcha()+userName);		//删除验证码
						jedis.del(objPrefix.getPwdErrorNum()+userName);		//删除错误数
						//
						resultMap.put("code", Cache.response_code_success);		//成功
						if(objLogin.getIsReturnSsoId()) {
							resultMap.put("ssoId", ssoId);
						}
						AuthSSO.add(ssoId, expiry, ip, userName, passWord);
						return resultMap;
					}
				}catch(IllegalArgumentException e) {
					isYzm=SystemUtil.increasePwdErrorNum(jedis, userName);		//增加1次失败
        			if(isYzm) {
        				resultMap.put("isYzm", isYzm);
        			}
					throw e;
				}
			}catch(IllegalArgumentException e) {
				message=e.getMessage();
			}finally {
				jedis.close();
			}
		}
		if(message!=null) {
			resultMap.put("message", message);
		}
		return resultMap;
	}

}
