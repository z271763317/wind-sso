package org.wind.sso.service.time;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.wind.sso.model.AuthSSO;
import org.wind.sso.util.RegexUtil;
import org.wind.sso.util.SysConstant;

/**
 * @描述 : 定时任务——异步删除指定用户名下的所有sso
 * @作者 : 胡璐璐
 * @时间 : 2021年6月23日 17:58:25
 */
public class TimeService_delete_sso_userName extends TimerTask{

	private static BlockingQueue<String> queue=new LinkedBlockingQueue<String>();
	private static final int threadNum=Integer.parseInt(SysConstant.getProperty("thread_delete_sso_userName", "1"));
	private static final ExecutorService executorService=Executors.newFixedThreadPool(threadNum);
	
	public static void add(String userName) {
		userName=RegexUtil.clearEmpty(userName);
		if(userName==null) {
			throw new RuntimeException("未指定要删除的用户名");
		}
		queue.add(userName);
	}
	
	/**执行**/
	public void run() {
		while(true) {
			try {
				String userName=queue.take();
				//
				final AuthSSO obj=new AuthSSO();
				obj.setUserName(userName);
				executorService.execute(new Runnable() {
					public void run() {
						try {
							List<AuthSSO> list=obj.find();
							for(AuthSSO t_obj:list) {
								TimeService_delete_sso.add(t_obj.getId());
							}
							obj.delete();
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
