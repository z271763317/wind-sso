package org.wind.sso.model.parent;

import org.wind.orm.annotation.Id;

/**
 * @描述 : 实体类的父类——数据（主键：字符串型）
 * @版权 : 胡璐璐
 * @时间 : 2021年6月23日 17:10:19
 */
public class Model_String extends Model{
	
	@Id
	protected String id;		//主键

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}