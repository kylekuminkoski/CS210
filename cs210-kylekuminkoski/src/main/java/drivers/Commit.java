package drivers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import types.Driver;
import types.Response;
import types.Status;

/*
 * Example:
 *  ECHO "Hello, world!"
 *
 * Response:
 *  success flag
 *  message "Hello, world!"
 *  no result table
 */



public class Commit implements Driver {
	static final Pattern pattern = Pattern.compile(
		//escaping
		//\s*Commit\s*"
		"\\s*Commit\\s*",
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
		
		apps.Console.transactionCheck = false;
		
		db.commit();

		return new Response(query, Status.SUCCESSFUL, "The transaction has ended and all backups/savepoints have been erased", null);
	}
}
