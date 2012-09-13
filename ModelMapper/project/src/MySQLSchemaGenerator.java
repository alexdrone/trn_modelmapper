// 
//  MySQLSchemaGenerator
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper.provider.mysql;

import modelmapper.*;

import modelmapper.schema.*;
import modelmapper.exception.*;
import modelmapper.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.lang.reflect.*;

public class MySQLSchemaGenerator implements RDBMSSchemaGenerator {
	
	/**
	 * Generate the SQL DDL declaration for an {@link Attribute} 
	 * <code>a</code>.
	 * Every attribute properties will be translated in database specific
	 * SQL modifiers.
	 * @return A valid SQL attribute declaration.
	 */
	private String toDDL(Attribute a) { 
		String res = "\n\t" + a.name + " " + a.sqlType;
		res += a.isAutoIncrement ? " AUTO_INCREMENT" : "";
		
		if (!a.isId) res += a.isNotNull ? " NOT NULL" : " NULL";
		else res += " NOT NULL";
		
		res += a.isUnique ? " UNIQUE" : "";
		return res;
	}

	
	public String createDDL(String tableName, 
	Collection<Attribute> attributes, String idName) {
		
		String create = "CREATE TABLE " + tableName + "(";

		boolean first = true;
		for (Attribute a : attributes) {
			create += first ? "" : ",";
			create += toDDL(a);			
			first = false;
		} 

		/* Add primary keys */
		String primaryKeys = ""; first = true;

		for (Attribute a : attributes) {
			if (a.isId) { primaryKeys += first ? "" : ","; first = false; }
			primaryKeys += a.isId ? a.name : "";
		}

		if (!primaryKeys.equals(""))
			create += ",\n\tPRIMARY KEY(" + idName + ")";
			
		create += "\n) ENGINE=InnoDB DEFAULT CHARSET=latin1;\n";
		
		return create;
		
	}
	
	public String createConstraints(String tableName,
	Collection<Attribute> attributes, String idName) {
		
		String alter = "\nALTER TABLE " + tableName;
		boolean altered = false, first = true;
		for (Attribute a : attributes)
			if (a.isAForeignKey) {
				
				altered = true;
				alter += first ? "" : ",";
				
				alter += 
					"\n\tADD FOREIGN KEY(" + a.name + ") REFERENCES " + 
					a.foreignTable + "(" + a.foreignTableId + ")";
					
				alter += a.composition ? " ON DELETE CASCADE" : "";
				
				first = false;
			}
			
		return altered ? alter+";" : "";
	}
	
	/**
	 * Legacy method - TODO implement a new using the {@link Attribute} class.
	 * @param rship The relationship containing a Many to Many connection
	 * @return A CREATE table statement for the join table.
	 */
	@Deprecated
	public String createJoinTable(Relationship rship) {
		String statement = "";
		
		if (rship.type.equals(ConnectionType.ManyToMany)) {

			statement = "CREATE TABLE "+ rship.name +" (";
			
			String tA = getDefaultMappedType(
				CommonStatic.getModelIdType(rship.classB));
				
			String tB = getDefaultMappedType(
				CommonStatic.getModelIdType(rship.classA));

			statement += 
				"\n\tId " + getDefaultMappedType(Integer.TYPE) + 
				" AUTO_INCREMENT,";
				
			statement += 
				"\n\t" + rship.fieldA + " " + tA + "," + "\n\t" + 
				rship.fieldB + " " + tB + ",";

			statement += "\n\tPRIMARY KEY(Id),";
			
			statement += 
				"\n\tFOREIGN KEY(" + rship.fieldA + ") REFERENCES " + 
				rship.tableB + "(" +  CommonStatic.getModelId(rship.classB) + 
				"),"; 
							
			statement += 
				"\n\tFOREIGN KEY(" + rship.fieldB + ") REFERENCES " + 
				rship.tableA + "(" + CommonStatic.getModelId(rship.classA) + 
				")";

			statement += "\n) ENGINE=InnoDB DEFAULT CHARSET=latin1;\n\n";
		}

		return statement;	
	}

	public String getDefaultMappedType(Class type) {
		return MySQLTypes.getType(type);
	}
	
}