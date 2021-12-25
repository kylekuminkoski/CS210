package drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apps.Database;
import types.Driver;
import types.Response;
import types.Status;


public class InsertReplaceTable implements Driver {
	//Pattern for the driver
	static final Pattern pattern = Pattern.compile(
		//(INSERT|REPLACE)\s+INTO\s+([a-z]+[a-z0-9]*(?:_+\w*)?)\s+(?:(?:\((?:(\s*(?:\w+)(?:(?:\s*,\s*(?:\w+))*)\s*)?)\)\s+)?)VALUES\s+\(((?:\s*(?:(?:\")?\s*[a-z0-9_ +!?\\]+\s*(?:\")?)(?:(?:\s*,\s*(?:(?:\")?\s*[a-z0-9_+!?\\]+\s*(?:\")?))*)\s*))\)
		"(INSERT|REPLACE)\\s+INTO\\s+([a-z]+[a-z0-9]*(?:_+\\w*)?)\\s+(?:(?:\\((?:(\\s*(?:\\w+)(?:(?:\\s*,\\s*(?:\\w+))*)\\s*)?)\\)\\s+)?)VALUES\\s+\\(((?:\\s*(?:(?:\\\")?\\s*[a-z0-9_+!\\-? 	\\\\]*\\s*(?:\\\")?)(?:(?:\\s*,\\s*(?:(?:\\\")?\\s*[a-z0-9_+\\-!? 	\\\\]*\\s*(?:\\\")?))*)\\s*))\\)",
		Pattern.CASE_INSENSITIVE
	);
	
	//Pattern for Strings
	static final Pattern S = Pattern.compile(
			//\"\s*?[a-z0-9_+!? 	\\]+\s*\"
			"\\\"\\s*?[a-z0-9_+!? 	\\\\]{0,127}\\s*\\\"",
			Pattern.CASE_INSENSITIVE
		);
	
	//Pattern for Integer
	static final Pattern I = Pattern.compile(
			//((?:\+|\-)?[1-9]+[0-9]*|0)
			"((?:\\+|\\-)?[1-9]+[0-9]*|0)",
			Pattern.CASE_INSENSITIVE
		);
	
	//Pattern for Boolean
	static final Pattern B = Pattern.compile(
			//(True|False|Null)
			"(True|False|Null)",
			Pattern.CASE_INSENSITIVE
		);
	
	//Pattern for Null
	static final Pattern N = Pattern.compile(
			//Null
			"Null",
			Pattern.CASE_INSENSITIVE
		);
	
	
	
