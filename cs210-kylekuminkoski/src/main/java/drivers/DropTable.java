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


public class DropTable implements Driver {
	static final Pattern pattern = Pattern.compile(
			//\s*DROP\s+TABLE\s*(\s*[a-z]\s*[0-9]*[\s*_\s*]*\s*[_]*[a-z]*[_]*[a-z0-9]*[_]*)\s*
			"\\s*DROP\\s+TABLE\\s+(\\s*[a-z]\\s*[0-9]*[\\s*_\\s*]*\\s*[_]*[a-z]*[_]*[a-z0-9]*[_]*)\\s*",
		
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
		
		Table table = db.tables().remove(tableName);
		
		int rowCount =	table.state().size();
		
		
		
		return new Response(query, Status.SUCCESSFUL, "Table " + tableName + " containing " + rowCount + " rows was successfully removed.", null);
		
		
		
	}
}
