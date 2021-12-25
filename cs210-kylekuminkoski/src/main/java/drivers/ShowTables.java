package drivers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import maps.SearchMap;
import types.Driver;
import types.Entry;
import types.Response;
import types.Status;
import types.Table;


public class ShowTables implements Driver {
	static final Pattern pattern = Pattern.compile(
			//\s*SHOW\s+TABLES\s*
			"SHOW\\s+TABLES",
		
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Response execute(String query, Database db) {
		Matcher match = pattern.matcher(query.strip());
		
		if(!match.matches()) 
			return new Response(query, Status.UNRECOGNIZED, null, null);
		
		int upper = 0;

		Table result_table = new Table(
			SearchMap.of(
				"table_name", "_tables",
				"column_names", List.of("table_name", "row_count"),
				"column_types", List.of("string", "integer"),
				"primary_index", 0
			),
			new SearchMap<>()
		);

		for (Entry<String, Table> entry: db.tables()) {
			List<Object> row = new LinkedList<>();
			String name = entry.key();
			int rowCount = entry.value().state().size();
			
			row.add(name);
			row.add(rowCount);
			
			result_table.state().put(name, row);
			
		}

		return new Response(query, Status.SUCCESSFUL, "Number of tables in database: " + db.tables().size(), result_table);
		 
		
		
		
		
		
	}
}
