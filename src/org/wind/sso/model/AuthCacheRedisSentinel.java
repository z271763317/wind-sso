package org.wind.sso.model;

import org.wind.orm.annotation.Column;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.annotation.Tables;
import org.wind.sso.model.parent.Model_Long;

/**
 * @描述 : 授权_缓存_redis_哨兵表
 * @作者 : 胡璐璐
 * @时间 : 2021年04月25日 14:40:13
 */
@Tables("auth_cache_redis_sentinel")
public class AuthCacheRedisSentinel extends Model_Long{

	@Column("auth_id")@ForeignKey
	private Auth authId;		//授权ID
	@Column("auth_cache_redis_id")@ForeignKey
	private AuthCacheRedis authCacheRedisId;		//授权_缓存_redis的ID
	private String ip;		//哨兵ip
	private Integer port;		//端口
	
	public Auth getAuthId() {
		return authId;
	}
	public void setAuthId(Auth authId) {
		this.authId = authId;
	}
	public AuthCacheRedis getAuthCacheRedisId() {
		return authCacheRedisId;
	}
	public void setAuthCacheRedisId(AuthCacheRedis authCacheRedisId) {
		this.authCacheRedisId = authCacheRedisId;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}

}