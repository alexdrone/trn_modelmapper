import modelmapper.*;
import modelmapper.annotation.*;

import java.util.*;

public interface Corporation extends Model {
	
	@Id
	@AutoIncrement
	public int getCorporateId();
	public void setCorporateId(int id);
	
	public String getName();
	public void setName(String name);
	
	@Connection(name = "CEO", type = ConnectionType.Aggregation)
	public Person getCEO();
	public void setCEO(Person founder);
	
	@Connection(name = "Employed", type = ConnectionType.Composition)
	public Person[] getEmployees();
	public void setEmployees(Person[] s);
	
	@Connection(name = "Stage", type = ConnectionType.ManyToMany)
	public Student[] getStageStudents();
	public void setStageStudents(Student[] s);

}