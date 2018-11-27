package com.cnki.provider.util;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.DropIndexNode;
import com.foundationdb.sql.parser.ParameterNode;
import com.foundationdb.sql.parser.TableName;
import com.foundationdb.sql.unparser.NodeToString;

public class SqlNode2Sting extends NodeToString {
	private String strDBName;

	public void setStrDBName(String strDBName) {
		this.strDBName = strDBName;
	}
	
	@Override
	protected String parameterNode(ParameterNode node) throws StandardException {
//        return "$" + (node.getParameterNumber() + 1);
        return "?";
    }
	
	@Override
    protected String tableName(TableName node) throws StandardException {
        String schema = strDBName;
        String table = node.getTableName();

        if (schema == null)
            return maybeQuote(table);
        else
            return maybeQuote(schema) + "." + maybeQuote(table);
    }
	
	@Override
    protected String dropIndexNode(DropIndexNode node) throws StandardException {
        StringBuilder str = new StringBuilder(node.statementToString());
        str.append(" ");
        str.append(existenceCheck(node.getExistenceCheck()));
        str.append(strDBName);
        str.append(".");
        str.append(maybeQuote(node.getIndexName()));
        return str.toString();
    }

}
