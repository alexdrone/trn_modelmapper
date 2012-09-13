// 
//  ModelFactory
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper;

import modelmapper.exception.*;
import modelmapper.annotation.*;

import java.util.*;
import java.lang.reflect.*;

public abstract class ModelFactory {
	
	private Cache cache = new Cache(this);
	
	public Cache getCache() { return cache; }	
	
	public <T extends Model> T create(Class<T> type) {
		return create(type, getCache().createInstance(type));
	}
	
	/**
	 * Crates a proxy for <code>type</code>, with the given class 
	 * implementation <code>impl</code>
	 * @param type The model type
	 * @param impl The given implementation 
	 * @return A model @link{java.lang.reflect.Proxy}
	 */
	protected <T extends Model> T create(Class<T> type, ModelProxy impl) {
		
		T back = null;
		try { 
			back = (T) Proxy.newProxyInstance(type.getClassLoader(), 
					   new Class[] {type}, impl);
					
		} catch (Exception e) { 
			throw new ModelRuntimeException(e.getMessage()); 
		}
		
		impl.factory = this;
		impl.type = type;
		
		/* sounds a little bit recoursive, doesn't it? */
		impl.implementation = impl;
		
		/* for handling multiple inheritance */
		getCache().initSupers(type, impl);
				
		/* timestamp informations */
		java.sql.Timestamp now = 
			new java.sql.Timestamp((new Date()).getTime());
		
		if (!impl.fields.containsKey("CreatedAt"))
			impl.fields.put("CreatedAt", now);
			
		if (!impl.fields.containsKey("UpdatedAt"))
			impl.fields.put("UpdatedAt", now);
			
		/* Finally */	
		return back;		
	}
	
	/**
	 * Saves all changed (dirty) fields within the model to the database. 
	 * @param o the model that will be saved on the database.
	 */
	public abstract void save(Model o);
	
	/**
	 * Deletes the specified entities from the database. 
	 * This method does attempt to group the deletes on a per-type basis.
	 * Thus, this method scales very well for large numbers of entities 
	 * grouped into types. 
	 * However, the execution time increases linearly for each entity of 
	 * unique type.
	 * @param entities varargs array of entities to delete. Method returns 
	 * immediately if length == 0.
	 * @throws ModelRuntimeException
	 */
	public abstract void delete(Model... entries);
	
	/**
	 * Convenience method to select all entities of the given type 
	 * with the specified, parameterized criteria.
	 * @param type the type of the entities to retrieve.
	 * @param criteria A parameterized search statement.
	 * @param parameters A varargs array of params to be passed to the search.
	 * @return An array of entities of the given type which match 
	 * the specified criteria. 
	 */
	public <T extends Model> T[] find(Class<T> type, String criteria, 
	Object... params) {
		return advancedFind(type, criteria, null, false, 0, null, params);
	}	
	
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
	public abstract <T extends Model> T[] advancedFind(Class<T> type, 
	String criteria, String orderBy, boolean desc, int limit, 
	String[] includedConnections, Object[] params);
		
	/** 
	 * This method is responsable of fetching the requested model after a 
	 * <code>get</code> invokation by <code>invokedBy</code>.
	 * It makes navigation through references possible.
	 * @param toFetch The model interfaces of the instances to fetch from the 
	 * datasource.
	 * @param invokedBy The model instance that invoked this method.
	 * @param conn The {@link modelmapper.annotations.Connection} related.
	 * @return A {@link Model} array with all the fetched instances.
	 */
	protected abstract Model[] fetch(Class toFetch, Model invokedBy, 
	Connection connection);
}