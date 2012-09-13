import modelmapper.*;
import modelmapper.annotation.*;

import java.util.*;

public interface Person extends Model {
	
	public String getFirstName();
	public void setFirstName(String name);
	
	public String getLastName();
	public void setLastName(String name);
	
	public int getAge();
	public void setAge(int age);
	
	@Connection(name = "Employed")
	public Corporation getEmployer();
	public void setEmployer(Corporation c);
	
	@Connection(name = "CEO")
	public Corporation getManagedCorporation();
	public void setManagedCorporation(Corporation c);
}