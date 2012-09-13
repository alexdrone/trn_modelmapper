// 
//  ModelProxy
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper;

import modelmapper.annotation.*;
import modelmapper.exception.*;

import java.util.*;
import java.lang.reflect.*;

public class ModelProxy implements Model, InvocationHandler {
	
	protected ModelFactory factory;
	protected Class type;
	
	/** contains all the fields values indexed by name */
	protected Map<String, Object> fields = new HashMap<String, Object>();
	
	public Map<String, List<Model>> 
		fetched = new HashMap<String, List<Model>>();
	
	/* Not fetched connections, index for Id */
	protected Map<String, Boolean> isFetched = new HashMap<String, Boolean>();
	
	protected boolean dirty = true;
	protected boolean newRecord = true;
	
	protected ModelProxy implementation;
	
	/** 
	 * Contains all the superclasses instances, used in the case this extends 
	 * any other Model.
	 * Methods are recursively called on the correct superclass instance that
	 * implements that method.
	 * Conflicts between methods are not allowed by the Model validator. 
	 */
	protected HashMap<Class, Model> supers = new HashMap<Class, Model>();
	
	/**
	 * Saves all changed (dirty) fields within the entity to the datasource.  
	 * This method should almost never be overridden within a defined
	 * implementation.  However, it is possible to do so if absolutely
	 * necessary.
	 */
	public void save() { if (validate()) factory.save(this); }
	
	/**
	 * Deletes the model from the database.
	 * If this owns other models with a {@link Connection} of type 
	 * <code>ConnectionType.Composition</code>, these will be deleted
	 * in cascade.
	 */
	public void delete() {}
	
	/**
	 * This method has to be overriden by the subclasses if they want 
	 * to implement some before-save validation.
	 * @return <code>true</code> if the Model is valid, <code>false</code>
	 * otherwise.
	 */
	public boolean validate() { return true; }
	
	/**
	 * The default ModelMapper Id.
	 * If nothing is specified, the serial field using for object 
	 * identification will be this integer (with autoincrement).
	 * @throws ModelRuntimeException If the user defines a different Id field
	 * and tries to invoke <code>getId()</code>.
	 * @return The Id value, or <code>null</code> if it's not defined yet.
	 */
	public int getId() {
										
		if (!factory.getCache().getModelId(type).equals("Id") ||
			!factory.getCache().getModelIdType(type).equals(Integer.TYPE))	
			
			throw new ModelRuntimeException("Default Id not available.");
						
		return get("Id", Integer.TYPE);			
	}
	
	/**
	 * Sets the Id value for this instance.
	 * @param id The new class Id.
	 * @throws ModelRuntimeException If the user defines a different Id field 
	 * and tries to invoke <code>setId()</code>.
	 */
	public void setId(int id) {
					
		if (!factory.getCache().getModelId(type).equals("Id") ||
			!factory.getCache().getModelIdType(type).equals(Integer.TYPE))	
			
			throw new ModelRuntimeException("Default Id not available.");
			
		else set("Id", id);
	}
 
	/**
	 * Getter for the Model primitive fields.
	 * @param field The field name.
	 * @param returnType The expected return type.
	 */
	private <R extends Object> R get(String field, Class<R> returnType) {
		Map f = this.fields();
		if (f.containsKey(field)) return (R) f.get(field);
		
		return null;
	}
	
	/**
	 * Get the connected model linked by the <code>getField</code> method.
	 * This is uses only for */
	private <R extends Model> R getConnection(String field, Class<R> type, 
	Connection c) {
		
		field = field.toLowerCase();
		getConnectionArray(field, type, c);

		List<R> list = (List<R>) fetched.get(field);
		
		if (list.size() > 0) return (R) fetched.get(field).get(0);
		else return null;
	}
	
