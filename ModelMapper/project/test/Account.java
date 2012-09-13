import modelmapper.*;
import modelmapper.annotation.*;

import java.util.*;

public interface Account extends Model {

	@Id
	public String getEmail();
	public void setEmail(String email);
	
	public String getPassword();
	public void setPassword(String password);
	
	@Connection(name = "AS")
	public WebService getWebService();
	public void setWebService(WebService service);
	
	
}