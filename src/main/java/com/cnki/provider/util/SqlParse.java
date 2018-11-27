package com.cnki.provider.util;


import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.SQLParser;
import com.foundationdb.sql.parser.StatementNode;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

public class SqlParse {
	
	/**
	 * 
	 * 将数据库Schema名称加入到SQL语句中数据表名的前面
	 * @param strSchema 数据库Schema名。MySQL为数据库名，Oracle为用户名
	 * @param strInputSQL 输入的需要处理的SQL语句
	 * @return 处理后的SQL语句，已经将输入的Schema加入到语句中的所有表名之前
	 */
	public static String addSchema2SQL(String strSchema, String strInputSQL) {
		String strOutputSQL = "";

		// 如果输入的Schema不为空，并且输入的SQL语句不为空，则处理SQL语句
		if(strSchema!=null&&!strSchema.isEmpty()
				&&strInputSQL!=null&&!strInputSQL.isEmpty()){
			
			try {
				// 解析SQL语句，判断SQL语句的类型。查询、更新、插入、删除，等DML语句单独调用对应的处理方法；其他DDL语句调用通用的处理方法。
				Statement stmtJsql = CCJSqlParserUtil.parse(strInputSQL);
		        if(stmtJsql instanceof Select){
		        	strOutputSQL=SqlParserUtil.sqlParserForSelect(strInputSQL,strSchema); 
		        }else if(stmtJsql instanceof Update){
		        	strOutputSQL=SqlParserUtil.sqlParserForUpdate(strInputSQL,strSchema); 
		        }else if(stmtJsql instanceof Insert){
		        	strOutputSQL=SqlParserUtil.sqlParserForInsert(strInputSQL,strSchema); 
		        }else if(stmtJsql instanceof Delete){
		        	strOutputSQL=SqlParserUtil.sqlParserForDelete(strInputSQL,strSchema); 
		        }else{
				    SQLParser parser = new SQLParser();
				    SqlNode2Sting unparser = new SqlNode2Sting();
				    unparser.setStrDBName(strSchema);
					StatementNode stmt;
					try {
						stmt = parser.parseStatement(strInputSQL);
						stmt.treePrint();
						strOutputSQL = unparser.toString(stmt);
					} catch (StandardException e) {
						e.printStackTrace();
					}
		        }
			} catch (JSQLParserException e) {
				e.printStackTrace();
			}
		}
 
		return strOutputSQL;
	}

	public static void main(String[] args) {
//		String sql = "insert into tar select * from boss_table bo, (" + "select a.f1, ff from emp_table a "
//				+ "inner join log_table b " + "on a.f2 = b.f3" + ") f " + "where bo.f4 = ? "
//				+ "group by bo.f6 , f.f7 having count(bo.f8) > 0 " + "order by bo.f9, f.f10";
//		String sql="update abc.tp_sys_tab set abc=1 where id=? ";
//        String sql = "select user from emp_table where id=:abc";
//       String sql = "update t set name = 'x' where id < 100 limit 10";
//      String sql = "SELECT ID, NAME, AGE FROM USER WHERE ID = ? limit 2";
//      String sql = "select * from aa.tp_sys_user limit 10";
      String sql = "select to_char(sysdate,'mi') as def from tablename order by abc";
		
		String strOut=addSchema2SQL("dev_m_81", sql);
		System.out.println("OutSQL==\n"+strOut);

	}

}
