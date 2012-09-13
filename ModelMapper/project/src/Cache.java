// 
//  Cache static methods
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper;

import modelmapper.*;
import modelmapper.exception.*;
import modelmapper.annotation.*;

import java.util.*;
import java.lang.reflect.*;


/**
 * Class aimed to improve performance of field naming indirection.
 * Table and fields names are progressively saved into an hash map for better 
 * runtime performances
 * @author Alex Usbergo, Luca Querella
 * @version prototype
 */
public class Cache {
	
	/** Maps */

	private Map<Method, String> 
		fieldNames = new HashMap<Method, String>();
		
	private Map<Class, String>	
		tableNames = new HashMap<Class, String>();
	
	/** It contains the correct implementation for each model interface */
	private Map<Class, Class>
		implementations = new HashMap<Class, Class>();
	
	/** A direct mapping beetween class fields and type for each class*/
	private Map<Class, Map<String, Class>>
		types = new HashMap<Class, Map<String, Class>>();
	
	/** If the model extends other interfaces (directly), here there are */
	private Map<Class, List<Class>>
		superclasses = new HashMap<Class, List<Class>>();
		
	private Map<Class, List<Class>>
		allSuperclasses = new HashMap<Class, List<Class>>();
	
	private Map<Class, List<String>>
		belongsToFields = new HashMap<Class, List<String>>();
	
	private Map<Class, List<Class>>
		relatedClasses = new HashMap<Class, List<Class>>();
	
	/** Model Ids */
	private Map<Class, String>	modelIds = new HashMap<Class, String>();
	private Map<Class, Class>	modelIdTypes = new HashMap<Class, Class>();


	/* Connections caching data */
	private Map<Class, Map<String, Class>> connectionType = 
		new HashMap<Class, Map<String, Class>>();
		
	private Map<Class, Map<String, Boolean>> arrayConnections = 
		new HashMap<Class, Map<String, Boolean>>();

	private ModelFactory owner;
	
	protected Cache(ModelFactory owner) { this.owner = owner; }
	
	/**
	 * Returns the conventional field name for a getter method.
	 * @param m The field getter.
	 * @return The field name.
	 * @throws IllegalArgumentException if the method isn't a valid getter.
	 */
	protected String fieldName(Method m) {
		
		if (fieldNames.containsKey(m)) return fieldNames.get(m);
		
		String name = CommonStatic.fieldName(m);
		
		fieldNames.put(m, name);
		return name;
	}
	
	/**
	 * Returns the conventional table name for a {@link Model} interface.
	 * @param type A valid Model interface.
	 * @return The conventional table name for the given interface.
	 */ 
	protected String tableName(Class type) {
		
		if (tableNames.containsKey(type)) return tableNames.get(type);
		
		String tN = CommonStatic.tableName(type);

		tableNames.put(type, tN);
		return tN;
	}
	
	/**
	 * Returns the field with the @link{Id} in the Model interface.
	 * For single intheritance, if the Id is not specified, will be the same 
	 * of his father class.
	 * In multiple inheritance, in order to avoid multiple keys identification 
	 * the Id must be defined by the user.
	 * @param The model interface
	 * @return The Id field name, or "Id" (the default id) if there's not.
	 */
	protected String getModelId(Class model) {
		if (!modelIds.containsKey(model)) 
			modelIds.put(model, CommonStatic.getModelId(model));
			
		return modelIds.get(model);		
	}
	
	protected Class getModelIdType(Class model) {
		if (!modelIdTypes.containsKey(model)) 
			modelIdTypes.put(model, CommonStatic.getModelIdType(model));
			
		return modelIdTypes.get(model);		
	}
	
