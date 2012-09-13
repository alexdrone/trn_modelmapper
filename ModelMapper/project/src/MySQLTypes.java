// 
//  MySQLTypes
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper.provider.mysql;

import modelmapper.*;

import java.math.BigDecimal;
import java.util.*;
import java.lang.reflect.*;


public class MySQLTypes  {
	
	/* Basic primitives default mapping */
	protected static String getType(Class type) {

		//mapping i byte[] at image/varbinary?
		if (type.equals(Long.TYPE)) return TYPE_BIGINT;
	
	 	if (type.equals(Boolean.TYPE)) return  TYPE_BIT; 

		if (type.equals(Character.TYPE)) return TYPE_CHAR; 

		if (type.equals(Integer.TYPE)) return TYPE_INT;	

		if (type.equals(Short.TYPE)) return TYPE_TINYINT;

		if (type.equals(Float.TYPE) || type.equals(Double.TYPE)) 
			return TYPE_DOUBLE;

		if (type.equals(BigDecimal.class))	return TYPE_DECIMAL; 

		if (type.equals(java.sql.Timestamp.class) 
		 || type.equals(java.sql.Time.class)) return TYPE_DATETIME;

		//mapping a text? 
		if (type.equals(String.class))	return TYPE_VARCHAR; 

		if (type.equals(java.sql.Blob.class)) return TYPE_VARBINARY;

		if (type.equals(java.sql.Clob.class)) return TYPE_TEXT;

		if (type.equals(java.sql.Date.class)) return TYPE_DATE;
		
		return null;
	}
		

	/* default data types names */
	private static String TYPE_BIGINT = "bigint";
	private static String TYPE_BIT = "bit";
	private static String TYPE_CHAR = "char";
	private static String TYPE_INT = "int";
	private static String TYPE_TINYINT = "tinyint";
	private static String TYPE_DOUBLE = "double";
	private static String TYPE_DECIMAL = "decimal";
	private static String TYPE_DATETIME = "datetime";
	private static String TYPE_VARCHAR = "varchar(255)";
	private static String TYPE_VARBINARY = "varbinary(max)";
	private static String TYPE_TEXT = "text";
	private static String TYPE_DATE = "date";
}