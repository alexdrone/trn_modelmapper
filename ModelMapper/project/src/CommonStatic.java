// 
//  Common static methods
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
 * A set of common static methods often used by the entire framework.
 * Many of them are wrapped in the Cache class, that dinamically saves 
 * computated results for an enviroment in order to achieve higher 
 * performances 
 * @autor Alex Usbergo, Luca Querella
 */
public class CommonStatic {
	
	private static String EXC_NOTAMODEL = "You can invoke this method only " +
	"on a Model subinterface.";
		
	/**
	 * Returns the conventional field name for a getter method.
	 * @param m The field getter.
	 * @return The field name.
	 * @throws IllegalArgumentException if the method isn't a valid getter.
	 */
	protected static String fieldName(Method m) {
					
		String name = m.getName();
		
		name = name.startsWith("is") ? 
		name.substring(2, name.length()) : name.substring(3, name.length());
		
		return name;
	}
	
	/**
	 * Returns the conventional table name for a {@link Model} interface.
	 * @param type A valid Model interface.
	 * @return The conventional table name for the given interface.
	 */
	protected static String tableName(Class type) {
		if (type.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
						
		String className = type.getName();
		String classPckg = null;
		
		Package pckg = type.getPackage();
		classPckg = pckg == null ? null : pckg.getName();
		
		String tN; 
		if (classPckg == null) tN = type.getName();
		
		else tN = 
			className.substring(classPckg.length() + 1, className.length()); 
		
		
		return tN;
	}
	
	/**
	 * Checks if a method is a conventional getter for a {@link Model}
	 * @param m A model method.
	 * @return <code>true</code> if is a field getter, <code>false</code> 
	 * otherwise.
	 */
	protected static boolean isAGetter(Method m) {
		if (m.getName().startsWith("get") ||
			m.getName().startsWith("is")) return true;
			
		return false;
	}
	
	/**
	 * Checks if a method is a conventional setter for a {@link Model}
	 * @param m A model method.
	 * @return <code>true</code> if is a field setter, <code>false</code> 
	 * otherwise.
	 */
	protected static boolean isASetter(Method m) {
		if (m.getName().startsWith("set")) return true;
		return false;
	}
	
	/**
	 * Returns all the getter methods that belongs to a specific model.
	 * @param type The given model.
	 * @return An array of getters methods.
	 * @throws MalformedModelRuntimeException If the model extends multiple 
	 * models and don't define ad Id - This is made in order to avoid multiple 
	 * keys references in the schema.
	 */
	protected static Method[] getGetters(Class type) {
		if (type.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		List<Method> getters = new ArrayList<Method>();
		Method[] all = type.getDeclaredMethods();
		
		boolean idDefined = false;
		for (Method m : all) 
			if (CommonStatic.isAGetter(m)) {
				getters.add(m);
				if (m.isAnnotationPresent(Id.class)) idDefined = true;
			}
			
		if (getSupers(type).size() > 1 && !idDefined) 
			throw new MalformedModelRuntimeException("You have to define an" +
			" Id for multiple inheritance.");
	
		if (!idDefined && !extendsModels(type)) {
			all = type.getMethods();		
			for (Method m : all) 
				if(m.getName().equals("getId")) getters.add(m);	
		}
		
		return 
			getters.toArray((Method[]) 
			java.lang.reflect.Array.newInstance(Method.class, getters.size()));
	}
	
	/**
	 * Returns all the getter methods that belongs to a specific model.
	 * The difference from <code>getGetters</code> is that this method 
	 * retrieve only the declared methods - So no default Id getter.
	 * @param type The given model.
	 * @return An array of getters methods.
	 * @throws MalformedModelRuntimeException If the model extends multiple 
	 * models and don't define ad Id - This is made in order to avoid multiple 
	 * keys references in the schema.
	 */
	protected static Method[] getDeclaredGetters(Class type) {
		if (type.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		List<Method> getters = new ArrayList<Method>();
		Method[] all = type.getDeclaredMethods();
		
		for (Method m : all) 
			if (CommonStatic.isAGetter(m)) getters.add(m);

		return 
			getters.toArray((Method[]) 
			java.lang.reflect.Array.newInstance(Method.class, getters.size()));
	}
	
	/**
	 * Returns ALL the getters, also the inherited ones.
	 * @param type The given model.
	 * @return An array of getters methods.
	 */
	protected static Method[] getAllGetters(Class type) {
		if (type.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		List<Method> getters = new ArrayList<Method>();
		Method[] all = type.getMethods();
		
		boolean idDefined = false;
		for (Method m : all) 
			if (CommonStatic.isAGetter(m) && !m.getName().equals("getId")) {
				getters.add(m);
				if (m.isAnnotationPresent(Id.class)) idDefined = true;
			}
			
		if (getSupers(type).size() > 1 && !idDefined) 
			throw new MalformedModelRuntimeException("You have to define an" +
			" Id for multiple inheritance.");
				
		if (!idDefined && !extendsModels(type)) {
			all = type.getMethods();		
			for (Method m : all) 
				if(m.getName().equals("getId")) getters.add(m);	
		}
		
		return 
			getters.toArray((Method[]) 
			java.lang.reflect.Array.newInstance(Method.class, getters.size()));
	}
	
	/**
	 * Checks if the passed type is a legal allowed primitive type for the 
	 * framwork. The alloweds type are just a few at the moment due to the 
	 * prototypal nature of this framework.
	 * @param type The given type.
	 * @return <code>true</code> if the given field is a valid primitiva,
	 * <code>false</code> otherwise.
	 */
	protected static boolean validPrimitiveField(Class type) {
		
		if (type.equals(Void.TYPE)) return false;
		if (type.isPrimitive()) return true;
		
		Class[] alloweds = { 
			java.sql.Time.class,	
			java.sql.Timestamp.class, 
			java.util.Date.class,	
			
			String.class, 
			java.sql.Clob.class, 
			java.sql.Blob.class, 	
			
			java.math.BigDecimal.class, 
			java.net.URL.class
			
			/*TODO: Add more types like byte[] */
		};
		
		boolean found = false;
		for (Class allowed : alloweds) if (type.equals(allowed)) found = true;
			
		return found;
	}
	
	/**
	 * Checks if a field type is a {@link Model} - 
	 * A connection between other fields.
	 */
	protected static boolean isAModelType(Class model) { 
		if (model.isArray()) model = model.getComponentType();
		return model.isAssignableFrom(Model.class); 
	}

	/**
	 * Returns the default conventional implementation suffix for a model.
	 * In this version of the framework the suffix is <code>Impl</code>, 
	 * so for having your custom business logic implementations class for your 
	 * <code>Person</code> model, you just need
	 * to implement the methods marked as {@link BusinessLogic} in a class 
	 * called <code>PersonImpl</code> in the same package of 
	 * <code>Person</code>. <p><code>TODO</code> Allow to define a different 
	 * implementation name via annotations.</p>
	 */
 	protected static String getImplementationSuffix() { 
		return "Impl"; 
	}
	
	/**
	 * Checks if the given method is a valid field getter for 
	 * a {@link Model} interface.
	 */
	protected static boolean isFieldGetter(Method m, String field, Class rt) {
		if (m.getName().equals("get"+field) && 
			m.getReturnType().equals(rt)) return true;
			
		return false;
	}
	
	/**
	 * Checks if the Model <code>type</code> is inherits from other models
	 * @return <code>true</code> if extends other models, <code>false</code> 
	 * otherwise
	 */
	protected static boolean extendsModels(Class type) { 
		if (type.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		return getSupers(type).size() != 0; 
	}
	
	/** Dummy primitive assignement workaroud  */
	protected static boolean validPrimitiveAssignament(Class assigned, 
	Class value) {
	
		if (assigned.equals(Double.TYPE) || assigned.equals(Float.TYPE))
			if (value.equals(Integer.TYPE) || value.equals(Float.TYPE)) 
				return true;
		
		return false;
	}
	
	/**
	 * Generates a list of Model interfaces that are supers (all) of 
	 * <code>type</code>
	 * @param type The given {@link Model}.
	 * @return A list of super interfaces.
	 */
	protected static List<Class> getAllSupers(Class type) {
		if (type.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		List<Class> supers = getSupers(type);
		List<Class> back = new ArrayList<Class>();
		back.addAll(supers);
		
		for (Class s : supers)
			if (Model.class.isAssignableFrom(s)) 
				back.addAll(getAllSupers(s));
		
		return back;
	}
	
	protected static Connection getConnection(Class model, String cName) {
		if (model.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		Method[] methods = model.getMethods();
		
		for (Method m : methods) {
			if (m.isAnnotationPresent(Connection.class) &&
				m.getAnnotation(Connection.class).name().equals(cName))
					
				return m.getAnnotation(Connection.class);
		}
		
		return null;
	}
	
	/**
	 * Search a {@link Connection} from a given field of a {@link Model}
	 * interface 
	 */
	protected static Connection searchConnection(Class model, String field) {
		
		if (model.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		Method[] getters = CommonStatic.getAllGetters(model);
		
		for (Method m : getters)
			if (m.getName().equalsIgnoreCase("get"+field) &&
				m.isAnnotationPresent(Connection.class)) {
				return m.getAnnotation(Connection.class);
			}
			
		throw new ModelRuntimeException("Invalid field for a connection.");
	}
	
	/**
	 * Search a {@link Connection} from a given field of a {@link Model}
	 * interface 
	 */
	protected static Class getConnectionFieldType(Class model, String field) {
		
		if (model.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		Method[] getters = CommonStatic.getAllGetters(model);
		
		for (Method m : getters)
			if (m.getName().equalsIgnoreCase("get"+field) &&
				m.isAnnotationPresent(Connection.class)) {
				Class t = m.getReturnType();
				if (t.isArray()) t = t.getComponentType();
				return t;
			}
			
		throw new ModelRuntimeException("Invalid field for a connection.");
	}
	
	/**
	 * Search a {@link Connection} from a given field of a {@link Model}
	 * interface 
	 */
	protected static boolean isAnArrayConnection(Class model, String field) {
		
		if (model.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		Method[] getters = CommonStatic.getAllGetters(model);
		
		for (Method m : getters)
			if (m.getName().equalsIgnoreCase("get"+field) &&
				m.isAnnotationPresent(Connection.class)) {
				Class t = m.getReturnType();
				if (t.isArray()) return true;
				return false;
			}
			
		throw new ModelRuntimeException("Invalid field for a connection.");
	}
	
	/**
	 * Generates a list of Model interfaces that are supers (directly) of 
	 * <code>type</code>
	 * @param type The given {@link Model}.
	 * @return A list of super interfaces.
	 */
	protected static List<Class> getSupers(Class type) {
		if (type.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
		
		Class[] interfaces = type.getInterfaces();
		List<Class> sC = new ArrayList<Class>();
		boolean subclass = true;

		for (Class i : interfaces) 
			if (i.equals(Model.class)) subclass = false;

		if (subclass)
			for (Class i : interfaces) {
				if (Model.class.isAssignableFrom(i) && 
					!i.equals(type)) sC.add(i);
			}
						
		return sC;
	}
		
	/**
	 * This methods checks if a given {@link Connection} is recursive.
	 * Actually recursive connections are not implemented. 
	 */
	protected static boolean isRecursive(Class type, String connectionName) {
		if (type.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
					
		Method[] getters = getDeclaredGetters(type);
		
		for (Method m : getters)
			if (m.isAnnotationPresent(Connection.class)) {
				Connection c = m.getAnnotation(Connection.class);
			
				Class returnType = m.getReturnType();
				if (returnType.isArray()) 
					returnType = returnType.getComponentType();
				
				/* connection found */
				if (c.name().equals(connectionName) && 
					returnType.equals(type)) return true;
			}
			
		return false;
	}
	
	/**
	 * Checks if the Id of the given model is marked as an autoincremental 
	 * identifier.
	 * @return <code>true</code> if the {@AutoIncrement} annotation is present, 
	 * <code>false</code>
	 * otherwise.
	 */
	protected static boolean isAutoIncrementPresent(Class type) {
		if (type.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		Method[] getters = getDeclaredGetters(type);
		
		for (Method m : getters) 
			if (m.isAnnotationPresent(Id.class) && 
				m.isAnnotationPresent(AutoIncrement.class)) {
				
				return true;
			}
		
		if (getModelId(type).equals("Id")) return true;
				
		return false;
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
	public static String getModelId(Class model) {
		if (model.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		Method[] all = model.getDeclaredMethods();
		
		String id = "";
		boolean found = false;
		for (Method m : all)
		
			/*TODO: make conventions ind. */
			if (m.isAnnotationPresent(Id.class)) {
				found = true;
				id = m.getName().substring(3, m.getName().length());
			}
			
		if (!found) {
			List<Class> supers = CommonStatic.getSupers(model);
			
			if (supers.size() > 1) 
			throw new MalformedModelRuntimeException("You have to define an" +
			" Id for multiple inheritance.");
								
			else if (supers.size() == 1) 
				id = CommonStatic.tableName(
					 supers.get(0)) + getModelId(supers.get(0));
					
			else id = "Id"; /* no inheritance */
		}
								
		return id;
	}
	
	/**
	 * Returns the Id type for a Model 
	 * @param type The given interface.
	 * @return The Id return type.
	 */
	public static Class getModelIdType(Class model) { 
		if (model.isAssignableFrom(Model.class))
			throw new IllegalArgumentException(EXC_NOTAMODEL);
			
		Method[] declareds = CommonStatic.getDeclaredGetters(model);
		
		Class returnType = null;
		boolean found = false;
		
		for (Method m : declareds) 
			if (m.isAnnotationPresent(Id.class)) {
				found = true;
				returnType = m.getReturnType();
			}
		
		if (!found) {
			List<Class> supers = CommonStatic.getSupers(model);

			if (supers.size() > 1) 
			throw new MalformedModelRuntimeException("You have to define an" +
			" Id for multiple inheritance.");
			
			else if (supers.size() == 1) 
				returnType = getModelIdType(supers.get(0));
				
			else returnType = Integer.TYPE; /* no inheritance */
		}
				
		return returnType;
	}
	
	public static String JSONFormatValue(Object o) {
		Class oC = o.getClass();
		
		if (oC.equals(Integer.TYPE)	|| oC.equals(Double.TYPE) 	|| 
			oC.equals(Float.TYPE) 	|| oC.equals(Boolean.TYPE)	|| 
			oC.equals(Byte.TYPE) 	|| oC.equals(Short.TYPE)	||
			oC.equals(Long.TYPE) 	|| oC.equals(Long.class)	||
			oC.equals(Float.class)	|| oC.equals(Byte.class)	|| 
			oC.equals(Double.class)	|| oC.equals(Integer.class)	||
			oC.equals(Boolean.class)|| oC.equals(java.math.BigDecimal.class))
			
			return o.toString();
			
		if (oC.equals(java.util.Date.class) || 
			oC.equals(java.sql.Timestamp.class) ||
			oC.equals(java.sql.Time.class)) 
			
			return "" + ((java.util.Date)o).getTime();
			
		return "\"" + o.toString() + "\"";
	}
	
	
 	
}