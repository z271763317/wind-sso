package org.wind.sso.model;

import org.wind.orm.annotation.Column;
import org.wind.orm.annotation.Tables;
import org.wind.orm.util.TableUtil;
import org.wind.sso.model.parent.Model_String;
import org.wind.sso.service.time.TimeService_insert;

/**
 * @描述 : 授权_SSO表（已登录的）
 * @作者 : 胡璐璐
 * @时间 : 2021年05月12日 14:47:46
 */
@Tables("auth_sso")
public class AuthSSO extends Model_String{

	private static final String tablePrefix=TableUtil.getTable(AuthSSO.class)+"_";		//原表+前缀
	
	private Integer expiry;		//过期时间（单位：秒）
	private String ip;		//登录者的IP
	@Column("user_name")
	private String userName;		//登录的用户名
	@Column("pass_word")
	private String passWord;		//登录的密码
	
	public AuthSSO() {
		super.setTable(tablePrefix+Auth.getAuthId());
	}
	
	/**获取 : 根据SsoId**/
	public static AuthSSO getBySsoId(String ssoId) {
		if(ssoId!=null) {
			AuthSSO obj=new AuthSSO();
			obj.setId(ssoId);
			AuthSSO objSSO=obj.findOne(false);
			return objSSO;
		}else {
			return null;
		}
	}
	/**插入**/
	public static void add(String ssoId,Integer expiry,String ip,String userName,String passWord) {
		AuthSSO obj=new AuthSSO();
		obj.setId(ssoId);
		obj.setExpiry(expiry);
		obj.setIp(ip);
		obj.setUserName(userName);
		obj.setPassWord(passWord);
		TimeService_insert.add(obj);
	}
	public Integer getExpiry() {
		return expiry;
	}
	public void setExpiry(Integer expiry) {
		this.expiry = expiry;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassWord() {
		return passWord;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
}