package org.wind.sso.bean;

import java.util.List;
import java.util.Set;

import org.wind.sso.model.Auth;
import org.wind.sso.model.AuthCache;
import org.wind.sso.model.AuthCacheRedis;
import org.wind.sso.model.AuthCacheRedisPool;
import org.wind.sso.model.AuthCachePrefix;
import org.wind.sso.model.AuthCacheRedisSentinel;
import org.wind.sso.model.AuthCookie;
import org.wind.sso.model.AuthLogin;

/**
 * @描述 : 授权数据（域名【ip+端口】对应的数据）
 * @作者 : 胡璐璐
 * @时间 : 2021年4月23日 08:52:29
 */
public class AuthData {

	private Auth auth;		//授权
	private AuthCache authCache;		//缓存
	private AuthCachePrefix authCachePrefix;			//缓存_key前缀
	private AuthCacheRedis authCacheRedis;		//缓存_redis
	private AuthCacheRedisPool authCacheRedisPool;		//缓存_redis_连接池
	private List<AuthCacheRedisSentinel> authCacheRedisSentinelList;	//缓存_redis_哨兵
	private AuthCookie authCookie;		//cookie
	private AuthLogin authLogin;		//登录接口
	private Set<String> authWhiteSet;		//白名单
	
	public AuthData(Auth auth,AuthCache authCache,AuthCachePrefix authCachePrefix,AuthCacheRedis authCacheRedis,AuthCacheRedisPool authCacheRedisPool,List<AuthCacheRedisSentinel> authCacheRedisSentinelList,AuthCookie authCookie,AuthLogin authLogin,Set<String> authWhiteSet) {
		this.auth=auth;
		this.authCache=authCache;
		this.authCacheRedis=authCacheRedis;
		this.authCacheRedisPool=authCacheRedisPool;
		this.authCachePrefix=authCachePrefix;
		this.authCacheRedisSentinelList=authCacheRedisSentinelList;
		this.authCookie=authCookie;
		this.authLogin=authLogin;
		this.authWhiteSet=authWhiteSet;
	}
	
	public Auth getAuth() {
		return auth;
	}
	public void setAuth(Auth auth) {
		this.auth = auth;
	}
	public AuthCache getAuthCache() {
		return authCache;
	}
	public void setAuthCache(AuthCache authCache) {
		this.authCache = authCache;
	}
	public AuthCacheRedis getAuthCacheRedis() {
		return authCacheRedis;
	}
	public void setAuthCacheRedis(AuthCacheRedis authCacheRedis) {
		this.authCacheRedis = authCacheRedis;
	}
	public AuthCookie getAuthCookie() {
		return authCookie;
	}
	public void setAuthCookie(AuthCookie authCookie) {
		this.authCookie = authCookie;
	}
	public AuthLogin getAuthLogin() {
		return authLogin;
	}
	public void setAuthLogin(AuthLogin authLogin) {
		this.authLogin = authLogin;
	}
	public Set<String> getAuthWhiteSet() {
		return authWhiteSet;
	}
	public void setAuthWhiteSet(Set<String> authWhiteSet) {
		this.authWhiteSet = authWhiteSet;
	}
	public AuthCacheRedisPool getAuthCacheRedisPool() {
		return authCacheRedisPool;
	}
	public void setAuthCacheRedisPool(AuthCacheRedisPool authCacheRedisPool) {
		this.authCacheRedisPool = authCacheRedisPool;
	}
	public AuthCachePrefix getAuthCachePrefix() {
		return authCachePrefix;
	}
	public void setAuthCachePrefix(AuthCachePrefix authCachePrefix) {
		this.authCachePrefix = authCachePrefix;
	}
	public List<AuthCacheRedisSentinel> getAuthCacheRedisSentinelList() {
		return authCacheRedisSentinelList;
	}
	public void setAuthCacheRedisSentinelList(List<AuthCacheRedisSentinel> authCacheRedisSentinelList) {
		this.authCacheRedisSentinelList = authCacheRedisSentinelList;
	}
	
}