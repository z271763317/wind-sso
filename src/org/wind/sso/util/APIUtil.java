package org.wind.sso.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @描述 : API工具类（可生成签名）
 * @版权 : 胡璐璐
 * @时间 : 2016年4月9日 13:29:44
 */
public final class APIUtil{
	
	protected static Logger logger = Logger.getLogger(APIUtil.class);
	
	private APIUtil(){
		//不允许实例化
	}
	/**
	 * 生成签名（末尾放secret）
	 * @param secret，分配给您的APP_SECRET
	 * @param isOut : 是否输出参数日志
	 */
	public static String signature(Map<String, String> params, String secret,boolean isOut) {
		String paramsStr = getSortedParamsStr(params);
		if (paramsStr == null){
			return null;
		}
		paramsStr += secret;		
		if(isOut){
			logger.info("签名参数："+paramsStr);
		}
		return EncryptUtil.getMD5(paramsStr);
	}
	//所有参数按“key=value”形式拼接，顺序——参数按“升序”排序的顺序
	private static String getSortedParamsStr(Map<String, String> params){
		if (params == null){
			return null;
		}
		StringBuffer result = new StringBuffer();
		Map<String, String> treeMap = new TreeMap<String, String>(params);
		for(Entry<String,String> entry:treeMap.entrySet()){
			String value=entry.getValue();
			if(value!=null && value.trim().length()>0){
				if(result.length()>0){
					result.append("&");
				}
				String key=entry.getKey();
				result.append(key).append("=").append(value.trim());
			}
		}
		return result.toString();
	}
	/**获取 : 请求的所有参数**/
	public static Map<String,String> getRequestParam(HttpServletRequest request){
		Map<String,String> requestParamMap = new HashMap<String, String>();
		Enumeration<String> paramterNames = request.getParameterNames();
		while(paramterNames.hasMoreElements()){
			String paramName = paramterNames.nextElement();
			String t_value=request.getParameter(paramName);
			if(t_value!=null){
				requestParamMap.put(paramName, t_value);	//默认rest服务不处理单个参数名有多个参数值的情况
			}
		}
		return requestParamMap;
	}

}