	/**
	 * Returns the correct implementation instance for the interface 
	 * <code>type</code>. For convention the implentation is named 
	 * <code>InterfaceName + Impl</code>.
	 * @param type The interface type
	 * @return The correct ModelProxy subclass (if exists), a ModelProxy 
	 * instance otherwise
	 * @throws MalformedModelException is the interface or the implementation 
	 * are not well formed (they don't respect the conventions).
	 * @throws ModelRuntimeException is any error occurs during the 
	 * instantiation 
	 */
	protected ModelProxy createInstance(Class type) {
		Class backClass;
		if (implementations.containsKey(type)) 
			backClass = implementations.get(type);
		
		else { /* type never seen */
			
			try { 
				Package pkg =  type.getPackage();
				String pkgN = pkg == null ? "" : pkg.getName();
								
				backClass = Class.forName(pkgN + tableName(type) + 
							CommonStatic.getImplementationSuffix());
							
			} catch(Exception e) { backClass = ModelProxy.class; }
			
			Validator.validateModel(type, backClass);
			
			initFieldsTypes(type);
			implementations.put(type, backClass);
		}
		
		ModelProxy impl = null; 
		try { impl = (ModelProxy)backClass.newInstance();
		} catch (Exception e) { 
			throw new ModelRuntimeException(e.getMessage()); 
		}
		
		return impl;
	}
	
	/**
	 * Checks if an interface declares a field
	 * @param fielName The name of the searched field.
	 * @param fieldType The field type.
	 * @param model The given model interface.
	 */
	protected boolean ownField(String fieldName, Class model) {
		Map<String, Class> fields = types.get(model);
		
		if (fields.containsKey(fieldName)) return true;
			
		return false;
	}
	
	/**
	 * Checks if an interface owns a field (search recursively in all the 
	 * superinterfaces)
	 * @param fielName The name of the searched field.
	 * @param fieldType The field type.
	 * @param model The given model interface.
	 */
	protected boolean ownFieldExtended(String fieldName, Class model) {
		if (ownField(fieldName, model)) return true;
		
		List<Class> supers = allSuperclasses.get(model);
		if (supers == null) return false;
		
		for (Class s : supers)
			if (ownField(fieldName, s)) return true;
			
		return false;
	} 
	
	/**
	 * Returns the type for each class field of the given {@link Model} class
	 * @return A mapping through names and types.
	 */
	protected Map<String, Class> getFieldsTypes(Class type) {
		if (!types.containsKey(type)) { 
			initFieldsTypes(type); 
			
		}
		
		return types.get(type);
	}

	private void initFieldsTypes(Class type) {
		if (types.containsKey(type)) return;
		
		//TODO: Checks the connections - handle them differently
		Map<String, Class> tMap = new HashMap<String, Class>();
		Method[] getters = CommonStatic.getGetters(type);
		
		for (Method m : getters) 
			if(!m.isAnnotationPresent(Connection.class))
				tMap.put(fieldName(m), m.getReturnType());
				
		/* In inheritance case */
		if (extendsModels(type)) {
			List<Class> supers = getSupers(type);
			
			for (Class c : supers)
				tMap.put(/*tableName(c)+*/getModelId(type), c);
		}
		
		tMap.put("CreatedAt", java.sql.Timestamp.class);
		tMap.put("UpdatedAt", java.sql.Timestamp.class);
		
		types.put(type, tMap);
	}
	
	/** 
	 * This section is written to handle the inheritance between the
	 * interfaces. The models supports multiple inheritance. 
	 * @param type The given {@link Model} interface.
	 * @parma impl The {@link ModelProxy} is the proxy implementation for 
	 * <code>type</code>. It will be modified after the execution of 
	 * <code>initSupers</code>.
	 */
	protected void initSupers(Class type, ModelProxy imp) {
		List<Class> sC = getSupers(type);

		/* Creates the superclass instances */
		for (Class i : sC) 
			if(!imp.supers.containsKey(i)) imp.supers.put(i,owner.create(i));
	}
	
	/**
	 * Returns a list of inherited {@link Model} interfaces (directly 
	 * inherited) of the given model class.
	 * @param type The given {@link Model} class type.
	 * @return A list of interfaces that <code>type</code> extends directly.
	 */
	protected List<Class> getSupers(Class type) {
		if (!superclasses.containsKey(type)) {
			List<Class> sC = CommonStatic.getSupers(type);
			superclasses.put(type, sC);
		}
			
		return superclasses.get(type);
	}
		
