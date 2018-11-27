package com.cnki.provider.util;


import java.io.StringReader;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.UpdateDeParser;  
  
  
/** 
 * @ 
 * @author Administrator 
 *  
 */  
  
  
public class SqlParserUtil extends SelectDeParser {  
    private String schemaName;  
    public void setSchemaName(String schemaName) {  
        this.schemaName = schemaName;  
    }  
  
    public SqlParserUtil(ExpressionVisitor expressionVisitor,  
            StringBuilder buffer) {  
        super(expressionVisitor, buffer);  
    }  
  
    public void visit(Table tableName) {  
        tableName.setSchemaName(schemaName);  
        StringBuilder buffer = getBuffer();  
        buffer.append(tableName.getFullyQualifiedName());  
        Pivot pivot = tableName.getPivot();  
        if (pivot != null) {  
            pivot.accept(this);  
        }  
        Alias alias = tableName.getAlias();  
        if (alias != null) {  
            buffer.append(alias);  
        }  
    }  
  
  
    public static String sqlParserForSelect(String sql, String schemaName) {  
        Select select;  
        StringBuilder buffer = new StringBuilder();  
        try {  
            select = (Select) CCJSqlParserUtil.parse(sql);  
            ExpressionDeParser expressionDeParser = new ExpressionDeParser();  
            SqlParserUtil deparser = new SqlParserUtil(expressionDeParser,  
                    buffer);  
            deparser.setSchemaName(schemaName);  
            expressionDeParser.setSelectVisitor(deparser);  
            expressionDeParser.setBuffer(buffer);  
            select.getSelectBody().accept(deparser);  
        } catch (JSQLParserException e) {  
            e.printStackTrace();  
        }  
  
        return buffer.toString();  
    }  
  
    public static String sqlParserForInsert(String sql, String schemaName) {  
        StringBuilder buffer = new StringBuilder();  
        Insert Statement = null;  
        try {  
            CCJSqlParserManager parser = new CCJSqlParserManager();  
            Statement stmt = parser.parse(new StringReader(sql));  
            Statement = (Insert) stmt;  
  
            Table t = Statement.getTable();  
            t.setSchemaName(schemaName);  
            Statement.setTable(t);  
  
            Select select = Statement.getSelect();  
            if (select != null) {  
                ExpressionDeParser expressionDeParser = new ExpressionDeParser();  
                SqlParserUtil deparser = new SqlParserUtil(expressionDeParser,  
                        buffer);  
                deparser.setSchemaName(schemaName);  
                expressionDeParser.setSelectVisitor(deparser);  
                expressionDeParser.setBuffer(buffer);  
                select.getSelectBody().accept(deparser);  
            }  
        } catch (JSQLParserException e) {  
            e.printStackTrace();  
        }  
        return Statement.toString();  
    }  
  
  
    /** 
     *  
     * @param sql 
     * @param schemaName 
     * @return 
     */  
    public static String sqlParserForUpdate(String sql, String schemaName) {  
        StringBuilder buffer = new StringBuilder();  
        Update Statement = null;  
        try {  
            CCJSqlParserManager parser = new CCJSqlParserManager();  
            Statement stmt = parser.parse(new StringReader(sql));  
            Statement = (Update) stmt;  
  
            List<Table> list = Statement.getTables();  
            for (Object object : list) {  
                Table t = (Table) object;  
                t.setSchemaName(schemaName);  
            }  
            Statement.setTables((List<Table>) list);  
  
            // 处理from  
            FromItem fromItem = Statement.getFromItem();  
            if (fromItem != null) {  
                Table t = (Table) fromItem;  
                t.setSchemaName(schemaName);  
  
  
            }  
  
            // 处理join  
            List<Join> joins = Statement.getJoins();  
            if (joins != null && joins.size() > 0) {  
                for (Object object : joins) {  
                    Join t = (Join) object;  
                    Table rightItem = (Table) t.getRightItem();  
                    rightItem.setSchemaName(schemaName);  
                }  
            }  
  
            ExpressionDeParser expressionDeParser = new ExpressionDeParser();  
            UpdateDeParser p = new UpdateDeParser(expressionDeParser, null,  
                    buffer);  
            expressionDeParser.setBuffer(buffer);  
            p.deParse(Statement);  
        } catch (JSQLParserException e) {  
            e.printStackTrace();  
        }  
        return Statement.toString();  
  
    }  
  
  
    public static String sqlParserForDelete(String sql, String schemaName) {  
        Delete Statement = null;  
        try {  
            CCJSqlParserManager parser = new CCJSqlParserManager();  
            Statement stmt = parser.parse(new StringReader(sql));  
            Statement = (Delete) stmt;  
  
  
            Table t = Statement.getTable();  
            t.setSchemaName(schemaName);  
            Statement.setTable(t);  
  
            Expression where = Statement.getWhere();  
            InExpression getRightItems = null;  
            if (where != null) {  
                getRightItems = (InExpression) where;  
                ItemsList rightItemsList = getRightItems.getRightItemsList();  
                if (rightItemsList instanceof SubSelect) {  
                    SubSelect s = (SubSelect) rightItemsList;  
                    SelectBody selectBody = s.getSelectBody();  
                    Select se = (Select) CCJSqlParserUtil.parse(selectBody  
                            .toString());  
  
  
                    StringBuilder buffer = new StringBuilder();  
                    ExpressionDeParser expressionDeParser = new ExpressionDeParser();  
                    SqlParserUtil deparser = new SqlParserUtil(  
                            expressionDeParser, buffer);  
                    deparser.setSchemaName(schemaName);  
                    expressionDeParser.setSelectVisitor(deparser);  
                    expressionDeParser.setBuffer(buffer);  
                    se.getSelectBody().accept(deparser);  
  
  
                    s.setSelectBody(se.getSelectBody());  
                }  
            }  
  
        } catch (JSQLParserException e) {  
            e.printStackTrace();  
        }  
        return Statement.toString();  
  
  
    }  
  
  
    public static void main(String[] args) {  
        // String sqlParserForInsert =  
        // sqlParserForInsert("Insert into Table2 select  *  from Table1",  
        // "two");  
        String sql = "delete from persp where id in "
        		+ "(select p.user_id "
        		+ "from per p left join test a on(a.id = b.id)"
        		+ " inner join test t on(p.id = t.id)"
        		+ " where t=1 and id in (:ids) )";  
        
		try {
			Statement stmt = CCJSqlParserUtil.parse(sql);
	        if(stmt instanceof Select){
		        System.out.println(sqlParserForSelect(sql,"CUST1")); 
	        }else if(stmt instanceof Update){
		        System.out.println(sqlParserForUpdate(sql,"CUST1")); 
	        }else if(stmt instanceof Insert){
		        System.out.println(sqlParserForInsert(sql,"CUST1")); 
	        }else if(stmt instanceof Delete){
		        System.out.println(sqlParserForDelete(sql,"CUST1")); 
	        }
		} catch (JSQLParserException e) {
			e.printStackTrace();
		}
    }  
}  