	protected <R extends Model> R[] getConnectionArray(String field, 
	Class<R> type, Connection c) {
		
		field = field.toLowerCase();
		
		if (!fetched.containsKey(field)) { 
			R[] objs = (R[]) factory.fetch(type, this, c);
			
			List<Model> list = new ArrayList<Model>();
			
			String id = factory.getCache().getModelId(type);
			
			if (objs != null) for (R o : objs) list.add(o);
				
			fetched.put(field, list);
		
		/* 
		 * It contains elements, but they're added by hand and they've not
		 * been fetched from the database.
		 */
		} else if (isFetched.containsKey(field) && !isFetched.get(field)) {
			
			R[] objs = (R[]) factory.fetch(type, this, c);
			List<R> list = (List<R>) fetched.get(field);
			
			if (objs != null) for (R o : objs) list.add(o);
		}
		
		isFetched.put(field, true);
		List<R> back = (List<R>) fetched.get(field);
		
		return 
			back.toArray((R[]) 
			java.lang.reflect.Array.newInstance(type, back.size()));
	}
	
	protected void addModels(String field, Model[] models, boolean isAFetch) {
				
		field = field.toLowerCase();
		
		isFetched.put(field, isAFetch);
		
		if (!fetched.containsKey(field))
			fetched.put(field, new ArrayList<Model>());
		
		List<Model> list = fetched.get(field);

		if (models == null) return;
		
		for (Model m : models) list.add(m);
			
	}
	

	
		
	private void set(String field, Object value) {
		if (!extendsModels() || 
			factory.getCache().ownField(field, this.type)) { 
				
			fields.put(field, value); 
			return; 
		}
		
		/* searching the field in the superclasses */
		Set<Class> keys = supers.keySet();
		
		for (Class k : keys) {
			ModelProxy sI = supers.get(k).modelImplementation();
			if (factory.getCache().ownFieldExtended(field, k)) { 
				sI.set(field, value); 
				return; 
			}
		}
		
		throw new ModelRuntimeException("Unable to find the field.");
	}
	
	/**
	 * A convenient method for print at screen the object status/contents
	 * @return A string representing the object 
	 */
	public String toString() { 
		String string = "#< " + factory.getCache().tableName(type);
		
				
		if (extendsModels()) {
			string += " inherits (";
			Set<Class> sup = supers.keySet();
			
			boolean first = true;
			for (Class s : sup) {
				string += first ? "" : ", ";
				string +=  " "+ factory.getCache().tableName(s);
				first = false;
			}
			string += ")";
		}
		
		Map<String, Object> objFields = fields();
		Set<String> fieldsName = fields().keySet();
		
		String id = factory.getCache().getModelId(type);
		
		string += " " + id + ": " + objFields.get(id);
		
		for (String fN : fieldsName)
			if (!fN.equals(id) && !fN.equals("UpdatedAt") && 
				!fN.equals("CreatedAt"))
				
				string += ", " + fN + ": " + objFields.get(fN);
		
		string += ", CreatedAt: " + objFields.get("CreatedAt");
		string += ", UpdatedAt: " + objFields.get("UpdatedAt");
		
		return string + ">";
	}
	