	/**
	 * Checks if the given {@link Model} <code>type</code> inherits some other
	 * models
	 * @param type The given interface.
	 * @return <code>true</code> if the {@link Model} extends other model 
	 * interfaces, <code>false</code> otherwise.
	 */
	protected boolean extendsModels(Class type) { 
		return getSupers(type).size() != 0; 
	}
	
	/**
	 * If a {@link Model} is part of a composition, and is the weak part 
	 * (is part of other models),
	 * this method returns all the fields containing connection of 
	 * {@link ConnectionType.BelongsTo} type.
	 * It's useful in the <code>INSERT</code> of the record, for preventing 
	 * foreign key costraint inconsistency.
	 * @param model The given {@link Model}.
	 * @return A list of fields that represent the Models that owns 
	 * <code>model</code>.
	 */
	protected List<String> belongsTo(Class model) {
		
		if (belongsToFields.containsKey(model)) 
			return belongsToFields.get(model);
		
		List<Class> sC = getSupers(model);		
		List<String> ownerFields = new ArrayList<String>();
		
		Method[] getters = CommonStatic.getDeclaredGetters(model);
		for (Method g : getters)
		
			/* has a belongs to annotation */
			if (g.isAnnotationPresent(Connection.class) &&
				g.getAnnotation(Connection.class).
				type().equals(ConnectionType.BelongsTo)) {
				
				ownerFields.add(fieldName(g));
			}
				
				
		for (Class s : sC) ownerFields.addAll(belongsTo(s));
		belongsToFields.put(model, ownerFields);
		
		return ownerFields;

	}
	
	/**
	 * Result of recursive calls at <code>getSupers</code>.
	 * It returns a list of all the inherited interfaces of <code>type</code>.
	 * @param type The given {@link Model} class type.
	 * @return The complete <code>super</code> hierarchy.
	 */
	protected List<Class> getAllSupers(Class type) {
		
		if (!allSuperclasses.containsKey(type)) {
			List<Class> sC = CommonStatic.getAllSupers(type);
			allSuperclasses.put(type, sC);
		}
			
		return allSuperclasses.get(type);
	}
	
	/**
	 * Returns all the classes related to <code>type</code> by a 
	 * {@link Connection}
	 * @param type The given {@link Model} interface.
	 * @return A list that contains all the models in a relationship with
	 *  <code>type</code>
	 * interface.
	 */
	 protected List<Class> getAllReleatedClasses(Class type) {
		
		if (relatedClasses.containsKey(type)) return relatedClasses.get(type);
		
		
		List<Class> related = new ArrayList<Class>();
		Method[] getters = CommonStatic.getDeclaredGetters(type);
		
		for (Method g : getters)
			if (g.isAnnotationPresent(Connection.class)) {
				
				Class t = g.getReturnType();
				t = t.isArray() ? t.getComponentType() : t;
				
				if (!related.contains(t)) related.add(t);
			}
			
		relatedClasses.put(type, related);
		
		return related;
	}
	
	protected Class getConnectionFieldType(Class model, String field) {
		
		if (!connectionType.containsKey(model)) {
			connectionType.put(model, new HashMap<String, Class>());
		}
		
		Map<String, Class> map = connectionType.get(model);
		
		if (map.containsKey(field.toLowerCase())) 
			return map.get(field.toLowerCase());
			
		else map.put(field.toLowerCase(), 
			 CommonStatic.getConnectionFieldType(model, field));
			
		return map.get(field.toLowerCase());
	}
		
	protected boolean isAnArrayConnection(Class model, String field) {
		
		if (!arrayConnections.containsKey(model)) {
			arrayConnections.put(model, new HashMap<String, Boolean>());
		}
		
		Map<String, Boolean> map = arrayConnections.get(model);
		
		if (map.containsKey(field.toLowerCase())) 
			return map.get(field.toLowerCase());
			
		else map.put(field.toLowerCase(), 
			 CommonStatic.isAnArrayConnection(model, field));
			
		return map.get(field.toLowerCase());
	}

}