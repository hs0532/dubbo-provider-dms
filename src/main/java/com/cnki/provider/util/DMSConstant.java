package com.cnki.provider.util;

public class DMSConstant {
	
	public static final String AES_KEY = "wdoj223ind2j3ndhr4ij99rmfdualspx9";
	
	/**
	 * 复制库的命名初始格式
	 */
	public static final String DB_NAME_MASTER_START = "master_";
	public static final String DB_NAME_MASTER_END = "_db";
	
	
	
	public static final String ENCRYPT_AES_TYPE = "AES";
	public static final String ENCRYPT_SHA1PRNG = "SHA1PRNG";
	public static final String SESSION_KEY = "userInfo" ;
	public static final String MD5 = "MD5";
	public static final int PASSWORD_BEGIN = 3;
	public static final int PASSWORD_LANG = 31;
	
	public static final String LOGIN_CHECK_IMG_CODE = "imageCode";
	public static final String RETURN_SUCCESS = "isSuccess"; 
	public static final String RETURN_MESSAGE = "message";
	
	/**
	 * 数据源配置常量
	 */
	public static final String DB_INFO_DRIVERCLASSNAME = "com.mysql.cj.jdbc.Driver";
	public static final String DB_INFO_URL_A = "jdbc:mysql://";
	public static final String DB_INFO_URL_B = ":";
	public static final String DB_INFO_URL_C = "/";
	public static final String DB_INFO_URL_D = "?characterEncoding=utf-8&serverTimezone=GMT%2B8&&useSSL=false";
}
