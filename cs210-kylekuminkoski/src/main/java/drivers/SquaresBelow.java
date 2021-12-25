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
 * For Lab 1, implement this driver.
 *
 * Examples:
 * 	SQUARES BELOW 20
 * 	SQUARES BELOW 30 AS a
 * 	SQUARES BELOW 15 AS a, b
 *
 * Response 1:
 *  success flag
 *  message "There were 5 results."
 * 	result table
 * 		primary integer column "x", integer column "x_squared"
 *		rows [0, 0]; [1, 1]; [2, 4]; [3, 9]; [4, 16]
 *
 * Response 2:
 *  success flag
 *  message "There were 6 results."
 * 	result table
 * 		primary integer column "a", integer column "a_squared"
 *		rows [0, 0]; [1, 1]; [2, 4]; [3, 9]; [4, 16]; [5, 25]
 *
 * Response 3:
 *  success flag
 *  message "There were 4 results."
 * 	result table
 * 		primary integer column "a", integer column "b"
 *		rows [0, 0]; [1, 1]; [2, 4]; [3, 9]
 */


@Deprecated
public class SquaresBelow implements Driver {
	static final Pattern pattern = Pattern.compile(
			//SQUARES\s+BELOW\s+([0-9]+)(?:\s+AS\s+([a-z][a-z0-9_]*)(?:\s*,\s*([a-z][a-z0-9_]*))?)?
			"SQUARES\\s+BELOW\\s+([0-9]+)(?:\\s+AS\\s+([a-z][a-z0-9_]*)(?:\\s*,\\s*([a-z][a-z0-9_]*))?)?",
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
			
			
			
			String name = matcher.group(2) != null ? matcher.group(2) : "x";
			if (name.length() > 15)
				return new Response(query, Status.FAILED, "Column name is too long. Must be 1 to 15 characters", null);
			
			String sname = matcher.group(3) != null ? matcher.group(3) : name + "_squared";
			if (sname.length() > 15)
				return new Response(query, Status.FAILED, "Column name is too long. Must be 1 to 15 characters", null);
			
			if (name.equals(sname))
			return new Response(query, Status.FAILED, "Column names must be unique", null);	
				
			Table result_table = new Table(
				SearchMap.of(
					"table_name", "_squares",
					"column_names", List.of(name, sname),
					"column_types", List.of("integer", "integer"),
					"primary_index", 0
				),
				new SearchMap<>()
			);

			for (int i = 0; i * i < upper; i++) {
				List<Object> row = new LinkedList<>();
				row.add(i);
				row.add( i * i);
				result_table.state().put(i, row);
			}

			return new Response(query, Status.SUCCESSFUL, null, result_table);
		}
	}

