package org.wind.sso.util.system;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wind.sso.bean.AuthData;
import org.wind.sso.config.URL;
import org.wind.sso.model.AuthCachePrefix;
import org.wind.sso.model.AuthCookie;
import org.wind.sso.model.AuthLogin;
import org.wind.sso.model.AuthSSO;
import org.wind.sso.service.time.TimeService_delete_sso;
import org.wind.sso.service.time.TimeService_save;
import org.wind.sso.util.HttpClientSupport;
import org.wind.sso.util.JsonUtil;
import org.wind.sso.util.RegexUtil;
import org.wind.sso.util.Response;

import redis.clients.jedis.Jedis;

/**
 * @描述 : SSO工具类
 * @数据格式 : json
 * @作者 : 胡璐璐
 * @时间 : 2021年5月12日 22:02:29
 */
@SuppressWarnings("unchecked")
public final class SSOUtil {

	/*****************sso里的key列表*****************/
	public static final String key_ssoId="ssoId";			//ssoId
	public static final String key_session="session";		//session数据
	public static final String key_expiry="expiry";		//过期时间
	
	//不允许实例化
	private SSOUtil(){
		//请不要试图实例化我
	}
	/*********************ssoId*********************/
	/**生成 : 当前会话单点登录的唯一ID**/
	public static String generate_ssoId(){
		return UUID.randomUUID().toString().replace("-", "");
	}
	/**获取 : 当前会话Cookie的本站ssoId**/
	public static String getSsoId(HttpServletRequest request){
		Cookie cookieArr[]=request.getCookies();
		AuthData obj=SystemUtil.getAuthData();
		String t_cookie_name=obj.getAuthCookie().getSsoIdName();
		for(int i=0;cookieArr!=null && i<cookieArr.length;i++){
			Cookie c=cookieArr[i];
			String t_name=c.getName();
			//指定cookie名
			if(t_name!=null && t_name.equals(t_cookie_name)){
				return c.getValue();
			}
		}
		return null;
	}
	/**添加 : Cookie信息——ssoId**/
	public static void addCookie_ssoId(HttpServletResponse response,String ssoId,int expiry) throws UnsupportedEncodingException{
		AuthData obj=SystemUtil.getAuthData();
		AuthCookie objCookie=obj.getAuthCookie();
		Cookie cookie=new Cookie(objCookie.getSsoIdName(), ssoId);
		cookie.setMaxAge(expiry);
		cookie.setPath(objCookie.getSsoIdPath());
		cookie.setDomain(objCookie.getDomain());
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}
	
	/**
	 * 登录（返回session数据，出错或失败抛出异常）
	 * @throws IllegalArgumentException : 主动错误，可接收并返回给前端 
	 */
	public static String login(String userName,String passWord) throws IllegalArgumentException,Exception {
		AuthData authData=SystemUtil.getAuthData();
		AuthLogin objLogin=authData.getAuthLogin();
		//
		HttpClientSupport hcs=HttpClientSupport.getInstance().generatorHttpClient();
		//
		Map<String,String> loginParamMap=new HashMap<String,String>();
		loginParamMap.put(objLogin.getParamUserName(), userName);
		loginParamMap.put(objLogin.getParamPassWord(), passWord);
		//调用登录接口，验证用户身份（用户名和密码）是否正确
		Response httpResponse=hcs.post(objLogin.getUrl()+URL.login, loginParamMap, null);
		/*
		 * 返回的数据格式：
		 * {
		 * 		"code":"【状态码。1=成功，0=失败】",
		 * 		"message":"【信息，一般是失败后返回的错误信息】",
		 * 		"session":【Map对象。存储传统形式的登录成功后的会话信息】
		 * }
		 * 
		 */
		Map<String,Object> loginResultMap=JsonUtil.toObject(httpResponse.asString(), Map.class);
		if(loginResultMap!=null && loginResultMap.size()>0) {
			Integer t_code=(Integer) loginResultMap.get("code");
			if(t_code!=null && t_code.equals(1)) {
				Object sessionObj=loginResultMap.get("session");
				String sessionStr=null;
				if(sessionObj instanceof Map) {
					Map<Object,Object> sessionMap=(Map<Object, Object>) loginResultMap.get("session");
					sessionStr=JsonUtil.toJson(sessionMap);
				}else{
					sessionStr=sessionObj.toString();
				}
				return sessionStr;
			}else{
				String message=(String) loginResultMap.get("message");
				throw new IllegalArgumentException(message);
			}
		}else{
			throw new IllegalArgumentException("远程登录服务异常");
		}
	}
	/*********************SSO对象*********************/
	public static Map<String,String> getSSO(String ssoId,String ip) throws Exception{
		Map<String,String> ssoMap=new HashMap<String,String>();
		//
		String session=getSession(ssoId);
		if(session==null) {
			AuthSSO objSSO=getAuthSSO(ssoId, ip);
			if(objSSO!=null) {
				try {
					String sessionStr=login(objSSO.getUserName(), objSSO.getPassWord());		//登录
					//String newSsoId=generate_ssoId();		//新的SSO
					String newSsoId=ssoId;
					AuthData authData=SystemUtil.getAuthData();
					SSOUtil.put(newSsoId, sessionStr,authData.getAuthCacheRedis().getSsoIdExpiry());
					ssoMap.put(key_ssoId, ssoId);
					ssoMap.put(key_session, sessionStr);
					//
					AuthSSO objSSO_save=new AuthSSO();
					objSSO_save.setId(objSSO.getId());
					objSSO_save.setCreateTime(RegexUtil.getDate());
					TimeService_save.add(objSSO_save);
				}catch(IllegalArgumentException e) {
					//删除错误信息
					TimeService_delete_sso.add(ssoId);
					throw e;
				}
			}else{
				return null;
			}
		}else{
			ssoMap.put(key_session, session);
		}
		AuthData obj=SystemUtil.getAuthData();
		ssoMap.put(key_expiry, obj.getAuthCookie().getSsoIdExpiry().toString());
		return ssoMap;
	}
	
