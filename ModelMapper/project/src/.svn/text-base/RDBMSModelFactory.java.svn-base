// 
//  RDBMSModelFactory
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper;

import modelmapper.exception.*;
import modelmapper.schema.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public abstract class RDBMSModelFactory extends ModelFactory {
	
	/* Tables available in the database */
	protected List<String> dbTables = new ArrayList<String>();
	
	protected Connection connection;
	
	protected RDBMSSchema schema = new RDBMSSchema(this);
		
	/**
	 * Crates a proxy for <code>type</code>, with the given class 
	 * implementation <code>impl</code>
	 * @param type The model type
	 * @param impl The given implementation 
	 * @return A model @link{java.lang.reflect.Proxy}
	 */
	protected <T extends Model> T create(Class<T> type, ModelProxy impl) {
		T creation = super.create(type, impl);
		schema.initSchema(type);
		
		return creation;
	}
		
	/**
	 * Executes the specified SQL and extracts the given key field, 
	 * wrapping each row into a instance of the specified type.
	 * The SQL is not parsed or modified in any way. 
	 * As such, it is possible to execute database-specific queries using this 
	 * method without realizing it. As such, be extremely careful about what 
	 * SQL is executed using this method, 
	 * or else be conscious of the fact that you may be locking yourself to 
	 * a specific DBMS.
	 * @param type the type of the entities to retrieve.
	 * @param query the query to execute
	 * @return An array of entities of the given type which match the 
	 * specified query.
	 * @throws ModelRuntimeException 
	 */
	public abstract <T extends Model> T[] findWithSql(Class<T> t, String q);
	
	/**
	 * Retrieves a JDBC {@link Connection} instance which corresponds
	 * to the database represented by the provider instance.  This Connection
	 * can be used to execute arbitrary JDBC operations against the database.
	 *
	 * @return	A new connection to the database or <code>null</code>
	 *if the driver could not be loaded.
	 */
	public abstract java.sql.Connection getConnection();
	
	/**
	 * Returns the database specific {@link RDBMSSchemaGenerator}.
	 * @return A schema generator for the specific RDBMS database.
	 */
	public abstract RDBMSSchemaGenerator getSchemaGenerator();
	
	/**
 	 * Returns the database specific {@link RDBMSQueryGenerator}.
 	 * @return A schema generator for the specific RDBMS database.
 	 */	
	public abstract RDBMSQueryGenerator getQueryGenerator();


	public RDBMSSchema getSchema() { return schema; }
	
	/**
	 * Convenience method to select all entities of the given type 
	 * with the specified, parameterized criteria.
	 * @param type the type of the entities to retrieve.
	 * @param criteria A parameterized search statement.
	 * @param parameters A varargs array of params to be passed to the search.
	 * @param limit A limit to the results number.
	 * @param orderBy The key field for ordering the result set
	 * @param desc <code>true</code> for descending, <code>false</code>
	 * for ascending.
	 * @return An array of entities of the given type which match the 
	 * specified criteria. 
	 */
	public <T extends Model> T[] advancedFind(
		Class<T> type, String criteria, String orderBy, boolean desc, 
		int limit, String[] includedConnections, Object[] params) {
			
		create(type);
			
		/* timing the query */
		java.util.Date start = new java.util.Date();
			
		/* In the case the user call a method include(...) on a Finder
		 * the relationships have to be fetched  in a single query for avoid
		 * the N + 1 query problem in a loop.
		 * This section of code handle this problem by adding the right
		 * JOIN statement, and later the packing will be different.
		 */
		modelmapper.annotation.Connection[] connections = null;
		if (includedConnections != null && includedConnections.length > 0) {
			
			if (criteria.length() > 0) criteria += " and ";
			/* creates the array */
			connections = new
				modelmapper.annotation.Connection[includedConnections.length];
				
			for (int i = 0; i < includedConnections.length; i++) {
				connections[i] = 
				CommonStatic.searchConnection(type, includedConnections[i]);
				criteria += includeConnection(type, connections[i]);
				
				if (i != includedConnections.length-1) criteria += " and ";
			}			
		}
		
		/* The upper section got no functionalities at the moment */
				
		/* select FROM */
		Set<String> from = new HashSet<String>();
		from.add(getCache().tableName(type).toLowerCase());
		
		List<Class> supersInterfaces = CommonStatic.getAllSupers(type);
		
		for (Class s : supersInterfaces)
			from.add(getCache().tableName(s).toLowerCase());
		
		/* Search if the WHERE statement uses some other table
		 * (condition like TABLE1.field = ?) */
		for (String table : dbTables)
			if (criteria.toLowerCase().indexOf(table.toLowerCase()+".")!= -1) 
				from.add(table.toLowerCase());
				
		Set<Class> whatInterfaces = new HashSet<Class>();
		whatInterfaces.add(type);
		whatInterfaces.addAll(supersInterfaces);
			
		if (includedConnections != null) {	
			for (String c : includedConnections)
				whatInterfaces.add(
				CommonStatic.getConnectionFieldType(type, c));
		}
		/* select WHAT */
		List<String> what = new LinkedList<String>();
		for (Class i : whatInterfaces) what.add(getCache().tableName(i));
		
		//TODO: Add to what the preload connections chosen
		
		String gCriteria = "";
		
		/* Inheritance implicit join case - The find must retrieve all the data
		 * of the instance superclasses, and for relating the superinstances to
		 * this, a JOIN through these tables is necessary.
		 * User don't know how inheritance is implemented. */
		if (getCache().extendsModels(type)) {
			List<Class> supers = getCache().getSupers(type);
			
			for (Class s : supers) 
				gCriteria += 
					/* super criteria adding */
					getCache().tableName(s) + "." + getCache().getModelId(s) + 
					" = " + getCache().tableName(type) + "." + 
					getCache().tableName(s) + getCache().getModelId(s) + 
					" and "; 
								
		}
		
		/* Adds the user criteria */
		if (criteria != null && criteria.length() > 1) gCriteria += criteria;
		else gCriteria += " true ";
				
		String query = getQueryGenerator().generateSELECT(what, from, 
					   gCriteria, orderBy, desc, limit); 
		
		System.out.println("Find#<SQL: " + query.toLowerCase());
		
		ResultSet rs;
		
		try { /* Packing object */
			PreparedStatement statement = connection.prepareStatement(query);

			for (int i = 0; i < params.length; i++) 
				statement.setObject(i+1, params[i]);		
					
	 		rs = statement.executeQuery();
	
		} catch (SQLException e) { 
			throw new ModelRuntimeException(e.getMessage()); 
		}
		
		
		/* Packing the objects */
		List<Model> back = new ArrayList<Model>();
		
		try {
			
			if (connections == null) 
				while(rs.next()) back.add(pack(type, rs));
				
			else packCompound(back, type, rs, includedConnections, connections);
			
				
		} catch (SQLException e) { 
			throw new ModelRuntimeException(e.getMessage()); 
		}
		
		//TODO: Pack and add the connections.

		/* timing the query */
		double ms = (new java.util.Date()).getTime()-start.getTime();
		System.out.print(">  executed and packed in " + ms + "ms.\n");

		return 
			back.toArray((T[]) 
			java.lang.reflect.Array.newInstance(type, back.size()));	
	}
	
	/**
	 * Packs an result set row into an object.
	 * If the given model type extends other models, all the data of the 
	 * superclasses
	 * are recursively packed into the object.
	 * @param type The class type.
	 * @param rs The result set line.
	 * @return A valid instance of the model <code>T</code>
	 */
	private <T extends Model> T pack(Class<T> type, ResultSet rs) {
				
		ModelProxy instance = getCache().createInstance(type);
		
		try {
			Set<String> fields = getCache().getFieldsTypes(type).keySet();
			String tN = getCache().tableName(type);
			
			for (String f : fields) 
				if(!CommonStatic.isAModelType(type)) 
					instance.fields.put(f,rs.getObject(tN + "." + f));
				
			instance.newRecord = instance.dirty = false;
				
			for (Class s : getCache().getSupers(type)) {
				Model y = pack(s, rs);
				instance.supers.put(s, y);	
			}
						
		} catch (SQLException e) { 
			
			e.printStackTrace();
		}
		
		return create(type, instance);
	}
	
	/** 
	 * This method is responsable of fetching the requested model after a 
	 * <code>get</code> invokation by <code>invokedBy</code>.
	 * It makes navigation through references possible.
	 * @param toFetch The model interfaces of the instances to fetch from the 
	 * database.
	 * @param invokedBy The model instance that invoked this method.
	 * @param conn The {@link modelmapper.annotations.Connection} related.
	 * @return A {@link Model} array with all the fetched instances.
	 */
	protected Model[] fetch(Class toFetch, Model invokedBy, 
	modelmapper.annotation.Connection conn) {
		
		Relationship r = getSchema().getNamedRelationship(conn.name());
		
		String idA = getCache().getModelId(r.classA);
		String idB = getCache().getModelId(r.classB);
				
		/* Case BelongsTo */
		if (conn.type().equals(
			modelmapper.annotation.ConnectionType.BelongsTo)) {
		
			String condition = 
				r.tableB + "." + r.fieldB + " = " + r.tableA + "." + idA + 
				" and " + r.tableB + "." + idB + " = ?"; 
			
			return find(toFetch, condition, invokedBy.fields().get(idB));
		
		/* Case Composition/Aggregation */
		} else if (!conn.type().equals(
					modelmapper.annotation.ConnectionType.ManyToMany)) {
			
			String condition = 
				r.tableB + "." + r.fieldB + " = " + r.tableA + "." + idA + 
				" and " + r.tableA + "." + idA + " = ?";
			
			return find(toFetch, condition, invokedBy.fields().get(idA));
		
		
		} else { /* Many to Many */
			
			String idParam = 
				getCache().getModelId(invokedBy.modelInterface());
			
			String tableParam =
				getCache().tableName(invokedBy.modelInterface());
			
			String condition = 
			r.tableA + "." + idA + " = " + r.name + "." + r.fieldB + " and " + 
			r.tableB + "." + idB + " = " + r.name + "." + r.fieldA + " and " + 
			tableParam + "." + idParam + " = ? ";
							
			return find(toFetch, condition, invokedBy.fields().get(idParam));
		}
	}
	
	/** 
	 * It generates the JOIN condition for retrieve the given connection 
	 * @param model The {@link Model} that own the connection
	 */
	private String includeConnection(Class model, 
	modelmapper.annotation.Connection conn) {

		//modelmapper.annotation.Connection conn = 
		//	CommonStatic.getConnection(model, connectionName);
			
		Relationship r = getSchema().getNamedRelationship(conn.name());

		String condition = " ";
		String idA = getCache().getModelId(r.classA);
		String idB = getCache().getModelId(r.classB);

		if (conn.type().equals(
			modelmapper.annotation.ConnectionType.BelongsTo))

			condition = 
				r.tableB + "." + r.fieldB + " = " + r.tableA + "." + idA;

		else if (!conn.type().equals(
				  modelmapper.annotation.ConnectionType.ManyToMany))

			condition = 
				r.tableB + "." + r.fieldB + " = " + r.tableA + "." + idA;

		else  
			condition = 
			r.tableA + "." + idA + " = " + r.name + "." + r.fieldB + " and " + 
			r.tableB + "." + idB + " = " + r.name + "." + r.fieldA + " ";

		return condition;
	}

	/**
	 * Pack a compound (used in include finder query).
	 * In the case the user call a method include(...) on a Finder
	 * the relationships have to be fetched in a single query for avoid
	 * the N + 1 query problem in a loop.
	 * This section of code handle this problem by adding the right
	 * JOIN statement, and later the packing will be different.
	 */
	private void packCompound(List<Model> back, Class type, ResultSet rs, 
	String[] fields, modelmapper.annotation.Connection[] conn) 
	throws SQLException {
	
		Model prev = null;
		List<Model> list = new ArrayList<Model>();
		
		while (rs.next()) {
			
			Model o = pack(type, rs);
			if (!o.equalsId(prev)) { back.add(o); prev = o; }
			
			for (int i = 0; i < fields.length; i++) {
				Relationship r = 
					getSchema().getNamedRelationship(conn[i].name());
					
				Class returnType = 
					getCache().getConnectionFieldType(type, fields[i]);

				Model c = pack(returnType, rs);
				prev.modelImplementation().addModels(fields[i],
													 new Model[]{c}, true);
			}
		}
		
	}
	
	
	
}