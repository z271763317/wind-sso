package org.wind.sso.service.time;

import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.wind.orm.Table;
import org.wind.sso.util.SysConstant;

/**
 * @描述 : 定时任务——异步保存
 * @作者 : 胡璐璐
 * @时间 : 2021年6月24日 15:39:29
 */
public class TimeService_save extends TimerTask{

	private static BlockingQueue<Table> queue=new LinkedBlockingQueue<Table>();
	private static final int threadNum=Integer.parseInt(SysConstant.getProperty("thread_save", "1"));
	private static final ExecutorService executorService=Executors.newFixedThreadPool(threadNum);
	
	public static void add(Table obj) {
		queue.add(obj);
	}
	
	/**执行**/
	public void run() {
		while(true) {
			try {
				final Table obj=queue.take();
				executorService.execute(new Runnable() {
					public void run() {
						try {
							obj.save();
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
