// 
//  MalformedSearcher Exception
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
public class MalformedSearcherException extends MalformedClassException { 
	
	private Class searcher;
	private List<String> errors = new ArrayList<String>();
	private boolean isEmpty = true;
	
	public MalformedSearcherException(Class searcher) {
		this.searcher = searcher;
	}
	
	public void searcherErr(String messages) {

		errors.add(messages);
		isEmpty = false;
	} 

	
	public String getMessage() {

		
		String message = "Malformed Searcher\n";
		
		for(String error: errors)
				message += "\n" + error;

		return message;
	}
	
	protected boolean isEmpty() { return isEmpty; }
}