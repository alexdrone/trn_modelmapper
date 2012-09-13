// 
//  MalformedModel Exception
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper.exception;

import modelmapper.*;

import java.lang.*;
import java.lang.reflect.*;
import java.util.*;


@SuppressWarnings("serial")
public class MalformedModelException extends MalformedClassException { 
	
	private Class modelInterface;
	private Class modelImplementation;
	
	private Map<Method, List<String>> 
		implementationErrors = new HashMap<Method, List<String>>();
		
	private Map<Method, List<String>> 
		interfaceErrors = new HashMap<Method, List<String>>();
		
	private List<String> genericImplementationErrors = new ArrayList<String>();
	private List<String> genericInterfaceErrors = new ArrayList<String>();
	
	private boolean isEmpty = true;
	
	public MalformedModelException(Class interf, Class implementation) {
		this.modelInterface = interf;
		this.modelImplementation = implementation; 
	}
	
	public void implementationErr(List<String> messages,Method method) {

		if(!implementationErrors.containsKey(method))
			implementationErrors.put(method,messages);
		else {
			List<String> list = implementationErrors.get(method);
			list.addAll(messages);
			implementationErrors.put(method,list);
		}
			
		isEmpty = false;
	} 
	
	public void interfaceErr(List<String> messages, Method method) {
		
		if(!interfaceErrors.containsKey(method))
			interfaceErrors.put(method,messages);
		else {
			List<String> list = interfaceErrors.get(method);
			list.addAll(messages);
			interfaceErrors.put(method,list);
		}
			
		isEmpty = false;
	}
	
	public void implementationErr(String message,Method method) {
		List<String> list = new ArrayList<String>();
		list.add(message);
		implementationErr(list,method);
	} 
	
	public void interfaceErr(String message, Method method) {
		List<String> list = new ArrayList<String>();
		list.add(message);
		interfaceErr(list,method);
	}
	
	public void implementationErr(String message) {
		genericImplementationErrors.add(message);
		isEmpty = false;
	} 
	
	public void interfaceErr(String message) {
		genericInterfaceErrors.add(message);
		isEmpty = false;
	}
	
	public String getMessage() {

		
		String message = "Malformed model interface/implementation\n";
		
		if (!genericInterfaceErrors.isEmpty() || !interfaceErrors.isEmpty()) {
			
			int totalError = 
				genericInterfaceErrors.size() + interfaceErrors.size();
				
			message  += "\nIn interface ("+totalError+" error(s))";
			
			for(String error: genericInterfaceErrors)
				message += "\n" + error ;
			
			for(Method method: interfaceErrors.keySet())
				for(String error : interfaceErrors.get(method)) 
					message += "\n" + method + ": " + error; 
		}
		
		
		if (!genericImplementationErrors.isEmpty() || 
			!implementationErrors.isEmpty()) {
				
			int totalError = 
				genericImplementationErrors.size() + 
				implementationErrors.size();
				
			message  += "\n\nIn implementation ("+totalError+" error(s))";
			
			
			for(String error: genericImplementationErrors)
				message += "\n" + error;
			
			for(Method method: implementationErrors.keySet())
				for(String error : implementationErrors.get(method)) 
					message += "\n" + method + ": " + error; 
		
		}

		return message;
	}
	
	public boolean isEmpty() { return isEmpty; }
}