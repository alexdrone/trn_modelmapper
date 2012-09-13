import modelmapper.*;
import modelmapper.annotation.*;

import java.util.*;

public interface WebService extends Model {
	
	@Id
	public String getName();
	public void setName(String name);
	
	public String getHostName();
	public void setHostName(String hostName);

	@Connection(name = "AS", type = ConnectionType.Composition)
	public Account[] getAccounts();
	public void setAccounts(Account[] a);

}