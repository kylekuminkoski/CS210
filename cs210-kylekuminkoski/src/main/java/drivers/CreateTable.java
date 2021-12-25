package drivers;

import java.util.ArrayList;
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


public class CreateTable implements Driver {
	static final Pattern pattern = Pattern.compile(
			//CREATE\s+TABLE\s+([a-z]+[a-z0-9]*(?:_+\w*)?)\s+\(((?:\s*(?:\w+)\s+(?:INTEGER|STRING|BOOLEAN)(?:\s+PRIMARY)?)(?:\s*,\s*(?:\w+)\s+(?:INTEGER|STRING|BOOLEAN)(?:\s+PRIMARY)?)*)\s*\)
			
			"CREATE\\s+TABLE\\s+([a-z]+[a-z0-9]*(?:_+\\w*)?)\\s+\\(((?:\\s*(?:\\w+)\\s+(?:INTEGER|STRING|BOOLEAN)(?:\\s+PRIMARY)?)(?:\\s*,\\s*(?:\\w+)\\s+(?:INTEGER|STRING|BOOLEAN)(?:\\s+PRIMARY)?)*)\\s*\\)",
		
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches())
			return new Response(query, Status.UNRECOGNIZED, null, null);

		String tableName = matcher.group(1);
		
		
		if (db.tables().contains(tableName))
			return new Response(query, Status.FAILED, "Table name must be unique", null);
		
		
		if (tableName.length() > 15)
			return new Response(query, Status.FAILED, "Table name length is too long. Must be between 1 and 15 characters.", null);

		
		List<String> columns = Arrays.asList(matcher.group(2).split("\\s*,\\s*"));
		
		int upper = columns.size();
		
		if (upper > 15)
			return new Response(query, Status.FAILED, "Too many columns defined. There must be between 1 and 15 columns", null);

	List<String> columnNames = new LinkedList<>(), columnTypes = new LinkedList<>();
	int primaryIndex = -1;
		
		for (String s : columns) {
			String columnDefs[] = (s.strip()).split(" ");
			
			for(String name: columnNames) {
				
					if(name.equalsIgnoreCase(columnDefs[0]))
						return new Response(query, Status.FAILED, "Column names must be unique.", null);
			
			}
			
			
			
				
				columnNames.add(columnDefs[0]);
				columnTypes.add(columnDefs[1].toLowerCase());
				
				if(columnDefs.length> 2) {
					if (primaryIndex == -1) {
					primaryIndex = columnNames.indexOf(columnDefs[0]);
					}else {
						return new Response(query, Status.FAILED, "The primary column has already been set.", null);
					}
				}
			
				}
			
		
		
		
		
		if (primaryIndex == -1)
			return new Response(query, Status.FAILED, "No primary column has been set.", null);
		
		
		
		
		
		Table result_table = new Table(
				SearchMap.of(
					"table_name", tableName,
					"column_names", columnNames,
					"column_types", columnTypes,
					"primary_index", primaryIndex
				),
				new SearchMap<>()
			);
		
		
		
		
		
		
		db.tables().put(tableName, result_table);
		
		return new Response(query, Status.SUCCESSFUL, "Table Name: " + tableName + " contains " + upper + " columns in it.", null);
	}
}
