// 
//  Attribute
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper.schema;

/**
 * Contatins the informations about an attribute. It's pubblic only for 
 * internal issues.Don't use this class, it's used as a data container in the 
 * schema generation classes
 * @author Alex Usbergo, Luca Querella
 * @version alpha
 */
public class Attribute {
	
	public Class type;
	public String name, sqlType, foreignTable, foreignTableId;
	
	public boolean 	isId, isAutoIncrement, isNotNull, 
					isUnique, isAForeignKey, composition;
	
	public Attribute(String name){ 
		
		this.name = name; 
		
		/* booleans initialization */
		isId = isAutoIncrement = isNotNull = 
		isUnique = isAForeignKey = composition = false;
	}
	


}