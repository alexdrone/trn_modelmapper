// 
//  Validator
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
import java.util.*;
import java.lang.annotation.*;

/*
 * TODO: Validate inheritance
 */

public class Validator {

	public static void main(String[] args) {
			
		
		for (String c : args) {
			Class backClass, type; type = backClass = null;
			
			/* search the class for that name... */
			try { type = Class.forName(c); 
			} catch (ClassNotFoundException e) { System.out.println("Class " + c +" doesn't exist." ); }

			try { 
				Package pkg =  type.getPackage();
				String pkgN = pkg == null ? "" : pkg.getName();
								
				backClass = Class.forName(pkgN + tableName(type) + 
							CommonStatic.getImplementationSuffix());
							
			} catch(Exception e) { backClass = ModelProxy.class; }
			
			try { validateModel(type, backClass);
			
			} catch (MalformedModelException e) { 
				String title = c + "\n"; for (int i = 0; i < c.length(); i++) title += "-"; title += "\n";
				System.out.println(title + e.getMessage()); 
			}
		}
	}


	/**
	 * Validates the model interface and the (optional) given implementation.
	 */
	
	protected static void validateModel(Class modelInterface, Class modelImplementation) {
		
		Method[] iMethods = modelInterface.getDeclaredMethods();
		Method[] cMethods = modelImplementation.getDeclaredMethods();
		
		/* the business logic methods */
		List<Method> bMethods = new ArrayList<Method>();
		
		/* getter/setter methods */
		Map<String,Method> gMethods = new HashMap<String,Method>();
		Map<String,Method> sMethods = new HashMap<String,Method>();
		
		/* the exception build up in the validation */
		MalformedModelException exc = new MalformedModelException(modelInterface, modelImplementation);
	
		/* Firstly, let's validate the interface */
		for (Method m : iMethods) {
			String mN = m.getName();
			
			//setters
			if (mN.startsWith("set")) {
								
				if(!sMethods.containsKey(mN)) {
					List<String> errors = validateSetter(m);
					if (errors.isEmpty()) sMethods.put(mN.substring(3, mN.length()), m);
					else exc.interfaceErr(errors,m);
				} else
					exc.interfaceErr("duplicate setter",m);
				
				
			//getters
			} else if (mN.startsWith("get") || mN.startsWith("is")) {
			
				if(!gMethods.containsKey(mN)) {
					List<String> errors = validateGetter(m);
					int x = m.getReturnType().equals(Boolean.TYPE) ? 2 : 3;
					if (errors.isEmpty()) gMethods.put(mN.substring(x, mN.length()), m);
					else exc.interfaceErr(errors,m);
				} else
					exc.interfaceErr("duplicate getter",m);
		
			//reserved
			} else if (mN.equalsIgnoreCase("save") || mN.equalsIgnoreCase("delete") || 
					   mN.equalsIgnoreCase("find") || mN.equalsIgnoreCase("validate") ||
			           mN.equalsIgnoreCase("migrate")) { exc.interfaceErr("reserved method name",m);
		
			} else { 
				if (!m.isAnnotationPresent(BusinessLogic.class))
					exc.interfaceErr("has to be marked with @BusinessLogic",m);
					
				bMethods.add(m);
			}
		}

		//business methods implementation needed
		int count = 0;
		for (Method b : bMethods) {
			
			Method matching = null; boolean found = false;
			
			for (Method c : cMethods)
				if (c.getName().equals(b.getName()) && c.getReturnType().equals(b.getReturnType())) {
					found = true;
					
					//checks all the parameters
					Class[] pC = c.getParameterTypes();
					Class[] pB = b.getParameterTypes();
					
					if (pC.length != pB.length)	
						found = false;
					
					for (int i = 0; found && i < pC.length; i++)
							if (!pC[i].equals(pB[i])) found = false;
				}
				
			if (!found)	exc.implementationErr("missing BusinessLogic implementation: ", b); 
			else count++;
		}

		if (!modelImplementation.equals(ModelProxy.class) && count != cMethods.length) 
			exc.implementationErr("some methods are not declared on interface"); 
		
		//getter and setter again
		for (String getterName : gMethods.keySet()) {
			
			Method getter = gMethods.get(getterName);
			
			if (!getter.isAnnotationPresent(Connection.class) && 
				!isAValidFieldTypeForAPrimitive(getter.getReturnType()))
					exc.interfaceErr("missing @Connection " ,getter);

			if (getter.isAnnotationPresent(Connection.class) && 
				isAValidFieldTypeForAPrimitive(getter.getReturnType()))
					exc.interfaceErr("this getter is not a @Connection" ,getter);
			
			if (getter.isAnnotationPresent(Connection.class)) {
				Connection a = getter.getAnnotation(Connection.class);
				if (a.type().equals(ConnectionType.ManyToMany) && !getter.getReturnType().isArray())
					exc.interfaceErr("ManyToMany @Connection getter has to reuturn an Array" ,getter);
					
				if (a.type().equals(ConnectionType.BelongsTo) && getter.getReturnType().isArray())
					exc.interfaceErr("BelongsTo @Connection getter has not to reuturn an Array" ,getter);
					
					
				int found = 0;
				List<Connection> candidates = getPossibleConnection(getter.getReturnType());
				for (Connection candidate: candidates) {
					if ((a != candidate) && isACompatibleConnection(a,candidate))
						found++;
				}
				
				if (found == 0)
					exc.interfaceErr("Can't find brother @Connection" ,getter);
				
				if (found > 1)
					exc.interfaceErr("Founded duplicated @Connection" ,getter);
			}
			
			if (sMethods.containsKey(getterName)) {
				
				Method setter = sMethods.get(getterName);
				
				if (!isACompatibleSetter(getter,setter))
					exc.interfaceErr("incompatible setter for "+ getterName ,setter);
				
				sMethods.remove(getterName);
				
			} else exc.interfaceErr("missing setter",gMethods.get(getterName));
		}

		for (String setterName : sMethods.keySet())
				exc.interfaceErr("missing getter",sMethods.get(setterName));

		if (!exc.isEmpty()) throw exc;
	}

