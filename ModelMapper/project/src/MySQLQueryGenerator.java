// 
//  MySQLModelFactory
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper.provider.mysql;

import modelmapper.*;


import java.util.*;

public class MySQLQueryGenerator implements RDBMSQueryGenerator {	
	
	/**
	 * Generates a valid <code>SELECT</code> query for a MySQL database
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
		String orderBy, boolean desc, int limit) {

		String limitStm = "";
		String orderByStm = "";

		if (limit > 0) limitStm = " LIMIT  " + limit;

		if (orderBy != null) {
			orderByStm += " ORDER BY " + orderBy;
			orderByStm += desc ? " DESC " : " ASC ";
		}
		
		String query = "SELECT ";

		boolean first = true;
		for (String tN : what) {
			query += first ? "" + tN + ".*" : "," + tN +".*";
			first = false;
		}	
		
		query += " FROM ";
		
		first = true;
		/* The tables specified in FROM */
		for (String table : from) {
			query += first ? "" + table : "," + table;
			first = false;
		}
		
		query += " WHERE " + criteria + orderByStm + limitStm;
		
		//System.out.println(query);
		
		return query;
	}
}