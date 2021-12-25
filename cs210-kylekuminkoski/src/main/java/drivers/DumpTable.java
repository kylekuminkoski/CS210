package drivers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import maps.SearchMap;
import types.Driver;
import types.Response;
import types.Status;
import types.Table;


public class DumpTable implements Driver {
	static final Pattern pattern = Pattern.compile(
			//DUMP\s+TABLE\s+([a-z][a-z0-9_]*)
			"DUMP\\s+TABLE\\s+([a-z][a-z0-9_]*)",
		
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return new Response(query, Status.UNRECOGNIZED, null, null);

		String tableName = matcher.group(1);
		
		
		if (!db.tables().contains(tableName))
			return new Response(query, Status.FAILED, "Table " + tableName + " does not exist.", null);
		
		Table table = db.tables().get(tableName);
	
		int rowCount =	table.state().size();
		
		
		
		
		
		return new Response(query, Status.SUCCESSFUL, "Table " + tableName + " containing " + rowCount + " rows was successfully dumped.", table);
		
		
		
	}
}