	protected static void validateSearcher(Class searcher, Class model) {
	/*	Method[] methods = searcher.getDeclaredMethods();
		
		MalformedSearcherException exc = new MalformedSearcherException(searcher, model);
		
		
		for(Method m: methods) {
				String mN = m.getName();
			
				if (mN.startsWith("by")) { 
					
					if (!m.getReturnType().equals(searcher)) exc.searcherErr("method must return a "+ searcher.getName());
					//controlli sui tipi accettati
					//controllare l'esistenza del campo
					
				} else exc.searcherErr("unknow method");
				
		}
	*/	
	}

	private static List<String> validateSetter(Method m) {
		List<String> errors = new ArrayList<String>();
		
		if (m.isAnnotationPresent(BusinessLogic.class)) errors.add("@BusinessLogic is not valid here");
		if (m.isAnnotationPresent(Xml.class)) errors.add("@Xml is not valid here, use it on getter");
		if (m.isAnnotationPresent(Connection.class)) errors.add("@Connection is not valid here, use it on getter");
		if (!m.getReturnType().equals(Void.TYPE)) errors.add("setter must return void");
		if (m.getParameterTypes().length != 1) errors.add("only 1 param permitted on setter");
		if (!isAValidFieldType(m.getParameterTypes()[0])) errors.add("invalid param type");
		
		return errors;
	}

	private static List<String> validateGetter(Method m) {
		List<String> errors = new ArrayList<String>();
		
		if (m.isAnnotationPresent(BusinessLogic.class)) errors.add("@BusinessLogic is not valid here");
		if (!isAValidFieldType(m.getReturnType())) errors.add("invalid return type");
		if (m.getParameterTypes().length != 0 ) errors.add("no params allowed on getter");
		if (m.getName().startsWith("is") &&  !m.getReturnType().equals(Boolean.TYPE)) 
			errors.add("non-boolean getters must starts with get");
		if (m.getName().startsWith("get") &&  m.getReturnType().equals(Boolean.TYPE)) 
			errors.add("boolean getter must starts with is");
		
		return errors;
	}

	private static boolean isAValidFieldType(Class type) {
		
		if (Model.class.isAssignableFrom(type)) return true;
		else if (type.isArray()) {
			Model[] mArray = {};
			if (mArray.getClass().isAssignableFrom(type)) return true; 
		}
			
		return isAValidFieldTypeForAPrimitive(type);
	}
	
	protected static boolean isAValidFieldTypeForAPrimitive(Class type) {
		
		if (type.equals(Void.TYPE)) return false;
		if (type.isPrimitive()) return true;
		
		Class[] alloweds = { java.sql.Time.class, java.sql.Timestamp.class, java.util.Date.class, 
		String.class, java.sql.Clob.class, java.sql.Blob.class, java.math.BigDecimal.class, java.net.URL.class};
		
		boolean found = false;
		for(Class allowed : alloweds )
			if (type.equals(allowed)) found = true;
			
		return found;
	}

	private static boolean isACompatibleSetter(Method getter, Method setter) {
			return setter.getParameterTypes()[0].equals(getter.getReturnType());
	}
	
	private static boolean isACompatibleConnection(Connection connection, Connection brother) {
		
		//first of all, same name
		if(!connection.name().equals(brother.name())) return false;
		
		//then if MtM -> MtM, Aggregation\Composition -> Bt and viceversa
		if(connection.type().equals(ConnectionType.ManyToMany) 
			&& !brother.type().equals(ConnectionType.ManyToMany))
			return false;
		
		if(connection.type().equals(ConnectionType.Aggregation) 
			&& !brother.type().equals(ConnectionType.BelongsTo))
			return false;
			
		if(connection.type().equals(ConnectionType.Composition) 
			&& !brother.type().equals(ConnectionType.BelongsTo))
			return false;
			
		if(connection.type().equals(ConnectionType.BelongsTo)
		 	&& !(brother.type().equals(ConnectionType.Aggregation) 
			|| brother.type().equals(ConnectionType.Composition)))
			return false;
					
		return true;
	}
	
	private static List<Connection>  getPossibleConnection(Class type) {
		if (type.isArray()) type = type.getComponentType();
		
		List<Connection> connections = new ArrayList<Connection>();
				
		for (Method m : type.getDeclaredMethods())
			if(m.isAnnotationPresent(Connection.class)) {
				
				connections.add(m.getAnnotation(Connection.class));
		}
				
		return connections;
	}
	
	/*
	 * Returns the conventional table name for a {@link Model} interface.
	 * @param type A valid Model interface.
	 * @return The conventional table name for the given interface.
	 */ 
	private static String tableName(Class type) {
				
		String className = type.getName();
		String classPckg = null;
		
		Package pckg = type.getPackage();
		classPckg = pckg == null ? null : pckg.getName();
		
		String tN; 
		if (classPckg == null) tN = type.toString();
		else tN = className.substring(classPckg.length() + 1, className.length()); 
		
		tN = tN.substring(10, tN.length());
		
		return tN;
	}
}