import modelmapper.*;
import modelmapper.annotation.*;

import java.util.*;

public interface Student extends Person, Account {

	@Id
	@AutoIncrement
	public int getStudentId();
	public void setStudentId(int id);

	public double getAvg();
	public void setAvg(double avg);	
	
	public String getDepartment();
	public void setDepartment(String d);

	@Connection(name = "Stage", type = ConnectionType.ManyToMany)
	public Corporation[] getCorporations();
	public void setCorporations(Corporation[] c);
}