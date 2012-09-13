// 
//  Searcher
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper;

import modelmapper.exception.*;

import java.util.*;

public class Finder<T extends Model> {
	
	private static String EQ_OP  = " = ";
	private static String LT_OP  = " < ";
	private static String GT_OP  = " > ";
	private static String NOT_OP = " <> ";
	
	private Class model;
	private ModelFactory factory;
	
	private List<Object> params = new ArrayList<Object>();
	private List<String> connections = new ArrayList<String>();
	
	private String criteria = "", orderBy = null;
	private int limit = 0;
	private boolean desc = true;
	
	public Finder(Class model, ModelFactory factory) {
		if (model.isAssignableFrom(Model.class))
			throw new IllegalArgumentException("Not a Model: " + model);
		this.model = model;
		this.factory = factory;
	}
	

	/* TODO: Not Implemented yet */
	public long count() { return 0; }
	
	
	private T[] get(int limit, boolean desc, boolean invert) {
		
		String id = factory.getCache().getModelId(model);
		String tN = factory.getCache().tableName(model);
		
		if (orderBy == null) orderBy = tN + "." + id;
		
		Model[] all = 
			factory.advancedFind(model, criteria, orderBy, desc, limit, 
			null, toArray(params));
					
		if (connections.size() == 0) return (T[]) all;
		
		/* NOT SUPPORTED. (Workaround for maximise compatibility)
		 * This version of MySQL doesn't yet support 
		 * 'LIMIT & IN/ALL/ANY/SOME subquery'
		 */
		if (limit > 0) {
			criteria = " ( ";
			params =  new ArrayList<Object>();
			
			boolean first = true;
			for (Model m : all) {
				criteria += first ? " " : " or ";
				criteria += tN + "." + id + " = ? ";
				params.add(m.fields().get(id));
				first = false;
				orderBy = tN + "." + id;
			}
			
			criteria += " ) ";
		}
		
		/* A query for every inclusion.
		/* In the case the user call a method include(...) on a Finder
		 * the relationships have to be fetched in a single query for avoid
		 * the N + 1 query problem in a loop.
		 * This section of code handle this problem by adding the right
		 * JOIN statement, and later the packing will be different.
		 */
		for (String c : connections) {
			
			Model[] results = 
					factory.advancedFind(model, criteria, orderBy, desc,
					0, new String[]{c}, toArray(params));
							
				
			int i = 0, j = 0;	
			for (; i < all.length; i++) {
				if (j < results.length && all[i].equalsId(results[j])) {
					
					/* A little bit too much into ModelProxy implementation
					 * but it's useful in order to achieve good performancies
					 */
					ModelProxy objI = all[i].modelImplementation();
					ModelProxy objJ = results[j++].modelImplementation();
					
					objI.fetched.put(c, objJ.fetched.get(c));
					
				} else all[i].modelImplementation().addModels(c, null, true);
			}
		}
		
		if (invert) {
			List<T> back = new ArrayList<T>();
						
			for (int i = 0; i < all.length; i++) 
				back.add( (T) all[all.length - 1 - i]);
				
			all = back.toArray((T[]) 
				  java.lang.reflect.Array.newInstance(model, back.size()));
		}
		
		return (T[]) all;
	}


	
	public T[] all() { return get(0, desc, false); }
	
	public T first() { 
		T[] results = get(1, desc, false);
		if (results.length == 1) return (T) results[0];
		else return null;
	}
	
	public T last() { 
		T[] results = get(1, !desc, false);
		if (results.length == 1) return (T) results[0];
		else return null;
	}
	
	public T[] first(int limit) { 
		return get(limit, desc, false);
	}
	
	public T[] last(int limit) { 
		return get(limit, !desc, true);
	}
	
	public Finder<T> where(String field, Object... args) { 
		return buildCriteria(field, EQ_OP, args);
	}
	
	public Finder<T> whereLt(String field, Object... args) { 
		return buildCriteria(field, LT_OP, args);
	}
	
	public Finder<T> whereGt(String field, Object... args)  { 
		return buildCriteria(field, GT_OP, args);
	}
	
	public Finder<T> whereNot(String field, Object... args)  { 
		return buildCriteria(field, NOT_OP, args);
	}
	
	/* TODO: Not Implemented yet */
	public Finder<T> include(String connectionField) { 
			
		//for (String c : connectionField) connections.add(c);
		connections.add(connectionField);
		
		return new Finder<T>(model, factory, criteria, orderBy, 
							 desc, params, connections);
	}
	
	public Finder<T> orderByDesc(String field) { 
		return orderBy(field, true);
	}
	
	public Finder<T> orderByAsc(String field) { 
		return orderBy(field, false);
	}
		
	/**
	 * Build a valid <code>WHERE</code> criteria, from the searcher
	 * <code>where*</code> method wrapper.
	 * @param field The parameter of the search.
	 * @param op The operator
	 * @param params a list of possible values (they will be all putted in 
	 * <code>OR</code>)
	 * in the <code>WHERE</code> condition.
	 */
	private Finder<T> buildCriteria(String field, String op, Object... args) {
		

		criteria += criteria.equals("") ? " " : " and ";
		
		criteria += "(";
		boolean first = true;
		for (Object o : args) {
			criteria += first ? " " : " or ";
			criteria += field + " " + op + " ? ";
			first = false;
		}
		criteria += ")";
		
		for (Object o : args) params.add(o);
				
		/* Finders are handled ad immutable objects */
		return new Finder<T>(model, factory, criteria, orderBy, 
							 desc, params, connections);
	}
	
	private Finder<T> orderBy(String field, boolean desc) {
		this.orderBy = field;
		this.desc = desc;
		
		return new Finder<T>(model, factory, criteria, orderBy, 
							 desc, params, connections);
	}
	
	private static Object[] toArray(List<Object> objects) {
		Object[] back = new Object[objects.size()];
		
		for (int i = 0; i < objects.size(); i++) back[i] = objects.get(i);
		return back;
	}
	
	private static String[] toArrayString(List<String> objects) {
		String[] back = new String[objects.size()];
		
		for (int i = 0; i < objects.size(); i++) back[i] = objects.get(i);
		return back;
	}
	
	private Finder(Class model, ModelFactory factory, String criteria, 
	String orderBy, boolean desc, List<Object> params, List<String> conn) {
		/* fields init */
		this.model = model; 
		this.factory = factory; 
		this.desc = desc;
		
		/* string clonation */
		this.criteria = new String(criteria);
		if (orderBy != null) this.orderBy = new String(orderBy);
		
		/* adding all the elements */
		this.params.addAll(params);
		this.connections.addAll(conn);
	}
}