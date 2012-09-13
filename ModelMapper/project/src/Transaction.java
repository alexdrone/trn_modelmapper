// 
//  Transaction
//  ModelMapper
//  
//  Created by Querella Luca and Usbergo Alex on 2010-01-31.
//  Università di Torino 
//

package modelmapper;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Allows for the syntactically simple use of database transactions within 
 * the ModelMapper API.  This class's syntax is modeled after the 
 * <code>transaction do ... end</code> syntax provided by Rails's 
 * ActiveRecord ORM.  The intention
 * is to provide the simplest possible encapsulation around the transaction
 * functionality. 
 * 
 * The design behind <code>Transaction</code> is modeled after the
 * following code snippet:
 * 
 * <pre>new Transaction&lt;Object&gt;(manager) {
 *     public Object run() {
 *         Account a = modelFactory().find(Account.class, "Id = ?", 1);
 *         Account b = modelFactory().find(Account.class, "Id = ?", 2);
 *         
 *         a.setBalance(a.getBalance() - 1000);
 *         a.save();
 *         
 *         b.setBalance(b.getBalance() + 1000);
 *         b.save();
 *         
 *         return null;
 *     }
 * }.execute();</pre>
 * 
 * The transaction will be committed only after the <code>run()</code>
 * method returns.  Thus, <code>a.save()</code> doesn't immediately modify
 * the database values, only upon the committal of the transaction.  If any
 * conflicts are detected, JDBC will automatically throw an 
 * {@link SQLException}. <code>Transaction</code> catches this exception and 
 * rolls back the transaction, ensuring data integrity.  
 + * Once the transaction is rolled back, the exception is rethrown from the 
 <code>execute()</code> method.
 * 
 * In cases where the transaction generates data which must be returned, this
 * can be accomplished by returning from the {@link #run()} method against the
 * parameterized type.  Thus if a transaction to create an account is utilized:
 * 
 * <pre>Account result = new Transaction&lt;Account&gt;(factory) {
 *     public Account run() throws SQLException {
 *         Account back = factory.create(Account.class);
 *         
 *         back.setBalance(0);
 *         back.save():
 *         
 *         return back;
 *     }
 * }.execute();</pre>
 * 
 * The value returned from <code>run()</code> will be passed back up the call
 * stack to <code>execute()</code>, which will return the value to the caller.
 * Thus in this example, <code>result</code> will be precisely the 
 * <code>back</code>
 * instance from within the transaction.  This feature allows data to escape 
 * the scope of the transaction, thereby achieving a greater usefulness.
 * 
 * <p>The JDBC transaction type used is 
 * {@link Connection#TRANSACTION_SERIALIZABLE}.</p>
 * 
 * @see java.sql.Connection
 */
public abstract class Transaction<T> {
	final protected RDBMSModelFactory factory;
	
	/**
	 * Creates a new <code>Transaction</code> using the specified
	 * {@link ModelFactory} instance.  If the specified instance is 
	 * <code>null</code>, an exception will be thrown.
	 * 
	 * @param factory	The <code>ModelFactory</code> instance against 
	 * which the transaction should run.
	 * @throws IllegalArgumentException	If the {@link ModelFactory} 
	 * instance is <code>null</code>.
	 */
	public Transaction(RDBMSModelFactory factory) {
		if (factory == null) {
			throw new IllegalArgumentException("Null ModelFactory");
		}
		
		this.factory = factory;
	}
	
	protected final ModelFactory modelFactory() {
		return factory;
	}
	
	/**
	 * <p>Executes the transaction defined within the overridden 
	 * {@link #run()}method.  
	 * If the transaction fails for any reason (such as a conflict), it will
	 * be rolled back and an exception thrown.  The value returned from the
	 * <code>run()</code> method will be returned from 
	 * <code>execute()</code>.</p>
	 * 
	 * <p>Custom JDBC code can be executed within a transaction. 
	 * It is technically possible to commit a  transaction prematurely, 
	 * disable the transaction entirely, or otherwise really
	 * mess up the internals of the implementation.  You do <i>not</i> have to
	 * call <code>setAutoCommit(boolean)</code> on the {@link Connection}
	 * instance retrieved .  The connection is already initialized and within 
	 * an open  transaction by the time it gets to your custom code within the 
	 * transaction.</p>
	 * 
	 * @return	The value (if any) returned from the transaction 
	 * <code>run()</code>
	 * @throws SQLException	If the transaction failed for any reason and was 
	 * rolled back.
	 * @see #run()
	 */
	public T execute() throws SQLException {
		Connection conn = null;
		SQLException toThrow = null;
		T back = null;
		
		try {
			conn = factory.getConnection();
			
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			conn.setAutoCommit(false);
			
			back = Transaction.this.run();
			
			conn.commit();
		} catch (SQLException e) {
			if (conn != null) {
				try {conn.rollback();
				} catch (SQLException e1) { /* toHandle */ }
			}
			
			toThrow = e;
		} finally {
			if (conn == null) { return null; }
			
			try {
				conn.setAutoCommit(true);
				
				conn.close();
			} catch (SQLException e) {
			}
		}
		
		if (toThrow != null) { throw toThrow; }
		
		return back;
	}
	
	/**
	 * <p>Called internally by {@link #execute()} to actually perform the 
	 * actions within the transaction.  Any <code>SQLException(s)</code> 
	 * should be allowed to propogate back up to the calling method, which 
	 * will ensure that the transaction is rolled back and the proper 
	 * resources disposed.  
	 * If the transaction generates a value which must be passed back to 
	 * the calling method, this value may be returned as long as it is of 
	 * the parameterized type. If no value is generated, <code>null</code> 
	 * is an acceptable return value.</p>
	 * 
	 * <p>Be aware that <i>any</i> operations performed within a transaction
	 * (even if indirectly invoked by the <code>run()</code> method) will use
	 * the <i>exact same</i> {@link Connection} instance.  This is to ensure
	 * integrity of the transaction's operations while at the same time 
	 * allowing custom JDBC code and queries within the transaction.</p>
	 * 
	 * @return	Any value which must be passed back to the calling point 
	 * (outside the transaction), or <code>null</code>.
	 * @throws SQLException	If something has gone wrong within the transaction 
	 * and it requires a roll-back.
	 */
	protected abstract T run() throws SQLException;
}