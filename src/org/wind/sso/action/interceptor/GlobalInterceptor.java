package org.wind.sso.action.interceptor;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.wind.mvc.annotation.interceptor.An_Interceptor;
import org.wind.mvc.bean.context.ActionContext;
import org.wind.mvc.interceptor.Interceptor;
import org.wind.mvc.result.Result;
import org.wind.sso.config.cache.Cache;
import org.wind.sso.util.FileOS;
import org.wind.sso.util.JsonUtil;

/**
 * @描述 : 通用拦截器
 * @作者 : 胡璐璐
 * @时间 : 2020年8月29日 14:44:39
 */
@An_Interceptor("/*")
public class GlobalInterceptor implements Interceptor{

	private static final Logger logger=Logger.getLogger(GlobalInterceptor.class);
	
	/**执行前**/
	public boolean before(ActionContext context) throws Exception {
		return true;
	}

	/**执行后**/
	@SuppressWarnings("unchecked")
	public void after(ActionContext context,Result result) {
		if(!context.getResponse().isCommitted()) {
			String t_result=result.getData();
			//
			Object methodResult=result.getMethodResult();
			Map<Object,Object> resultMap=null;
			if(methodResult!=null) {
				if(methodResult instanceof Map) {
					resultMap=(Map<Object, Object>) methodResult;
				}else if(methodResult instanceof String){
					resultMap=JsonUtil.toObject(methodResult.toString(), Map.class);
				}
			}else{
				resultMap=new HashMap<Object,Object>();
			}
			if(resultMap!=null) {
				if(!resultMap.containsKey("code")) {
					resultMap.put("code", Cache.response_code_success);
				}
				t_result=JsonUtil.toJson(resultMap);
			}
			if(t_result!=null) {
				FileOS.writer(context.getRequest(), context.getResponse(), t_result);		//响应
			}
		}
	}

	/**完成后（所有处理完，渲染页面后执行。若在此之前出现了异常，则exception会有值，可做异常处理）**/
	public void complete(ActionContext context, Throwable e) {
		HttpServletRequest request=context.getRequest();
		HttpServletResponse response=context.getResponse();
		try{
			//有异常，说明处理失败
			if(e!=null){
				if(e.getClass()==IllegalArgumentException.class){
					returnError(request, response,e.getMessage());
				}else{
					logger.error(e.getMessage(),e);
					returnError(request, response,"SSO异常，请稍后在试试");
				}
			}
		}catch(Exception e1){
			logger.error(e.getMessage(),e);
			returnError(request, response,"SSO异常，请稍后在试试2");
		}finally {
			
		}
	}
	/**
	 * 返回错误
	 * @param request
	 * @param response
	 * @param errorMsg : 错误信息
	 */
	private static void returnError(HttpServletRequest request,HttpServletResponse response,String errorMsg){
		if(!response.isCommitted()) {
			Map<String,Object> resultMap=new HashMap<String,Object>();
			resultMap.put("code", Cache.response_code_failure);
			resultMap.put("message", errorMsg);
			String t_result=JsonUtil.toJson(resultMap);
			//
			FileOS.writer(request, response, t_result);		//返回错误信息
		}
	}
}
