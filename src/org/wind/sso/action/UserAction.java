package org.wind.sso.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.wind.mvc.annotation.controller.An_Controller;
import org.wind.mvc.annotation.controller.An_URL;
import org.wind.sso.bean.AuthData;
import org.wind.sso.model.Auth;
import org.wind.sso.service.time.TimeService_delete_sso_userName;
import org.wind.sso.util.JsonUtil;
import org.wind.sso.util.system.SSOUtil;
import org.wind.sso.util.system.SystemUtil;

/**
 * @描述 : 用户Action
 * @作者 : 胡璐璐
 * @时间 : 2020年11月19日 19:26:32
 */
@An_Controller("/user")
public class UserAction {
	
	private static final Logger logger=Logger.getLogger(UserAction.class);
	
	/**获取 : SSO**/
	@An_URL("/getSSO")
	public Map<String,Object> getSSO(String ssoId,String ip) throws Exception{
		if(ssoId!=null && ssoId.length()>0) {
			Map<String,String> sso=SSOUtil.getSSO(ssoId,ip);
			if(sso!=null) {
				Map<String,Object> resultMap=new HashMap<String, Object>();
				resultMap.put("sso", sso);
				return resultMap;
			}else{
				throw new IllegalArgumentException("该会话不存在或已失效");
			}
		}else{
			throw new IllegalArgumentException("没有指定ssoId（会话标识）");
		}
	}
	/**
	 * 保存 : 会话信息
	 * @param ssoId : 指定的会话
	 * @param handlerSession : 被处理的会话。key=原seesion（或新）的key，value=Map
	 * 				（key=操作【type、value】；value=对应操作的值【type=1是添加或修改，2=删除；value=则是type=1的时候的值】）
	 * 
	 */
	@An_URL("/saveSession")
	public void saveSession(String ssoId,String handlerSession) {
		if(ssoId!=null && ssoId.length()>0) {
			if(handlerSession!=null) {
				Map<Object,Map<String,Object>> handlerKeyMap=JsonUtil.toObject(handlerSession, Map.class);
				if(handlerKeyMap!=null && handlerKeyMap.size()>0) {
					String session=SSOUtil.getSession(ssoId);
					if(session!=null) {
						Map<Object,Object> sessionMap=JsonUtil.toObject(session, Map.class);
						if(sessionMap==null) {
							logger.error(ssoId+"：数据不正常："+session);
						}else{
							for(Entry<Object,Map<String,Object>> entry:handlerKeyMap.entrySet()) {
								Object key=entry.getKey();
								Map<String,Object> value=entry.getValue();
								//
								if(value!=null && value.size()>0) {
									Integer t_type=(Integer) value.get("type");
									if(t_type!=null) {
										switch(t_type) {
											//添加或修改
											case 1:{
												Object t_value=value.get("value");
												sessionMap.put(key, t_value);
												break;
											}
											//删除
											case 2:{
												sessionMap.remove(key);
												break;
											}
										}
									}
								}
							}
							SSOUtil.saveSession(ssoId, JsonUtil.toJson(sessionMap));	//更新缓存里的session数据
						}
					}
				}else{
					throw new IllegalArgumentException("会话数据是空的2");
				}
			}else{
				throw new IllegalArgumentException("会话数据是空的");
			}
		}else{
			throw new IllegalArgumentException("没有指定ssoId");
		}
	}
	/**退出**/
	@An_URL("/exit")
	public void exit(HttpServletRequest request, String ssoId) {
		SSOUtil.exit(request,ssoId);
	}
	/**退出全部（指定用户名所有登录的sso）**/
	@An_URL("/exitAll")
	public void exitAll(String userName){
		TimeService_delete_sso_userName.add(userName);
	}
	/**获取 : 登录URL **/
	@An_URL("/getLoginUrl")
	public Map<Object,Object> getLoginUrl() {
		AuthData obj=SystemUtil.getAuthData();
		Auth objAuth=obj.getAuth();
		String loginUrl=objAuth.getLoginUrl();
		//
		Map<Object, Object> resultMap=new HashMap<Object, Object>();
		resultMap.put("href", loginUrl);
		return resultMap;
	}
}
