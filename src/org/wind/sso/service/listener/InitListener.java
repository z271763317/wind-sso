package org.wind.sso.service.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;
import org.wind.sso.config.cache.Cache;
import org.wind.sso.model.Auth;
import org.wind.sso.service.time.TimeService_delete_sso;
import org.wind.sso.service.time.TimeService_delete_sso_userName;
import org.wind.sso.service.time.TimeService_insert;
import org.wind.sso.service.time.TimeService_save;
import org.wind.sso.util.SysConstant;

/**
 * @描述：web项目监听器（启动前、后的处理）
 * @版权：胡璐璐
 * @时间：2020年10月31日 13:49:41
 */
@WebListener
public class InitListener implements ServletContextListener {

	private static Logger logger=Logger.getLogger(InitListener.class);
	
	/**启动前：InitServlet首先执行它，启动前的处理*/
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			if(SysConstant.domain==null || SysConstant.domain.trim().length()<=0) {
				throw new RuntimeException("未在【wind-sso.properties】文件类配置【domain】");
			}
			Auth.init();
			Cache.getInstance().init();
			new Thread(new TimeService_insert()).start();
			new Thread(new TimeService_save()).start();
			new Thread(new TimeService_delete_sso()).start();
			new Thread(new TimeService_delete_sso_userName()).start();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			System.exit(0);		//启动失败，则关闭tomcat
		}
	}
	/**关闭前：关闭前的处理**/
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.info("关闭前的处理：");
		
		logger.info("关闭前的处理——完毕");
	}
}