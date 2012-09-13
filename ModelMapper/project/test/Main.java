import modelmapper.*;
import modelmapper.schema.*;
import modelmapper.provider.mysql.*;


public class Main {
	
	public static void main(String[] args) throws java.sql.SQLException{
		
		RDBMSModelFactory factory = new MySQLModelFactory("jdbc:mysql://localhost/mapper", "root", "");
		
		//test1(factory);
		//schema(factory);
		test2(factory);
	}
	
	
	public static void schema(RDBMSModelFactory factory) {
		factory.getSchema().migrate(Student.class, WebService.class, Corporation.class);
		System.out.println(factory.getSchema());
	}
	
	public static void test1(RDBMSModelFactory factory) {
		
		System.out.println("\nmany to many relationships test: ");
		
		System.out.println("\nGet a corporation [c]: \n");
		Corporation c = factory.find(Corporation.class, "CorporateId = ?", 1)[0];
		
		System.out.println(c);
		
		System.out.println("\nGet stage students [s = c.getStageStudents()]: \n");
		
		Student[] students = c.getStageStudents();
		printAll(students);
		
		System.out.println("\nStudent extends Account. For each student getWebService: [s.getWebService()]\n");
		
		for (Student s : students) System.out.println(s.getWebService());
		
		
	}
	
	public static void test2(RDBMSModelFactory factory) {
		
		Student[] studs = 	new Finder<Student>(Student.class, factory).whereLt("avg",28).include("corporations").last(2);
		
		for (Student s : studs) {
			System.out.println(s.toXml()+"\n\n");
			//System.out.println(s.toJson()+"\n\n");
			//System.out.println(s);
			//printAll(s.getCorporations());

		}
	}
	
	public static void printAllNames(Corporation[] model) {
		for (Corporation m : model) System.out.println(m);
	}
	
	public static void printAll(Model[] model) {
		for (Model m : model) System.out.println("\n"+m);
	}
}