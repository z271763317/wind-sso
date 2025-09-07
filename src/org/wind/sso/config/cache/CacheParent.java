package org.wind.sso.config.cache;

/**
 * @描述 : 缓存顶级父类——数据关键词（带部分字典）
 * @版权 : 胡璐璐
 * @时间 : 2017年9月29日 15:18:40
 */
public abstract class CacheParent{

	/****************************系统常量*******************************/
	/*其他*/
	public final static String jsonp_callBack_param="callback";		//ajax jsonp回调方法

    /*****************实体类状态（通用）*****************/
	/*************响应状态*************/
	//code=执行某方法的状态
	public final static int response_code_failure=0;		//失败
	public final static int response_code_success=1;		//成功
	
}