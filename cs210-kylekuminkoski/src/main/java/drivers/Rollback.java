package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import types.Driver;
import types.Map;
import types.Response;
import types.Status;
import types.Table;

/*
 * Example:
 *  ECHO "Hello, world!"
 *
 * Response:
 *  success flag
 *  message "Hello, world!"
 *  no result table
 */


	


public class Rollback implements Driver {
	
	static final Pattern pattern = Pattern.compile(
		
		//Rollback(?:\s+To\s+Savepoint\s+)?([a-z]+[a-z0-9]*(?:_+\w*)?)?"
		"Rollback(?:\\s+To\\s+Savepoint\\s+)?([a-z]+[a-z0-9]*(?:_+\\w*)?)?",
		Pattern.CASE_INSENSITIVE
	);
	
	
	
	

	@Override
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return new Response(query, Status.UNRECOGNIZED, null, null);
		
		if(!apps.Console.transactionCheck)
			return new Response(query, Status.FAILED, "There is no transaction in progress", null);
		
		
		
		Begin transaction = new Begin();
		
		
		
		if(matcher.group(1) == null) {
		db.rollback();
		
		return new Response(query, Status.SUCCESSFUL, "The database has been restored to its original state before the transaction", null);
		}
		
		
		
		String saveName = matcher.group(1).strip();
		int savePointer = -1;
		
		for(String s: db.savepoints()) {
			System.out.println(s);
			if(s.equalsIgnoreCase(saveName)) {
				savePointer = db.savepoints().indexOf(saveName);
			}
		}
		
		if(savePointer == -1) {
			return new Response(query, Status.FAILED, "This savepoint name does not exist.", null);
		}
		
		db.rollbackToSavepoint(saveName, savePointer);

		return new Response(query, Status.SUCCESSFUL, "Savepoint created", null);
	}
}
