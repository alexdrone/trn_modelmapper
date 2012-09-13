// 
//  MySQLModelFactory
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper;

import modelmapper.*;
import modelmapper.exception.*;
import modelmapper.schema.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

public interface RDBMSQueryGenerator {
	
	/**
	 * Generates a valid <code>SELECT</code> query for specific database
	 * @param what The selection predicate.
	 * @param from The tables to add in the search.
	 * @param criteria The custom where statement generate by the 
	 * <code>find</code> metod in {@link ModelFactory}.
	 * @param orderBy The order criteria.
	 * @param desc <code>true</code> for descending, <code>false</code> for
	 * ascending.
	 * @param limit A limit to the <code>SELECT</code> result set.
	 * @return The generated SQL query.
	 */
	public String generateSELECT(
		Collection<String> what, Collection<String> from, String criteria, 
		String orderBy, boolean desc, int limit);
	
}