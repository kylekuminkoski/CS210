package drivers;

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

/*
 * Examples:
 * 	RANGE 5
 * 	RANGE 3 AS x
 *
 * Response 1:
 * 	success flag
 * 	no message
 * 	result table
 * 		primary integer column "number"
 *		rows [0]; [1]; [2]; [3]; [4]
 *
 * Response 2:
 * 	success flag
 * 	no message
 * 	result table
 * 		primary integer column "x"
 *		rows [0]; [1]; [2]
 */
public class Range implements Driver {
	static final Pattern pattern = Pattern.compile(
			// RANGE\s+([0-9]+)(?:\s+AS\s+([a-z][a-z0-9_]*))?
			"RANGE\s+([0-9]+)(?:\s+AS\s+([a-z][a-z0-9_]*))?",
		
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return new Response(query, Status.UNRECOGNIZED, null, null);

		int upper;
		try {
		upper = Integer.parseInt(matcher.group(1));
		}
		catch (NumberFormatException e) {
			return new Response(query, Status.FAILED, "Number is not a valid integer", null);
		}
		
		String name = matcher.group(2) != null ? matcher.group(2) : "number";
		if (name.length() > 15)
			return new Response(query, Status.FAILED, "Column name is too long. Must be 1 to 15 characters", null);

		Table result_table = new Table(
			SearchMap.of(
				"table_name", "_range",
				"column_names", List.of(name),
				"column_types", List.of("integer"),
				"primary_index", 0
			),
			new SearchMap<>()
		);

		for (int i = 0; i < upper; i++) {
			List<Object> row = new LinkedList<>();
			row.add(i);
			
			result_table.state().put(i, row);
		}

		return new Response(query, Status.SUCCESSFUL, null, result_table);
	}
}
