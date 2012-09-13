// 
//  RDBMSSchema
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper.schema;

import modelmapper.*;

import modelmapper.schema.*;
import modelmapper.exception.*;
import modelmapper.annotation.*;

import java.util.*;

public interface RDBMSSchemaGenerator {
	

	public String createDDL(String tableName, 
	Collection<Attribute> attributes, String idName);
	
	public String createConstraints(String tableName, 
	Collection<Attribute> attributes, String idName);
	
	public String createJoinTable(Relationship r);
	
	public String getDefaultMappedType(Class type);
}