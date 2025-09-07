package org.wind.sso.action;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wind.mvc.annotation.controller.An_Controller;
import org.wind.mvc.annotation.controller.An_URL;
import org.wind.sso.bean.AuthData;
import org.wind.sso.config.URL;
import org.wind.sso.config.cache.Cache;
import org.wind.sso.model.AuthLogin;
import org.wind.sso.util.HttpClientSupport;
import org.wind.sso.util.JsonUtil;
import org.wind.sso.util.Response;
import org.wind.sso.util.system.SystemUtil;

/**
 * @描述 : 注册Action
 * @作者 : 胡璐璐
 * @时间 : 2021年5月4日 18:59:50
 */
@An_Controller("/reg")
public class RegAction {
	
	/**注册**/
	@An_URL
	public Map<String,Object> index(HttpServletRequest request, HttpServletResponse response) throws Exception{
		AuthData authData=SystemUtil.getAuthData();
		AuthLogin objLogin=authData.getAuthLogin();
		//
		//返回数据
		Map<String,Object> resultMap=new HashMap<String,Object>();
		resultMap.put("code", Cache.response_code_failure);
		//
		HttpClientSupport hcs=HttpClientSupport.getInstance().generatorHttpClient();
		Map<String,String> paramMap=new IdentityHashMap<String,String>();
		Map<String,String[]> requestParramMap=request.getParameterMap();
		if(requestParramMap!=null && requestParramMap.size()>0) {
			for(Entry<String,String[]> entry:requestParramMap.entrySet()) {
				String valueArr[]=entry.getValue();
				if(valueArr!=null && valueArr.length>0) {
					String key=entry.getKey();
					for(String value:valueArr) {
						paramMap.put(new String(key), value);
					}
				}
			}
		}
		
		//调用注册接口
		Response httpResponse=hcs.post(objLogin.getUrl()+URL.reg, paramMap, null);
		/*
		 * 返回的数据格式：
		 * {
		 * 		"code":"【状态码。1=成功，0=失败】",
		 * 		"message":"【信息，一般是失败后返回的错误信息】",
		 * }
		 */
		Map<String,Object> regResultMap=JsonUtil.toObject(httpResponse.asString(), Map.class);
		if(regResultMap!=null && regResultMap.size()>0) {
			Integer t_code=(Integer) regResultMap.get("code");
			if(t_code!=null && t_code.equals(1)) {
				resultMap.put("code", Cache.response_code_success);		//成功
				return resultMap;
			}else{
				throw new IllegalArgumentException((String) regResultMap.get("message"));
			}
		}else{
			throw new IllegalArgumentException("远程登录服务异常");
		}
	}
	
}