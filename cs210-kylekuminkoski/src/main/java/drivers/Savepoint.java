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





public class Savepoint implements Driver {
	
	private Database data;
	
	static final Pattern pattern = Pattern.compile(
		
		//Savepoint\s+([a-z]+[a-z0-9]*(?:_+\w*)?)"
		"Savepoint\\s+([a-z]+[a-z0-9]*(?:_+\\w*)?)",
		Pattern.CASE_INSENSITIVE
	);
	

	
	
	@Override
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return new Response(query, Status.UNRECOGNIZED, null, null);
		
		if(!apps.Console.transactionCheck)
			return new Response(query, Status.FAILED, "There is no transaction in progress", null);
		
		
			
		Database newSave = db;
		
		
		String saveName = matcher.group(1).strip();
		for(String s: db.savepoints()) {
			System.out.println(s);
			if(s.equalsIgnoreCase(saveName)) {
				return new Response(query, Status.FAILED, "This savepoint name has already been used.", null);
			}
	
		}
		
		
		
		
		
		db.createSavepoint(saveName, db);
		
		
		
		
		

		return new Response(query, Status.SUCCESSFUL, "Savepoint created", null);
	}
}
