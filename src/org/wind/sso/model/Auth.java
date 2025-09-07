package org.wind.sso.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wind.orm.Table;
import org.wind.orm.annotation.Column;
import org.wind.orm.annotation.Tables;
import org.wind.sso.annotation.An_Time;
import org.wind.sso.bean.AuthData;
import org.wind.sso.model.parent.Model_Long;
import org.wind.sso.util.SysConstant;

/**
 * @描述 : 授权表
 * @作者 : 胡璐璐
 * @时间 : 2021年4月16日 00:23:05
 */
@An_Time(delay =2*60*1000 ,period=2*60*1000)
@Tables("auth")
public class Auth extends Model_Long{

	private static AuthData obj;
	private static Long authId;			//本工程绑定的授权ID
	
	private String domain;		//域名
	private String name;		//名称
	@Column("is_ip_limit")
	private Boolean isIpLimit;		//是否启用ip访问限制（部分功能失效，如：SSO登录页）
	@Column("login_url")
	private String loginUrl;		//登录界面URL
	
	/**初始化**/
	public static void init() throws Exception{
		Auth auth=Table.findOne(Auth.class, "domain=?", new Object[] {SysConstant.domain}, false);
		if(auth!=null) {
			if(auth.getStatus().equals(1)) {
				authId=auth.getId();
				//
				String tjSQL="auth_id=?";
				List<Long> authIdList=new ArrayList<Long>();
				authIdList.add(authId);
				//
				AuthCache authCache=Table.findOne(AuthCache.class, tjSQL, authIdList, false);
				AuthCachePrefix authCachePrefix=Table.findOne(AuthCachePrefix.class, tjSQL, authIdList, false);
				AuthCacheRedis authCacheRedis=Table.findOne(AuthCacheRedis.class, tjSQL, authIdList, false);
				AuthCacheRedisPool authCacheRedisPool=Table.findOne(AuthCacheRedisPool.class, tjSQL, authIdList, false);
				List<AuthCacheRedisSentinel> authCacheRedisSentinelList=Table.find(AuthCacheRedisSentinel.class, tjSQL, authIdList, false,null);
				AuthCookie authCookie=Table.findOne(AuthCookie.class, tjSQL, authIdList, false);
				AuthLogin authLogin=Table.findOne(AuthLogin.class, tjSQL, authIdList, false);
				List<AuthWhite> listAuthWhite=Table.find(AuthWhite.class, tjSQL, authIdList, false, null);
				//
				Set<String> authWhiteSet=new HashSet<String>();		//白名单
				for(AuthWhite obj:listAuthWhite) {
					authWhiteSet.add(obj.getIp());
				}
				obj=new AuthData(auth, authCache, authCachePrefix,authCacheRedis, authCacheRedisPool,authCacheRedisSentinelList,authCookie, authLogin, authWhiteSet);
			}else{
				throw new RuntimeException("配置的【domain】对应的授权已禁用");
			}
		}else{
			throw new RuntimeException("配置的【domain】对应的授权不存在");
		}
		
	}
	/**获取 : 授权数据**/
	public static AuthData get() {
		return obj;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getIsIpLimit() {
		return isIpLimit;
	}
	public void setIsIpLimit(Boolean isIpLimit) {
		this.isIpLimit = isIpLimit;
	}
	public static Long getAuthId() {
		return authId;
	}
	public String getLoginUrl() {
		return loginUrl;
	}
	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
	
}
