package org.wind.sso.config.cache;

import org.wind.sso.annotation.An_Time;
import org.wind.sso.model.Auth;
import org.wind.sso.service.time.TimeService_model;
import org.wind.sso.util.TimeUtil;


/**
 * @描述 : 缓存父类——初始化方法
 * @版权 : 胡璐璐
 * @时间 : 2020年9月1日 12:31:20
 */
@SuppressWarnings({"unchecked"})
public abstract class CacheInit extends CacheParent{
	
	//初始化（通用）
	public void initConfig() throws Exception{
		init_time(Auth.class);
	}
	private void init_time(Class<?> t_class) {
		try{
			An_Time t_an_time=t_class.getAnnotation(An_Time.class);
			if(t_an_time!=null){
				long delay=t_an_time.delay();
				long period=t_an_time.period();
				//
				TimeService_model t_time_obj=new TimeService_model((Class<? extends An_Time>) t_class);
				TimeUtil.addTimeTask(t_time_obj, delay, period);
			}
		} catch (Exception e) {
			e.printStackTrace();	//跳过该异常
		}
	}
}