package com.cnki.provider.service.impl;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.cnki.api.DBRepositoryInterface;
import com.cnki.api.Field;
import com.cnki.api.Record;
import com.cnki.api.User;
import com.cnki.provider.util.DBUtil;
import com.cnki.provider.util.SqlParse;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * 
 * 提供消费者调用方法，版本好1.0.0
 *
 */
@Service(version = "${demo.service.version}",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
public class DBRepository implements DBRepositoryInterface {

	/**
	 * 注入 数据库查询类，用于查询数据库
	 */
    @Autowired  
    private JdbcTemplate jdbcTemplate;  


	/**
	 * 根据传入的分页参数，计算总页数
	 * 
	 * @param intPageSize
	 *            页面大小，即页面显示条数
	 * @param intRowCount
	 *            总记录数
	 * @return 总页数 int
	 */
	public int getTotalPageNum(int intPageSize, int intRowCount) {
		if (intPageSize < 0) {
			intPageSize = 1;
		}

		// 总页数
		int intTotalPageNum = (intRowCount + intPageSize - 1) / intPageSize;

		return intTotalPageNum;
	}

	/**
	 * 通过Record对象方式插入数据（包含：dbName、tableName、List<Field>）。
	 * 
	 * @param record
	 *            数据记录对象
	 * @return 是否插入成功 boolean
	 */
	@Transactional
	public boolean insert(Record record) throws Exception{
		boolean blnFlag = false;
		// 如果数据库连接成功（不为空）
		if (jdbcTemplate != null && record != null) {
			String strDBName = record.getDbName();
			String strTableName = record.getTableName();
			List<Field> listField = record.getFieldList();

			if (strDBName != null && !strDBName.isEmpty() 
					&& strTableName != null && !strTableName.isEmpty()
					&& listField != null && !listField.isEmpty()) {
					// 解析List<Field>对象，拼装需要的SQL语句
					StringBuffer sbSQLField = new StringBuffer();
					StringBuffer sbSQLValue = new StringBuffer();
					Object[] objParams = new Object[listField.size()];
					int intParam = 0;
					for (Field field : listField) {
						// 拼装字段列
						sbSQLField.append(field.getFieldName());
						sbSQLField.append(",");
						// 拼装值列
						sbSQLValue.append("?,");
						// 拼装值参数，数组
						objParams[intParam] = field.getFieldValue();
						intParam++;
					}

					String strSQLField = sbSQLField.toString();
					if (strSQLField.endsWith(",")) {
						strSQLField = strSQLField.substring(0, strSQLField.length() - 1);
					}
					String strSQLValue = sbSQLValue.toString();
					if (strSQLValue.endsWith(",")) {
						strSQLValue = strSQLValue.substring(0, strSQLValue.length() - 1);
					}

					StringBuffer sbSQL = new StringBuffer();
					sbSQL.append("insert into ");
					sbSQL.append(strDBName+"."+strTableName);
					sbSQL.append(" (");
					sbSQL.append(strSQLField);
					sbSQL.append(") values (");
					sbSQL.append(strSQLValue);
					sbSQL.append(") ");

					// 执行查询
					jdbcTemplate.update(sbSQL.toString(), objParams);
					blnFlag = true;
					if(true){
						throw new RuntimeException("发生异常了..");
					}
			}
		}

		return blnFlag;
	}

	/**
	 * 通过Record对象方式，根据数据ID，修改数据。
	 * 
	 * @param record
	 *            数据记录对象
	 * @return 是否插入成功 boolean
	 */
	public boolean update(Record record) {
		boolean blnFlag = false;
		// 如果数据库连接成功（不为空）
		if (jdbcTemplate != null && record != null) {
			String strDBName = record.getDbName();
			String strTableName = record.getTableName();
			String strID = record.getID();
			List<Field> listField = record.getFieldList();
			
			if (strDBName != null && !strDBName.isEmpty() 
					&& strTableName != null && !strTableName.isEmpty()
					&& strID != null && !strID.isEmpty()
					&& listField != null && !listField.isEmpty()) {
				try {
					if (listField == null || listField.isEmpty()) {
						return false;
					}

					// 解析List<Field>对象，拼装需要的SQL语句
					StringBuffer sbSQLFieldValue = new StringBuffer();
					Object[] objParams = new Object[listField.size() + 1];
					int intParam = 0;
					for (Field field : listField) {
						// 拼装字段及值
						sbSQLFieldValue.append(field.getFieldName());
						sbSQLFieldValue.append("=?,");
						// 拼装值参数，数组
						objParams[intParam] = field.getFieldValue();
						intParam++;
					}

					String strSQLFieldValue = sbSQLFieldValue.toString();
					if (strSQLFieldValue.endsWith(",")) {
						strSQLFieldValue = strSQLFieldValue.substring(0, strSQLFieldValue.length() - 1);
					}

					StringBuffer sbSQL = new StringBuffer();
					sbSQL.append("update ");
					sbSQL.append(strDBName+"."+strTableName);
					sbSQL.append(" set ");
					sbSQL.append(strSQLFieldValue);
					sbSQL.append(" where id=?");
					// 将ID值加入到参数表的最后
					objParams[intParam] = strID;

					// 执行查询
					jdbcTemplate.update(sbSQL.toString(), objParams);
					blnFlag = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return blnFlag;
	}

	/**
	 * 根据数据ID，删除数据。需要传入dbName、tableName、id
	 * 
	 * @param record
	 *            数据记录对象
	 * @return 是否删除成功 boolean
	 */
	public boolean delete(Record record) {
		boolean blnFlag = false;
		// 如果数据库连接成功（不为空）
		if (jdbcTemplate != null && record != null) {
			String strDBName = record.getDbName();
			String strTableName = record.getTableName();
			String strID = record.getID();

			if (strDBName != null && !strDBName.isEmpty() 
					&& strTableName != null && !strTableName.isEmpty()
					&& strID != null && !strID.isEmpty()) {
				try {
					// 解析List<Field>对象，拼装需要的SQL语句
					Object[] objParams = new Object[1];

					StringBuffer sbSQL = new StringBuffer();
					sbSQL.append("delete from ");
					sbSQL.append(strDBName+"."+strTableName);
					sbSQL.append(" where id=?");
					// 将ID值加入到参数表
					objParams[0] = strID;

					// 执行查询
					jdbcTemplate.update(sbSQL.toString(), objParams);
					blnFlag = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}

		return blnFlag;
	}

	/**
	 * 根据数据ID，读取数据。
	 * 
	 * @param record
	 *            数据记录对象
	 * @return 以List<Record>方式拼装的记录 List<Record>
	 */
	public List<Field[]> getRecordByID(Record record) {
		List<Field[]> listData = null;
		// 如果数据库连接成功（不为空）
		if (jdbcTemplate != null) {
			String strDBName = record.getDbName();
			String strTableName = record.getTableName();
			String strID = record.getID();
			List<Field> listField = record.getFieldList();
	
			if (strDBName != null && !strDBName.isEmpty() 
					&& strTableName != null && !strTableName.isEmpty()
					&& strID != null && !strID.isEmpty()
					&& listField != null && !listField.isEmpty()) {
				try {
					// 解析List<Field>对象，拼装需要的SQL语句
					StringBuffer sbSQLField = new StringBuffer();
					for (Field field : listField) {
						// 拼装字段及值
						sbSQLField.append(field.getFieldName());
						sbSQLField.append(",");
					}
	
					String strSQLField = sbSQLField.toString();
					if (strSQLField.endsWith(",")) {
						strSQLField = strSQLField.substring(0, strSQLField.length() - 1);
					}
	
					// 解析List<Field>对象，拼装需要的SQL语句
					Object[] objParams = new Object[1];
	
					StringBuffer sbSQL = new StringBuffer();
					sbSQL.append("select ");
					sbSQL.append(strSQLField);
					sbSQL.append(" from ");
					sbSQL.append(strDBName+"."+strTableName);
					sbSQL.append(" where id=?");
					// 将ID值加入到参数表
					objParams[0] = strID;
	
					// 执行查询
					listData = jdbcTemplate.query(sbSQL.toString(), objParams, new RowMapper<Field[]>() {
						@Override
						// 返回结果对象处理的回调函数
						public Field[] mapRow(ResultSet rs, int intRowNum) throws SQLException {
							// 获取当前查询表的字段列表
							ResultSetMetaData rsmd = rs.getMetaData();
							int intFieldCount = rsmd.getColumnCount();
							Field[] fields=new Field[intFieldCount];
							// 循环处理每个字段，将字段值put到Map中
							for (int i = 0; i < intFieldCount; i++) {
								Field field = new Field();
								field.setFieldName(rsmd.getColumnName(i + 1));
								field.setFieldValue(rs.getString(rsmd.getColumnName(i + 1)));
								fields[i]=field;
							}
	
							return fields;
						}
					});
	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	
		return listData;
	}

	/**
	 * 通用方法：通过 SQL语句插入/修改/删除数据。SQL中的参数必须使用？作为占位符，所有参数必须通过参数列表传入
	 * 
	 * @param strDBName
	 *            数据库名
	 * @param strSQL
	 *            输入的SQL语句
	 * @param objParams
	 *            拼装参数对象数组，可以替换SQL中的“？”对应的值
	 * @return 是否插入成功 boolean
	 */
	private boolean modifyBySQL( String strSQL, Object[] objParams,String strScheme) {
		boolean blnFlag = false;
		// 如果数据库连接成功（不为空）
		if (jdbcTemplate != null) {
			try {
				strSQL=SqlParse.addSchema2SQL(strScheme, strSQL);
				// 执行查询
				jdbcTemplate.update(strSQL, objParams);
				blnFlag = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		return blnFlag;
	}

	/**
	 * 通过 SQL语句插入数据。SQL中的参数必须使用？作为占位符，所有参数必须通过参数列表传入
	 * 
	 * @param strDBName
	 *            数据库名
	 * @param strSQL
	 *            输入的SQL语句
	 * @param objParams
	 *            拼装参数对象数组，可以替换SQL中的“？”对应的值
	 * @return 是否插入成功 boolean
	 */
	public boolean insertBySQL(String strSQL, Object[] objParams,String strScheme) {
		try {
			Statement stmtJsql = CCJSqlParserUtil.parse(strSQL);
			// 如果当前的SQL语句是Insert语句，则执行；否则返回失败状态
	        if(stmtJsql instanceof Insert){
	    		return modifyBySQL(strSQL, objParams,strScheme);
	        }
		} catch (Exception e) {
			e.printStackTrace();
        	return false;
		}
    	return false;
	}

	/**
	 * 通过SQL语句修改数据。SQL中的参数必须使用？作为占位符，所有参数必须通过参数列表传入
	 * 
	 * @param strSQL
	 *            输入的SQL语句
	 * @param objParams
	 *            拼装参数对象数组，可以替换SQL中的“？”对应的值
	 * @param strScheme
	 *            数据库名
	 * @return 是否修改成功 boolean
	 */
	public boolean updateBySQL(String strSQL, Object[] objParams,String strScheme) {
		try {
			Statement stmtJsql = CCJSqlParserUtil.parse(strSQL);
			// 如果当前的SQL语句是Update语句，则执行；否则返回失败状态
	        if(stmtJsql instanceof Update){
	    		return modifyBySQL(strSQL, objParams, strScheme);
	        }
		} catch (Exception e) {
			e.printStackTrace();
        	return false;
		}
    	return false;
	}

	/**
	 * 通过SQL语句删除数据。SQL中的参数必须使用？作为占位符，所有参数必须通过参数列表传入
	 * 
	 * @param strSQL
	 *            输入的SQL语句
	 * @param objParams
	 *            拼装参数对象数组，可以替换SQL中的“？”对应的值
	 * @param strScheme
	 *            数据库名
	 * @return 是否删除成功 boolean
	 */
	public boolean deleteBySQL( String strSQL, Object[] objParams,String strScheme) {
		try {
			Statement stmtJsql = CCJSqlParserUtil.parse(strSQL);
			// 如果当前的SQL语句是Delete语句，则执行；否则返回失败状态
	        if(stmtJsql instanceof Delete){
	    		return modifyBySQL(strSQL, objParams, strScheme);
	        }
		} catch (Exception e) {
			e.printStackTrace();
        	return false;
		}
    	return false;
	}


	/**
	 * 通过SQL语句查询数据，返回List<Map>对象。一条数据也通过此方法查询。
	 * 
	 * @param strSQL
	 *            输入的SQL语句
	 * @param intPageSize
	 *            页面数据条数
	 * @param intPageNum
	 *            页面
	 * @param strDbType
	 *            数据库类型：oracle/mysql
	 *            
	 * @param strScheme
	 *            数据库名
	 * @return 以List<Map>方式组装的查询结果集 List<Map>
	 */
	public List<Map<String, Object>> listPageDataBySQL( String strSQL, Object[] objParams,
			int intPageSize, int intPageNum, String strDbType,String strScheme) {
		List<Map<String, Object>> listData = null;

		
		try {
			Statement stmtJsql = CCJSqlParserUtil.parse(strSQL);
			// 如果当前的SQL语句是Select语句，则执行；否则返回失败状态
	        if(stmtJsql instanceof Select){
	    		// 如果数据库连接成功（不为空）
	    		if (jdbcTemplate != null) {
    				Map<String, Integer> mapValue = getRowNumberInPage(intPageSize, intPageNum, strDbType);
    				// 处理SQL语句，加入分页处理
    				if ("mysql".equalsIgnoreCase(strDbType)) {
    					strSQL = strSQL + " limit " + mapValue.get("beginRecordNumber") + ","
    							+ mapValue.get("endRecordNumber");
    				} else if ("oracle".equalsIgnoreCase(strDbType)) {
    					strSQL = "select * from " + "(select ROWNUM as rowno,t.* from (" + strSQL + ") t "
    							+ "where ROWNUM<=" + mapValue.get("endRecordNumber") + ") d " + "where d.rowno>="
    							+ mapValue.get("beginRecordNumber");
    				} else {
    					return null;
    				}

    				listData=listBySQL(strSQL, objParams,strScheme,new User());
	    		}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return listData;
	}
	


	/**
	 * 通过SQL语句查询数据，返回List<Map>对象。 该方法可传递in动态参数，具体操作方法如下： 示例：Sql ："select * from
	 * table where a =:name and b in(:names)" 参数由Map mapParams传递 , 其中Map中参数的key与
	 * SQL中"："匹配的参数名相对应,以下为组成方法
	 * 
	 * List list = new ArrayList(); list.add("name1"); list.add("name2");
	 * ........ mapParams.put("name","条件1") // 与：name对应
	 * mapParams.put("names",list) // 与：names对应 然后调用 ，
	 * listBySQL（strSQ,mapParamsL）
	 * 
	 * @param strSQL
	 *            输入的SQL语句
	 * @param mapParams
	 *            拼装参数对象数组，可以替换SQL中的“:name”对应的值
	 * @param strScheme
	 *            数据库名
	 * @return 是否删除成功 boolean
	 */
	@SuppressWarnings({ "rawtypes" })
	public List<Map<String, Object>> listPageDataBySQL(String strSQL, Map mapParams,
			int intPageSize, int intPageNum, String strDbType,String strScheme) {
		List<Map<String, Object>> listData = null;
		
		try {
			Statement stmtJsql = CCJSqlParserUtil.parse(strSQL);
			// 如果当前的SQL语句是Select语句，则执行；否则返回失败状态
	        if(stmtJsql instanceof Select){
				strSQL=SqlParse.addSchema2SQL(strScheme, strSQL);

	    		// 如果数据库连接成功（不为空）
	    		if (jdbcTemplate != null) {
    				Map<String, Integer> mapValue = getRowNumberInPage(intPageSize, intPageNum, strDbType);
    				// 处理SQL语句，加入分页处理
    				if ("mysql".equalsIgnoreCase(strDbType)) {
    					strSQL = strSQL + " limit " + mapValue.get("beginRecordNumber") + ","
    							+ mapValue.get("endRecordNumber");
    				} else if ("oracle".equalsIgnoreCase(strDbType)) {
    					strSQL = "select * from " + "(select ROWNUM as rowno,t.* from (" + strSQL + ") t "
    							+ "where ROWNUM<=" + mapValue.get("endRecordNumber") + ") d " + "where d.rowno>="
    							+ mapValue.get("beginRecordNumber");
    				} else {
    					return null;
    				}

    				listData=listBySQL(strSQL, mapParams,strScheme);
	    		}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return listData;
	}

	/**
	 * 根据传入的分页参数，计算总页数、开始记录条数、结束记录条数
	 * 
	 * @param intPageSize
	 *            页面大小，即页面显示条数
	 * @param intPageNum
	 *            页码，即当前需要显示数据的页码
	 * @param strDBType
	 *            数据库类型。mysql/oracle
	 * @return 以Map拼装的结果： beginRecordNumber，开始记录条数、endRecordNumber，结束记录条数
	 *         Map<String,Integer>
	 */
	private Map<String, Integer> getRowNumberInPage(int intPageSize, int intPageNum, String strDBType) {
		if (intPageSize < 0) {
			intPageSize = 1;
		}
		if (intPageNum < 0) {
			intPageNum = 1;
		}
	
		Map<String, Integer> mapValue = new HashMap<String, Integer>();
		// 开始条数
		int intBeginReocrdNumber = 1 + (intPageSize * (intPageNum - 1));
		// 结束条数
		int intEndReocrdNumber = intPageSize * intPageNum;
		
		if("mysql".equalsIgnoreCase(strDBType)){
			intBeginReocrdNumber--;
			intEndReocrdNumber--;
		}
		
		mapValue.put("beginRecordNumber", intBeginReocrdNumber);
		mapValue.put("endRecordNumber", intEndReocrdNumber);
	
		return mapValue;
	}
	/*
	 * @param strSQL
	 *            输入的SQL语句
	 * @param objParams
	 *            拼装参数对象，可以替换SQL中的“:name”对应的值
	 * @param strScheme
	 *            数据库名
	 * @return 是否删除成功 boolean
	 * */
	@Override
	public List<Map<String, Object>> listBySQL(String strSQL, Object[] objParams, String strScheme,User user) {
		//System.out.println(strSQL+"+"+scheame);
		List<Map<String, Object>> listData = null;
		DBUtil.addOrChangeDataSource(user);
		try {
			Statement stmtJsql = CCJSqlParserUtil.parse(strSQL);
			// 如果当前的SQL语句是Select语句，则执行；否则返回失败状态
	        if(stmtJsql instanceof Select){
	    		// 如果数据库连接成功（不为空）
	    		if (jdbcTemplate != null) {
					strSQL=SqlParse.addSchema2SQL(strScheme, strSQL);
					//System.out.println(strSQL);
					// 执行查询
					listData = jdbcTemplate.query(strSQL, objParams, new RowMapper<Map<String, Object>>() {
						@Override
						// 返回结果对象处理的回调函数
						public Map<String, Object> mapRow(ResultSet rs, int intRowNum) throws SQLException {
							// 单个记录对象，用Map拼装
							Map<String, Object> mapRow = new HashMap<String, Object>();
							// 获取当前查询表的字段列表
							ResultSetMetaData rsmd = rs.getMetaData();
							int intFieldCount = rsmd.getColumnCount();
							// 循环处理每个字段，将字段值put到Map中
							for (int i = 0; i < intFieldCount; i++) {
								mapRow.put(rsmd.getColumnName(i + 1), rs.getString(rsmd.getColumnName(i + 1)));
							}
	
							return mapRow;
						}
					});
	    		}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return listData;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Map<String, Object>> listBySQL(String strSQL, Map mapParams, String strScheme) {
		
		//System.out.println(strSQL+"++"+mapParams.toString());
		List<Map<String, Object>> listData = null;

		try {
			Statement stmtJsql = CCJSqlParserUtil.parse(strSQL);
			// 如果当前的SQL语句是Select语句，则执行；否则返回失败状态
	        if(stmtJsql instanceof Select){
	    		// 如果数据库连接成功（不为空）
	    		if (jdbcTemplate != null) {
					NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(
							jdbcTemplate);
					strSQL=SqlParse.addSchema2SQL(strScheme, strSQL);
					listData = namedParameterJdbcTemplate.queryForList(strSQL, mapParams);
	    		}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return listData;
	}

}

