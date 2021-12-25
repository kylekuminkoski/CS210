package apps;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.LinkedList;
import drivers.Begin;
import drivers.Commit;
import drivers.CreateTable;
import drivers.DescribeTable;
import drivers.DropTable;
import drivers.DumpTable;
import drivers.Echo;
import drivers.InsertReplaceTable;
import drivers.Range;
import drivers.Rollback;
import drivers.Savepoint;
import drivers.SelectFromTable;
import drivers.ShowTables;
import drivers.SquaresBelow;
import maps.SearchMap;
import types.Driver;
import types.Map;
import types.Response;
import types.Status;
import types.Table;

/**
 * This class implements a
 * database management system.
 * <p>
 * Additional protocols may be added.
 */

public class Database implements Closeable {
	private Map<String, Table> tables;
	private final List<Driver> drivers;
	 List<String> savepointNames;
	private Map<String, Table> backupTables;
	private List<Map<String, Table>> savepoints;


	/**
	 * Initialize the tables and drivers.
	 * <p>
	 * Do not modify the protocol.
	 */
	public Database() {
		this.tables = new SearchMap<>();

		this.drivers = List.of(	
			new Echo(),
			new Range(),
			new SquaresBelow(),
			new CreateTable(),
			new DropTable(),
			new ShowTables(),
			new DumpTable(),
			new InsertReplaceTable(),
			new SelectFromTable(),
			new Begin(),
			new Rollback(),
			new Savepoint(),
			new Commit()
			
			
			
			
			
		);
		
		this.backupTables = new SearchMap<>();
		this.savepointNames = new LinkedList<>();
		this.savepoints = new LinkedList<>();
	}

	/**
	 * Returns the tables of this database.
	 * <p>
	 * Do not modify the protocol.
	 *
	 * @return the tables.
	 */
	public Map<String, Table> tables() {
		return tables;
	}
	
	public List<String> savepoints() {
		return savepointNames;
	}
	
	public void rollback() {
		
				
		tables = this.backupTables;
	}
	
	public void backup(){
		backupTables = this.tables;
		System.out.println(backupTables);
	}
	
	public void createSavepoint(String saveName, Database db) {
		db.savepointNames.add(saveName);
		db.savepoints.add(db.tables());
	}
	
	public void rollbackToSavepoint(String saveName, int index) {
	
		tables = savepoints.get(index);
	}
	
	public void commit() {
		this.backupTables = null;
		this.savepointNames = null;
		this.savepoints = null;
		this.backupTables = new SearchMap<>();
		this.savepointNames = new LinkedList<>();
		this.savepoints = new LinkedList<>();
	}
	
	

	/**
	 * Interprets a list of queries and returns a list
	 * of responses to each query in sequence.
	 * <p>
	 * Do not modify the protocol.
	 *
	 * @param queries the list of queries.
	 * @return the list of responses.
	 */
	public List<Response> interpret(List<String> queries) {
		List<Response> responses = new LinkedList<>();

		outer:
		for (String query: queries) {
			for (Driver driver: drivers) {
				Response res = driver.execute(query, this);
				if (res.status() != Status.UNRECOGNIZED) {
				responses.add(res);
				continue outer;
				}
			}
			responses.add(new Response(query, Status.UNRECOGNIZED, null, null));	
		}

		return responses;
	}

	/**
	 * Execute any required tasks when
	 * the database is closed.
	 * <p>
	 * Do not modify the protocol.
	 */
	@Override
	public void close() throws IOException {

	}
}