	@SuppressWarnings( "unchecked")
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) 
			return new Response(query,Status.UNRECOGNIZED,null,null);
		
		
		
		String queryType = matcher.group(1);
		
		
		String tableName = matcher.group(2).strip();
			
		
		if(!db.tables().contains(tableName)) {
			return new Response(query,Status.FAILED,"This table does not exist.",null);
		}
		
		
		String columnValues[] = matcher.group(4).split(",");
		
		List<Integer> columnPointers = new LinkedList<>();
	
		List<Object> columnNamesDb = (List<Object>) db.tables().get(tableName).schema().get("column_names");
		
		if (matcher.group(3) != null) {
			String columnNames[] = matcher.group(3).split(",");
			
			if(columnValues.length != columnNames.length) {
				return new Response(query,Status.FAILED,"You have more column values than columns.", null);
			}
			
			List<String> checkName = new LinkedList<>();
			
			for(String s: columnNames) {
				String nameCheck= s.strip();

				for(String a: checkName) {
					if(nameCheck.equalsIgnoreCase(a)) {
						return new Response(query,Status.FAILED,"You have duplicate column names.",null);
					}
				}
				checkName.add(nameCheck);

				for(Object o: columnNamesDb) {
					if(nameCheck.equalsIgnoreCase((String) o)) {
						
						columnPointers.add(columnNamesDb.indexOf(o));
						
					}
				}
			}
		}
		else {
		
			for(int i=0; i<db.tables().get(tableName).schema().size()-1; i++) {
				
				columnPointers.add(i);	
				
			}
			
			if(columnValues.length != columnPointers.size()) {
				
				return new Response(query,Status.FAILED,"You have more values than columns " ,null);
			
			}
		}
		
		List<Object> row = new LinkedList<>();
		
		for(int i=0; i<db.tables().get(tableName).schema().size()-1; i++) {
			
			row.add(null);
		}
		
		
		List<Object> columnTypes = (List<Object>) db.tables().get(tableName).schema().get("column_types");
		
		int primaryIndex= (int) db.tables().get(tableName).schema().get("primary_index");
		
		if(!columnPointers.contains( primaryIndex)) {
			return new Response(query,Status.FAILED,"There is no primary column.",null);
		}
		
		for(int i=0; i < columnValues.length; i++) {
			String value = columnValues[i].strip();
			
			Matcher strings = S.matcher(value.strip());
			Matcher integers = I.matcher(value.strip());
			Matcher bools = B.matcher(value.strip());
			Matcher nulls = N.matcher(value.strip());
			
			if(strings.matches()) {
				value = value.strip().substring(1,value.strip().length()-1);
				
				if(i == primaryIndex && db.tables().get(tableName).state().contains(value) && queryType.equalsIgnoreCase("insert"))
					return new Response(query,Status.FAILED,"Primary value cannot be the same unless using Replace query",null);
				
				if(((String) columnTypes.get(columnPointers.get(i))).equalsIgnoreCase("string")) {
					row.set(columnPointers.get(i), value);
				}
				else {
					return new Response(query,Status.FAILED,"The value type does not match.",null);
				}
			}
			else if(integers.matches()) {
				try {
				   int columnIntegerValue = Integer.parseInt(value.strip());
				   
					if(i == primaryIndex && db.tables().get(tableName).state().contains(columnIntegerValue) && queryType.equalsIgnoreCase("insert"))
						return new Response(query,Status.FAILED,"Primary value cannot be the same unless using Replace query",null);
					
				   if(((String) columnTypes.get(columnPointers.get(i))).equalsIgnoreCase("integer")) {
					row.set(columnPointers.get(i), columnIntegerValue);
				   }
				   else {
						return new Response(query,Status.FAILED,"The value type does not match.",null);
					}
				  } catch (NumberFormatException e) {
					  return new Response(query,Status.FAILED,"Exceeded integer limit",null);
				 }
			}
			else if(nulls.matches()) {
				if(i == primaryIndex)
					return new Response(query,Status.FAILED,"Primary value cannot be null",null);
			}
			else if(bools.matches()) {
				Boolean columnBooleanValue = Boolean.parseBoolean(value.strip());
				
				if(i == primaryIndex && db.tables().get(tableName).state().contains(columnBooleanValue) && queryType.equalsIgnoreCase("insert"))
					return new Response(query,Status.FAILED,"Primary value cannot be the same unless using Replace query",null);
				
				if(( (String) columnTypes.get(columnPointers.get(i))).equalsIgnoreCase("boolean")) {
					row.set(columnPointers.get(i), columnBooleanValue);
				}
				else {
					return new Response(query,Status.FAILED,"The value type does not match.",null);
				}
			}
			else {
				return new Response(query,Status.FAILED,"The value type is incorrect.",null);
			}
		}
		if(row.get(primaryIndex)==null) {
			return new Response(query,Status.FAILED,"The primary column cannot be null.",null);
		}
		
		if(queryType.equalsIgnoreCase("replace")) {
			db.tables().get(tableName).state().put(row.get(primaryIndex), row);
		}
		else {
			db.tables().get(tableName).state().put(row.get(primaryIndex), row);
		}
		int rows = db.tables().get(tableName).state().size();
		
		return new Response(query, Status.SUCCESSFUL, "Inserted "+columnPointers.size()+" rows into "+tableName+" with "+rows+" rows currently.", null);
	}
}