	/**放入 : SSO到缓存**/
	public static void put(String ssoId,String sessionStr,int expiry){
		if(ssoId!=null){
			Jedis jedis=SystemUtil.getJedis();
			try {
				AuthData obj=SystemUtil.getAuthData();
				AuthCachePrefix objPrefix=obj.getAuthCachePrefix();
				jedis.set(objPrefix.getSsoId()+ssoId, sessionStr);
				jedis.expire(objPrefix.getSsoId()+ssoId, expiry);
			}finally {
				jedis.close();
			}
		}
	}
	
	/**保存 : session**/
	public static void saveSession(String ssoId,String session){
		if(ssoId!=null){
			Jedis jedis=SystemUtil.getJedis();
			try {
				AuthData obj=SystemUtil.getAuthData();
				AuthCachePrefix objPrefix=obj.getAuthCachePrefix();
				jedis.set(objPrefix.getSsoId()+ssoId,session);
			}finally {
				jedis.close();
			}
		}
	}
	/**获取 : session数据**/
	public static String getSession(String ssoId){
		String session=null;
		if(ssoId!=null){
			Jedis jedis=SystemUtil.getJedis();
			try {
				AuthData obj=SystemUtil.getAuthData();
				AuthCachePrefix objPrefix=obj.getAuthCachePrefix();
				session=jedis.get(objPrefix.getSsoId()+ssoId);
				if(session!=null) {
					updateExpiry(jedis, ssoId);
				}
			}finally{
				jedis.close();
			}
		}
		return session;
	}
	/**获取 : AuthSSO对象（返回为空，说明没有登录，或者非法IP）**/
	public static AuthSSO getAuthSSO(String ssoId,String ip) throws ParseException {
		AuthData obj=SystemUtil.getAuthData();
		boolean isValidIp=obj.getAuthLogin().getIsValidIp();
		//
		AuthSSO objSSO=AuthSSO.getBySsoId(ssoId);
		if(objSSO!=null) {
			//是否验证客户端IP
			if(isValidIp && !objSSO.getIp().equals(ip)) {
				return null;
			}
			int expiry=objSSO.getExpiry();
			String updateTime=objSSO.getCreateTime();
			long cha=Math.abs(RegexUtil.getDataDifference(updateTime));
			//超过了
			if(cha>(expiry*1000)) {
				TimeService_delete_sso.add(ssoId);
			}else{
				return objSSO;
			}
		}
		return null;
	}
	
	/*********************其他*********************/
	//更新过期时间（从指定的ssoId数据里取出。指定jedis式）
	private static void updateExpiry(Jedis jedis,String ssoId) {
		AuthData obj=SystemUtil.getAuthData();
		AuthCachePrefix objPrefix=obj.getAuthCachePrefix();
		//
		Integer expiry=obj.getAuthCacheRedis().getSsoIdExpiry();
		jedis.expire(objPrefix.getSsoId()+ssoId, expiry);
	}
	/**删除（退出） : 当前会话的所有信息（含：seesion）**/
	public static void exit(HttpServletRequest request,String ssoId){
		if(ssoId!=null){
			TimeService_delete_sso.add(ssoId);
		}
	}
	
}