	/**
	 * A convenient method for retriving all the fields data of the object.
	 * Actually is not safe but it's used in several parts of the frameworkw 
	 * for efficency issues.
	 * @return A {@link Map} with the objects fields values 
	 */
	public Map<String, Object> fields() { 
		if (!extendsModels()) return fields; 
	
		/* fields from all superclasses */
		Map<String, Object> result = new HashMap<String, Object>();
		result.putAll(fields);
		
		for (Class s : supers.keySet()) result.putAll(supers.get(s).fields());
		
		return result;
	}
	
	
	/**
	 * Grand invokation dispatcher method. Handles all the calls to calls at 
	 * the proxy object and dispatch them to the correct classes.
	 */
	public Object invoke(Object proxy, Method method, Object[] args) {

		/* Business method logic: this should implement it */
		if (method.isAnnotationPresent(BusinessLogic.class)) 
			return execMethod(this, method, args);
			
		/* Getters */
		if (CommonStatic.isAGetter(method)){
		
			String mN = factory.getCache().fieldName(method);
			Class  rT = method.getReturnType();
			
			if (!method.isAnnotationPresent(Connection.class))
				return get(mN, rT);
				
			else {
				Connection c = method.getAnnotation(Connection.class);
				
				if (rT.isArray()) 
					return getConnectionArray(mN, rT.getComponentType(), c);
					
				else return getConnection(mN, rT, c);
			}
		}
		
		/* Setters */
		if (CommonStatic.isASetter(method) && 
			!method.isAnnotationPresent(Connection.class)) {
			
			//TODO: Checks the connections - handle them differently
			String mN = factory.getCache().fieldName(method);		
			
			if (args.length != 1) 
				throw new ModelRuntimeException("Invalid setter arguments " + 
				"length: " + mN);
			
			set(mN, args[0]); return null;
		}
			
		return execMethod(this, method, args);
	}
	
	
	/**
	 * Tries to invoke the method <code>targetMethod</code> on the object 
	 * <code>target</code>
	 * @param target The invokation target
	 * @param target Method The requested method
	 * @param args Method parameters (if needed)
	 * @return The result of the method invokation (if the method return type 
	 * is not void),
	 * <code>null</code> otherwise.
	 * @throws ModelRuntimeException If <code>targetMethod</code> is not 
	 * found in the target object class. 
	 */
	private Object execMethod(Object target, Method targetMethod, 
	Object[] args) {		
	
		boolean found = false;
		
		Method[] all = target.getClass().getMethods();
		Method foundMethod = null;
		
		for (Method m : all)
		
			//checks the name and the return type of the two methods
			if(m.getName().equals(targetMethod.getName()) && 
			   m.getReturnType().equals(targetMethod.getReturnType())) {
				
				found = true;
				
				//checks the parameters (overloading)
				Class[] mP = m.getParameterTypes();
				Class[] tP = targetMethod.getParameterTypes();
				
				if (mP.length == tP.length) {
					//every parameter type
					for (int i = 0; i < mP.length; i++) 
						if (!mP[i].equals(tP[i])) found = false;
				
				} else found = false;
				if (found) { foundMethod = m; break; }
			}
			
		if (!found) 
			throw new ModelRuntimeException("Method " + targetMethod.getName() 
			+ " not found in target " + target.getClass());
			
		Object callResult = null;
		
		try { callResult = foundMethod.invoke(target, args);
		
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
		return callResult;
	}
	
	/**
	 * Returns the actual {@link Class} instance which corresponds to the
	 * original entity interface.  This is necessary because calling 
	 * {@link Object#getClass()}
	 * on a proxy instance doesn't return the value one would expect.  
	 * As such, <code>Model</code> provides this method to give developers 
	 * access to 
	 * the originating entity interface.  Example:</p>
	 * 
	 * <pre>public interface Person extends Model { ... }
	 * // ...
	 * Person p = factory.create(Person.class);
	 * p.modelInterface(); //returns Person.class
	 * p.getClass();       //indeterminate return value, probably something 
	 * like $ProxyN.class</pre>
	 * 
	 * @return The {@link Class} which defines the entity in question.
	 */
	public Class modelInterface() { return type; }
	
	/** 
	 * Returns the factory that instantiated this object
	 * @return A {@link ModelFactory} implementation depending from the 
	 * used datasource
	 * adapter 
	 */
	public ModelFactory modelFactory() { 
		return factory;
	}	
	
	/**
	 * Checks if the object is a new entry
	 * @return <code>true</code> if is a new record, <code>false</code> 
	 * otherwise.
	 */
	public boolean newRecord() { 
		return newRecord; 
	}
	
	/**
	 * Checks if this instance presents some differences between the stored 
	 * one in the datasource
	 * @return <code>true</code> if the entity is not sync with the datasource, 
	 * <code>false</code> otherwise
	 */
	public boolean dirty() { 
		return dirty;
	}
	
	/**
	 * Checks if the {@link Model} extends any other models.
	 * Multiple inheritance is permitted in interfaces, and so do ModelMapper 
	 * with models.
	 * @return <code>true</code> if this extends any other Model, 
	 * <code>false</code> otherwise
	 */
	public boolean extendsModels() {
		 return supers.keySet().size() != 0; 
	}
	
	
	public ModelProxy modelImplementation() { return this.implementation; }
	

	public java.sql.Timestamp updatedAt() { 
		return get("UpdatedAt", java.sql.Timestamp.class); 
	}
	
	public java.sql.Timestamp createdAt() {
		return get("createdAt", java.sql.Timestamp.class);
	}
	
	
	public boolean equalsId(Model o) {
		
		if (o == null) return false;
		
		//if (!o.modelImplementation().equals(type)) return false;
		
		String id = factory.getCache().getModelId(type);
		Class idType = factory.getCache().getModelIdType(type);
		if (idType == Integer.TYPE) {
			int idVal = (Integer) fields().get(id);
			int idObj = (Integer) o.fields().get(id);
			
			if (idVal == idObj) return true;
			else return false;
		}
		
		if (idType == String.class) {
			String idVal = (String) fields().get(id);
			String idObj = (String) o.fields().get(id);
			if (idVal.equals(idObj)) return true;
			else return false;
		}
		
		throw new ModelRuntimeException("Unattended");
	}
	
	public boolean compareId(Object o) {
		
		if (o == null) return false;
		
		//if (!o.modelImplementation().equals(type)) return false;
		
		String id = factory.getCache().getModelId(type);
		Class idType = factory.getCache().getModelIdType(type);
		
		if (!o.getClass().equals(idType)) return false;
		
		if (idType == Integer.TYPE) {
			int idVal = (Integer) fields().get(id);
			int idObj = (Integer) o;
			
			if (idVal == idObj) return true;
			else return false;
		}
		
		if (idType == String.class) {
			String idVal = (String) fields().get(id);
			String idObj = (String) o;
			if (idVal.equals(idObj)) return true;
			else return false;
		}
		
		throw new ModelRuntimeException("Unattended");
	}
	
	/** 
	 * Returns an <code>XML</code> representation of the current 
	 * structure of this object.
	 * @return The XML representation of <code>this</code>
	 */
	public String toXml() {
		
		String result = "<" + factory.getCache().tableName(type) + ">";
		
		Map<String, Object> modelFields = fields();
		Set<String> keySet = modelFields.keySet();
		
		String cA = "CreatedAt";
		String uA = "UpdatedAt";
		
		for (String k : keySet)
			
		if (!k.equals(cA) && !k.equals(uA))
		result += "\n<" + k + ">" + modelFields.get(k) + "</" + k + ">";
			
		Set<String> fetchedKeySet = fetched.keySet();
		
		for (String k : fetchedKeySet) {
			
			String innerResult = "";
			for (Model m : fetched.get(k)) innerResult += "\n"+m.toXml();
			
			if (factory.getCache().isAnArrayConnection(type, k))
				result += "\n<" + k + ">" + innerResult + "\n</" + k + ">";
				
			else result += "\n" + innerResult;
		}
			
		result += "\n</" + factory.getCache().tableName(type) + ">";
		
		return result;
	}
	

	public String toJson() {
		return "{\"" + factory.getCache().tableName(type) +"\":"+ json() +"}";
	}
	
	private String json() {
		
		String result = " {";
		
		Map<String, Object> modelFields = fields();
		Set<String> keySet = modelFields.keySet();
		
		for (String k : keySet)
			result += "\"" + k + "\":" + 
					  CommonStatic.JSONFormatValue(modelFields.get(k)) + ",";
			
		Set<String> fetchedKeySet = fetched.keySet();
		
		for (String k : fetchedKeySet) {
		
			String innerResult = "";
			for (Model m : fetched.get(k))
				innerResult += m.modelImplementation().json() + ",";
				
			//innerResult = innerResult.substring(0, innerResult.length()-1);

			/* If is an 1..N, M..N relationship */
			if (factory.getCache().isAnArrayConnection(type, k))
				result += "\""+k+"\": [" + innerResult + " ],";

			else result += "\""+k+"\":" + innerResult + ",";
		}
			
		result = result.substring(0, result.length()-1);
		result += "}";
		
		return result;
	}
	
}
