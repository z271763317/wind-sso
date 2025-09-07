package org.wind.sso.service.time;

import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.wind.sso.bean.AuthData;
import org.wind.sso.model.Auth;
import org.wind.sso.model.AuthCachePrefix;
import org.wind.sso.model.AuthSSO;
import org.wind.sso.util.RegexUtil;
import org.wind.sso.util.SysConstant;
import org.wind.sso.util.system.SystemUtil;

import redis.clients.jedis.Jedis;

/**
 * @描述 : 定时任务——异步删除
 * @作者 : 胡璐璐
 * @时间 : 2021年5月12日 22:53:53
 */
public class TimeService_delete_sso extends TimerTask{

	private static BlockingQueue<String> queue=new LinkedBlockingQueue<String>();
	private static final int threadNum=Integer.parseInt(SysConstant.getProperty("thread_delete_sso", "1"));
	private static final ExecutorService executorService=Executors.newFixedThreadPool(threadNum);
	
	public static void add(String ssoId) {
		ssoId=RegexUtil.clearEmpty(ssoId);
		if(ssoId==null) {
			throw new RuntimeException("未指定要删除的ssoId");
		}
		queue.add(ssoId);
	}
	
	/**执行**/
	public void run() {
		while(true) {
			try {
				final String ssoId=queue.take();
				//
				final AuthSSO obj=new AuthSSO();
				obj.setId(ssoId);
				executorService.execute(new Runnable() {
					public void run() {
						try {
							AuthData objData=Auth.get();
							Jedis jedis=SystemUtil.getJedis(Auth.get());
							try {
								AuthCachePrefix objPrefix=objData.getAuthCachePrefix();
								jedis.del(objPrefix.getSsoId()+ssoId);
								//
								obj.delete();
							}finally{
								jedis.close();
							}
						}catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
