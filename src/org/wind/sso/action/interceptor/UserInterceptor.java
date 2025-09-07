package org.wind.sso.action.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.wind.mvc.annotation.interceptor.An_Interceptor;
import org.wind.mvc.bean.context.ActionContext;
import org.wind.mvc.interceptor.Interceptor;
import org.wind.mvc.result.Result;
import org.wind.sso.util.system.SystemUtil;

/**
 * @描述 : 用户拦截器——如：验证当前请求者是否有权限操作
 * @作者 : 胡璐璐
 * @时间 : 2020年8月29日 14:44:39
 */
@An_Interceptor(value="/user/*",order=2)
public class UserInterceptor implements Interceptor{
	
	/**执行前**/
	public boolean before(ActionContext context) throws Exception {
		HttpServletRequest request=context.getRequest();
		return SystemUtil.isAllowAccess(request);
	}

	/**执行后**/
	public void after(ActionContext context,Result result) {
		
	}

	/**完成后（所有处理完，渲染页面后执行。若在此之前出现了异常，则exception会有值，可做异常处理）**/
	public void complete(ActionContext context, Throwable e) {
		
	}
	
	
}
