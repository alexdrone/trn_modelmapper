// 
//  Relationship
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper.schema;

import modelmapper.annotation.*;

/**
 * Contatins the informations about a relationship. It's pubblic only for 
 * internal issues.Don't use this class, it's used as a data container in the 
 * schema generation classes
 * @author Alex Usbergo, Luca Querella
 * @version alpha
 */
public class Relationship {
	
	public String name; 
	public ConnectionType type; 
	public String tableA, tableB, fieldA, fieldB;
	public Class classA, classB;
	
	public Relationship(String name, ConnectionType type) {
		this.name = name; this.type = type;
	}
	
	public String toString() {
		return "\nname = " + name +
		"\ntype = " + type +
		"\ntableA = " + tableA +
		"\nclassA = " + classA +
		"\nfieldA = " + fieldA + 
		"\ntableB = " + tableB +
		"\nclassB = " + classB +
		"\nfieldB = " + fieldB;
	}
		
	public Class getConnectedModel(String field) {
		if (field.equals(fieldA)) return classB;
		else return classA;
	}
}