package drivers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import maps.HashArray;
import maps.SearchMap;
import types.Driver;
import types.Response;
import types.Status;
import types.Table;


public class DescribeTable implements Driver {
	static final Pattern pattern = Pattern.compile(
			//DESCRIBE\s+TABLE\s+([a-z][a-z0-9_]*)
			"DESCRIBE\\s+TABLE\\s+([a-z][a-z0-9_]*)",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return new Response(query, Status.UNRECOGNIZED, null, null);

		String tableName = matcher.group(1);
		
		Table source_table = db.tables().get(tableName);
		
		if (source_table == null)
			return new Response(query, Status.FAILED, "Table " + tableName + " does not exist.", null);
		
		
	
		
		
		List<String> names =  source_table.getColumnNames();
		
		List<String> types =  source_table.getColumnTypes();
		
		int primaryIndex =  source_table.getPrimaryIndex();
		
		Table result_table = new Table(
				SearchMap.of(
					"table_name", "_describe_" + tableName,
					"column_names", List.of("column", "name", "type", "is_primary"),
					"column_types", List.of("integer", "string", "string", "boolean"),
					"primary_index", 0
				),
				new SearchMap<>()
			);
		
		for(int i = 0; i < names.size(); i++) {
			result_table.state().put(
					i, List.of(i, names.get(i), types.get(i), primaryIndex == i)
					);
		
		}
		
		
		return new Response(query, Status.SUCCESSFUL, String.format("Table <%s> has %d columns.", tableName, names.size()), result_table);
		
		
		
	}
}
