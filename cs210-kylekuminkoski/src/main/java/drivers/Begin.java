package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import types.Driver;
import types.Response;
import types.Status;





public class Begin implements Driver {
	
	static final Pattern pattern = Pattern.compile(
		//\s*Begin\s*"
		"\\s*Begin\\s*",
		Pattern.CASE_INSENSITIVE
	);
	

	@Override
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return new Response(query, Status.UNRECOGNIZED, null, null);
		
		
		
		if(apps.Console.transactionCheck)
			return new Response(query, Status.FAILED, "A transaction is already in progress", null);
		
		db.backup();
		
		apps.Console.transactionCheck = true;
		
		

		return new Response(query, Status.SUCCESSFUL, "A transaction has began and a copy of the database was created.", null);
	}
}
