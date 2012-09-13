// 
//  MySQLModelFactory
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper.provider.mysql;

import modelmapper.*;
import modelmapper.exception.*;
import modelmapper.schema.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class MySQLModelFactory extends RDBMSModelFactory {
	
	
	public MySQLModelFactory(String uri, String user, String password) {
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(uri, user, password);
				
			ResultSet rs = 
				connection.getMetaData().getTables(null, null, null, null);
			while (rs.next()) { dbTables.add(rs.getString("TABLE_NAME")); }
			
		//TOFIX: catch it better 			
		} catch (Exception e) { 
			throw new ModelRuntimeException("MySQL error: " + e.getMessage()); 
		}	
	}
	
	/**
	 * Creates a new instance of the class <code>type</code>
	 * @param type the classname
	 * @return The proxy class for the entity
	 */
	public <T extends Model> T create(Class<T> type) {
		return super.create(type);
	}
		
	/**
	 * Saves all changed (dirty) fields within the model to the database. 
	 * If the entry is a new record it will make an INSERT, else an UPDATE 
	 * on that row.
	 * @param o the model that will be saved on the database.
	 */
	public void save(Model o) {
	
	}
	
	//TOFIX : Save - It doesn't works right now..
	/**	
	 * It updates an entry on the database by building an UPDATE statement.
	 * @param o The database entry */
	private String updateRecord(Model o) {
		return null;
	}
	
	//TOFIX : Save - It doesn't works right now..
	/**
	 * Insert a new record on the dabase.
	 * @param o The new database entry */
	private String insertNewRecord(Model o) {
		return null;
	}
	
	/**
	 * Deletes the specified entities from the database. 
	 * <code>DELETE</code> statements are called on the rows in the 
	 * corresponding tables and 
	 * the entities are removed from the instance cache. 
	 * This method does attempt to group the DELETE statements on a 
	 * per-type basis.
	 * Thus, this method scales very well for large numbers of entities 
	 * grouped into types. 
	 * However, the execution time increases linearly for each entity of 
	 * unique type.
	 * @param  entities varargs array of entities to delete. Method returns 
	 * immediately if length == 0.
	 * @throws ModelRuntimeException
	 */
	public void delete(Model... entries) { }
	
	/**
	 * Executes the specified SQL and extracts the given key field, 
	 * wrapping each row into a instance of the specified type.
	 * The SQL is not parsed or modified in any way. 
	 * As such, it is possible to execute database-specific queries using this 
	 * method without 
	 * realizing it. As such, be extremely careful about what SQL is executed 
	 * using this method, 
	 * or else be conscious of the fact that you may be locking yourself to a 
	 * specific DBMS.
	 * @param type the type of the entities to retrieve.
	 * @param query the query to execute
	 * @return An array of entities of the given type which match the 
	 * specified query.
	 * @throws ModelRuntimeException 
	 */
	public <T extends Model> T[] findWithSql(Class<T> type, String query) {
		return null;
	}
	
	/**
	 * Retrieves a JDBC {@link Connection} instance which corresponds
	 * to the database represented by the provider instance.  This Connection
	 * can be used to execute arbitrary JDBC operations against the database.
	 *
	 * @return	A new connection to the database or <code>null</code>
	 *if the driver could not be loaded.
	 */
	public java.sql.Connection getConnection() { 
		return connection; 
	}
	
	public RDBMSSchemaGenerator getSchemaGenerator() { 
		return new MySQLSchemaGenerator(); 
	}  
		
	public RDBMSQueryGenerator getQueryGenerator() { 
		return new MySQLQueryGenerator(); 
	}  
	
}