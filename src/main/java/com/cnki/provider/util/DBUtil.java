package com.cnki.provider.util;

import java.util.Map;

import com.alibaba.druid.pool.DruidDataSource;
import com.cnki.api.User;
import com.cnki.provider.db.DataSourceContextHolder;
import com.cnki.provider.db.DynamicDataSource;

public class DBUtil {
	
	public static void addOrChangeDataSource(User user){

		/**
		 * 创建动态数据源
		 */
		Map<Object, Object> dataSourceMap = DynamicDataSource.getInstance().getDataSourceMap();
		if(!dataSourceMap.containsKey(user.getDbIP())&&!"".equals(user.getDbIP())&&null != user.getDbIP()){
			DruidDataSource dynamicDataSource = new DruidDataSource();
			dynamicDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
			dynamicDataSource.setUrl("jdbc:mysql://"+user.getDbIP()+""
					+ ":3306/"+user.getDbName()+"?characterEncoding=utf-8&serverTimezone=GMT%2B8"
							+ "&&useSSL=false"
					);
			dynamicDataSource.setUsername(user.getDbUsername());
			dynamicDataSource.setPassword(user.getDbPassword());
			dataSourceMap.put(user.getDbIP(), dynamicDataSource);
			DynamicDataSource.getInstance().setTargetDataSources(dataSourceMap);
			/**
			 * 切换为动态数据源实例，打印学生信息
			 */
			DataSourceContextHolder.setDBType(user.getDbIP());
		}else{
			/**
			 * 切换为动态数据源实例，打印学生信息
			 */
			DataSourceContextHolder.setDBType(user.getDbIP());
		}
		
	
	}

}
