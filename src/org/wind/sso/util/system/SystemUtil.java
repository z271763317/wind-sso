package org.wind.sso.util.system;

import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.wind.sso.bean.AuthData;
import org.wind.sso.config.URL;
import org.wind.sso.model.Auth;
import org.wind.sso.model.AuthCachePrefix;
import org.wind.sso.model.AuthCacheRedis;
import org.wind.sso.model.AuthCacheRedisPool;
import org.wind.sso.model.AuthCacheRedisSentinel;
import org.wind.sso.model.AuthLogin;
import org.wind.sso.util.HttpClientSupport;
import org.wind.sso.util.JsonUtil;
import org.wind.sso.util.RegexUtil;
import org.wind.sso.util.Response;
import org.wind.sso.util.ToolUtil;
import org.wind.sso.util.jedis.JedisAbstract;
import org.wind.sso.util.jedis.JedisSentinelUtil;
import org.wind.sso.util.jedis.JedisUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;


/**
 * @描述 : 系统重要工具类
 * @版权 : 胡璐璐
 * @时间 : 2019年11月22日 16:14:46
 */
public final class SystemUtil {

//	private static final Logger logger=Logger.getLogger(SystemUtil.class);

	/**关闭流**/
	public static void close(Closeable... objArr){
		if(objArr!=null && objArr.length>0){
			for(Closeable t_obj:objArr){
				if(t_obj!=null){try{t_obj.close();}catch(Exception e){e.printStackTrace();}}
			}
		}
	}
	/**是否允许访问**/
	public static boolean isAllowAccess(HttpServletRequest request) {
		String requestIp=ToolUtil.getRequestIP(request);
		AuthData obj=Auth.get();
		if(obj!=null) {
			Auth objAuth=obj.getAuth();
			//是否启用ip访问限制
			if(objAuth.getIsIpLimit()) {
				Set<String> whiteSet=obj.getAuthWhiteSet();
				//是否在白名单列
				if(whiteSet==null || whiteSet.size()<=0 || !whiteSet.contains(requestIp)) {
					throw new IllegalArgumentException("您无权访问该服务");
				}
			}
			return true;
		}else{
			throw new RuntimeException("当前服务器没有被授权");
		}
	}
	/**获取 ; Jedis对象（HttpServletRequest式）**/
	public static Jedis getJedis() {
		AuthData obj=Auth.get();
		Jedis jedis=getJedis(obj);
		if(jedis==null) {
			throw new IllegalArgumentException("该服务器未被授权");
		}
		return jedis;
	}
	/**获取 ; Jedis对象（domain式）**/
	public static Jedis getJedis(AuthData obj) {
		if(obj!=null) {
			AuthCacheRedis objAcr=obj.getAuthCacheRedis();
			AuthCacheRedisPool objAcrp=obj.getAuthCacheRedisPool();
			List<AuthCacheRedisSentinel> objAcrsList=obj.getAuthCacheRedisSentinelList();
			//
			String ip=objAcr.getIp();
			Integer port=objAcr.getPort();
			String passWord=objAcr.getPassWord();
			Integer index=objAcr.getIndex();
			int userWay=objAcr.getUseWay();
			String sentinelMasterName=objAcr.getSentinelMasterName();		//哨兵主服务名
			//
			Integer minIdle=objAcrp.getMinIdle();
			Integer maxIdle=objAcrp.getMaxIdle();
			Integer maxTotal=objAcrp.getMaxTotal();
			Long maxWaitMillis=objAcrp.getMaxWaitMillis();
			Boolean testOnBorrow=objAcrp.getTestOnBorrow();
			Boolean testOnReturn=objAcrp.getTestOnReturn();
			Boolean jmxEnabled=objAcrp.getJmxEnabled();
			Boolean testWhileIdle=objAcrp.getTestWhileIdle();
			Long timeBetweenEvictionRunsMillis=objAcrp.getTimeBetweenEvictionRunsMillis();
			Long minEvictableIdleTimeMillis=objAcrp.getMinEvictableIdleTimeMillis();
			Integer numTestsPerEvictionRun=objAcrp.getNumTestsPerEvictionRun();
			//
			Jedis objJedis = null;
			JedisPoolConfig config=JedisAbstract.getJedisPoolConfig(ip, port, minIdle, maxIdle, maxTotal, maxWaitMillis, testOnBorrow, testOnReturn, jmxEnabled, testWhileIdle, timeBetweenEvictionRunsMillis, minEvictableIdleTimeMillis, numTestsPerEvictionRun);
			/**使用方式**/
			switch(userWay) {
				//单节点
				case AuthCacheRedis.useWay_single:{
					objJedis=JedisUtil.getJedis(config, ip, port, passWord, index);
					break;
				}
				//哨兵
				case AuthCacheRedis.useWay_sentinel:{
					if(objAcrsList!=null && objAcrsList.size()>0) {
						Set<String> sentinelsSet=new LinkedHashSet<String>();
						for(AuthCacheRedisSentinel t_objAcrs:objAcrsList) {
							String t_ip=t_objAcrs.getIp();
							int t_port=t_objAcrs.getPort();
							sentinelsSet.add(t_ip+":"+t_port);
						}
						objJedis=JedisSentinelUtil.getJedis(config, sentinelsSet, passWord, index, sentinelMasterName);
					}
					break;
				}
			}
			if(objJedis==null) {
				throw new RuntimeException("没有找到方式为【"+AuthCacheRedis.getUseWayMap().get(userWay)+"】的配置");
			}
			return objJedis;
		}else {
			throw new RuntimeException("没有找到缓存使用方式");
		}
	}
	/**获取 : AuthData**/
	public static AuthData getAuthData() {
		return Auth.get();
	}
	/**获取 : 是否开启验证码功能（HttpServletRequest式）**/
	public static boolean getIsYzm(Jedis jedis,String userName) {
		if(userName!=null) {
			AuthData obj=Auth.get();
			AuthLogin objAl=obj.getAuthLogin();
			int pwdErrorNumYzm=objAl.getPwdErrorNumYzm();
			//开启验证码
			if(pwdErrorNumYzm>=0) {
		    	int pwdErrorNum=getPwdErrorNum(jedis, userName);		//已经错误的次数
		    	return getIsYzm(pwdErrorNumYzm, pwdErrorNum);
			}
		}
		return false;
	}
	/**获取 : 是否开启验证码功能（pwdErrorNumYzm=域名对应的登录错误次数【达到该值则代表开启验证码】；pwdErrorNum=已经错误的次数）**/
	public static boolean getIsYzm(int pwdErrorNumYzm,int pwdErrorNum) {
		//开启验证码
		if(pwdErrorNumYzm>=0) {
	    	//开启
	    	if(pwdErrorNumYzm<=pwdErrorNum) {
	    		return true;
	    	}
		}
		return false;
	}
	/**获取 : 指定用户名的错误数**/
	public static int getPwdErrorNum(Jedis jedis,String userName) {
		userName=RegexUtil.clearEmpty(userName);
		int pwdErrorNum=0;		//已经错误的次数
		if(userName!=null) {
			AuthData obj=Auth.get();
			AuthCachePrefix objPrefix=obj.getAuthCachePrefix();
			String pwdErrorNumStr=jedis.get(objPrefix.getPwdErrorNum()+userName);
	    	if(pwdErrorNumStr!=null) {
	    		pwdErrorNum=Integer.parseInt(pwdErrorNumStr);
	    	}
		}
		return pwdErrorNum;
	}
	/**增加1次 : 指定用户名的错误数（达到最高值将不增加），并返回是否开启验证码**/
	public static boolean increasePwdErrorNum(Jedis jedis,String userName) throws Exception {
		userName=RegexUtil.clearEmpty(userName);
		if(userName!=null) {
			AuthData obj=Auth.get();
			AuthLogin objAl=obj.getAuthLogin();
			//开启验证码处理
			if(objAl.getPwdErrorNumYzm()>=0) {
				boolean isUserExist=isUserExist(userName);
				if(isUserExist) {
					int pwdErrorNum=getPwdErrorNum(jedis, userName);		//已经错误的次数
					boolean isYzm=getIsYzm(objAl.getPwdErrorNumYzm(), pwdErrorNum);
					//
					AuthCachePrefix objPrefix=obj.getAuthCachePrefix();
					AuthCacheRedis objRedis=obj.getAuthCacheRedis();
					//不开启
					if(!isYzm) {
						pwdErrorNum++;		//累加1次
						isYzm=getIsYzm(objAl.getPwdErrorNumYzm(), pwdErrorNum);
						jedis.set(objPrefix.getPwdErrorNum()+userName,pwdErrorNum+"");
					}
					jedis.expire(objPrefix.getPwdErrorNum()+userName, objRedis.getCaptchaExpiry());		//存活多久（根据验证码走）
					return isYzm;
				}
			}
		}
		return false;
	}
	/**是否用户存在 **/
	public static boolean isUserExist(String userName) throws Exception {
		AuthData obj=Auth.get();
		AuthLogin objAl=obj.getAuthLogin();
		//
		HttpClientSupport hcs=HttpClientSupport.getInstance().generatorHttpClient();
		Map<String,String> existParamMap=new HashMap<String,String>();
		existParamMap.put("userName", userName);
		//调用登录接口，验证用户名是否存在
		Response httpResponse=hcs.post(objAl.getUrl()+URL.exist, existParamMap, null);
		/*
		 * 返回的数据格式：
		 * {
		 * 		"code":"【状态码。1=存在，0=不存在】",
		 * }
		 */
		Map<String,Object> existResultMap=JsonUtil.toObject(httpResponse.asString(), Map.class);
		if(existResultMap!=null && existResultMap.size()>0) {
			Integer t_code=(Integer) existResultMap.get("code");
			//存在
			if(t_code!=null && t_code.equals(1)) {
				return true;
			}
		}
		return false;
	}
}